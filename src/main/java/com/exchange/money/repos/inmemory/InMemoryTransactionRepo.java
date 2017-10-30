package com.exchange.money.repos.inmemory;

import com.exchange.money.model.Transaction;
import com.exchange.money.repos.CouldNotLockResourceException;
import com.exchange.money.repos.DataAccessException;
import com.exchange.money.repos.TransactionNotFoundException;
import com.exchange.money.repos.TransactionRepo;
import com.exchange.money.utils.Assert;
import com.google.common.annotations.VisibleForTesting;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

class InMemoryTransactionRepo implements TransactionRepo {

    public static final long AWAIT_LOCK_MILLISECONDS = 5000L;
    AtomicLong maxTransactionId = new AtomicLong(0);
    Map<Long, Transaction> transactionsMap = new ConcurrentHashMap<>();
    Map<Long, Semaphore> transactionLocks = new ConcurrentHashMap<>();

    @Override
    public Transaction insert(Transaction transaction) {
        long id = maxTransactionId.incrementAndGet();
        Transaction toInsert = new Transaction(id, transaction.getSourceId(),
                transaction.getDestinationId(), transaction.getAmount(),
                Transaction.TransactionStatus.PENDING);
        transactionsMap.put(toInsert.getId(), toInsert);
        transactionLocks.put(toInsert.getId(), new Semaphore(1));
        return toInsert;
    }

    @Override
    public Transaction update(Transaction transaction) {
        Assert.checkIsTrue(transaction.getId() > 0, "Cannot update non persisted transaction");
        transactionsMap.put(transaction.getId(), transaction);
        return transaction;
    }

    @Override
    public Transaction getById(long transactionId) throws DataAccessException {
        Transaction transaction = transactionsMap.get(transactionId);
        if (transaction == null) {
            throw new TransactionNotFoundException(transactionId);
        }
        return transaction;
    }

    @Override
    public void lockById(long transactionId) throws DataAccessException {
        lockById(transactionId, AWAIT_LOCK_MILLISECONDS);
    }

    @Override
    public void unlockById(long transactionId) throws DataAccessException {
        Semaphore semaphore = transactionLocks.get(transactionId);
        if (semaphore != null && semaphore.availablePermits() == 0) {
            semaphore.release();
        }
    }

    @Override
    public List<Transaction> getAll() throws DataAccessException {
        return transactionsMap.values().stream().collect(Collectors.toList());
    }

    @Override
    public void deleteAll() throws DataAccessException {
        transactionLocks = new ConcurrentHashMap<>();
        transactionsMap = new ConcurrentHashMap<>();
        maxTransactionId.set(0);
    }

    @VisibleForTesting
    void lockById(long transactionId, long milliseconds) throws DataAccessException {
        Semaphore semaphore = transactionLocks.get(transactionId);
        if (semaphore == null) {
            throw new TransactionNotFoundException(transactionId);
        } else {
            try {
                if (!semaphore.tryAcquire(milliseconds, TimeUnit.MILLISECONDS)) {
                    throw new CouldNotLockResourceException("Could not lock transaction with id " + transactionId);
                }
            } catch (InterruptedException e) {
                throw new CouldNotLockResourceException("Could not lock transaction with id " + transactionId);
            }
        }
    }

    @VisibleForTesting
    int getPermitsForLock(long transactionId) {
        return transactionLocks.get(transactionId).availablePermits();
    }
}
