package com.exchange.money.services;

import com.exchange.money.model.Account;
import com.exchange.money.model.Amount;
import com.exchange.money.model.Transaction;
import com.exchange.money.repos.*;
import com.exchange.money.utils.Assert;
import com.exchange.money.repos.*;

class TheTransactionService implements TransactionService {

    public static final String INSUFFICIENT_FUNDS_MSG_TEMPLATE =
            "Account had %s but needed %s to complete the transaction";
    private final AccountRepo accountRepo;
    private final TransactionRepo transactionRepo;
    private final ExchangeRateService exchangeRateService;

    public TheTransactionService(AccountRepo accountRepo, TransactionRepo transactionRepo,
                                 ExchangeRateService exchangeRateService) {
        Assert.checkNotNull(accountRepo, "accountRepo cannot be null");
        Assert.checkNotNull(transactionRepo, "transactionRepo cannot be null");
        Assert.checkNotNull(exchangeRateService, "exchangeRateService cannot be null");
        this.accountRepo = accountRepo;
        this.transactionRepo = transactionRepo;
        this.exchangeRateService = exchangeRateService;
    }

    public CreateTransactionResult createTransaction(long srcAccountId, long dstAccountId, Amount amount) {
        try {
            accountRepo.getById(srcAccountId);
            accountRepo.getById(dstAccountId);
            Transaction txn = new Transaction(srcAccountId, dstAccountId, amount);
            txn = transactionRepo.insert(txn);
            return new CreateTransactionResult(txn, CreateTransactionResult.CreationStatus.SUCCESS);
        } catch (AccountNotFoundException anfe) {
            return new CreateTransactionResult(null, CreateTransactionResult.CreationStatus.ACCOUNT_NOT_FOUND, anfe.getMessage());
        } catch (DataAccessException dae) {
            return new CreateTransactionResult(null, CreateTransactionResult.CreationStatus.INTERNAL_ERROR, dae.getMessage());
        }
    }

    public ExecuteTransactionResult executeTransaction(long transactionId) {
        Transaction txn = null;
        Account srcAccount;
        Account dstAccount;
        try {
            transactionRepo.lockById(transactionId);
            txn = transactionRepo.getById(transactionId);

            if (!txn.getStatus().equals(Transaction.TransactionStatus.PENDING)) {
                return new ExecuteTransactionResult(txn, ExecuteTransactionResult.ExecutionStatus.UNCHANGED,
                        "No changes. Transaction was already " + txn.getStatus());
            }

            accountRepo.lockById(txn.getSourceId());
            accountRepo.lockById(txn.getDestinationId());

            srcAccount = accountRepo.getById(txn.getSourceId());
            dstAccount = accountRepo.getById(txn.getDestinationId());

            Amount toWithdraw = exchangeRateService.convert(txn.getAmount(), srcAccount.getCurrency());
            if (!containsSufficientFunds(srcAccount, toWithdraw)) {
                String message = String.format(INSUFFICIENT_FUNDS_MSG_TEMPLATE, srcAccount.getBalance(), toWithdraw);
                return executionFailed(txn, ExecuteTransactionResult.ExecutionStatus.INSUFFICIENT_FUNDS, message);
            }
            Account newSrcAccount = srcAccount.withdraw(toWithdraw);
            Amount toDeposit = exchangeRateService.convert(toWithdraw, dstAccount.getCurrency());
            Account newDstAccount = dstAccount.deposit(toDeposit);

            accountRepo.update(newSrcAccount);
            accountRepo.update(newDstAccount);
            return executionSucceeded(txn);
        } catch (CouldNotLockResourceException cnlre) {
            return executionFailed(txn, ExecuteTransactionResult.ExecutionStatus.COULD_NOT_ACQUIRE_RESOURCES, cnlre.getMessage());
        } catch (TransactionNotFoundException tnfe) {
            return executionFailed(txn, ExecuteTransactionResult.ExecutionStatus.TRANSACTION_NOT_FOUND, tnfe.getMessage());
        } catch (AccountNotFoundException anfe) {
            return executionFailed(txn, ExecuteTransactionResult.ExecutionStatus.ACCOUNT_NOT_FOUND, anfe.getMessage());
        } catch (DataAccessException e) {
            return executionFailed(txn, ExecuteTransactionResult.ExecutionStatus.INTERNAL_ERROR, e.getMessage());
        } finally {
            unlockResources(transactionId, txn);
        }
    }

    private void unlockResources(long transactionId, Transaction txn) {
        try {
            transactionRepo.unlockById(transactionId);
            if (txn != null) {
                accountRepo.unlockById(txn.getSourceId());
                accountRepo.unlockById(txn.getDestinationId());
            }
        } catch (DataAccessException e) {
            throw new RuntimeException("Error unlocking the resources. This should never occur", e);
        }
    }

    private boolean containsSufficientFunds(Account account, Amount neededBalance) {
        return account.getBalance().getValue().compareTo(neededBalance.getValue()) >= 0;
    }

    private ExecuteTransactionResult executionFailed(Transaction txn, ExecuteTransactionResult.ExecutionStatus status, String message) {
        if (txn != null) {
            txn = txn.failed();
            try {
                transactionRepo.update(txn);
            } catch (DataAccessException e) {
                throw new RuntimeException("Could not update transaction");
            }
        }
        return new ExecuteTransactionResult(txn, status, message);
    }

    private ExecuteTransactionResult executionSucceeded(Transaction txn) {
        txn = txn.executed();
        try {
            transactionRepo.update(txn);
        } catch (DataAccessException e) {
            throw new RuntimeException("Could not update transaction");
        }
        return new ExecuteTransactionResult(txn, ExecuteTransactionResult.ExecutionStatus.SUCCESS);
    }
}
