package com.exchange.money.services;

import com.exchange.money.model.Amount;

public interface TransactionService {

    CreateTransactionResult createTransaction(long srcAccountId, long dstAccountId, Amount amount)
            throws TransactionServiceException;

    ExecuteTransactionResult executeTransaction(long transactionId) throws TransactionServiceException;

}
