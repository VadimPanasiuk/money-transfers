package com.exchange.money.repos;

import com.exchange.money.model.Transaction;

import java.util.List;

public interface TransactionRepo {

    Transaction insert(Transaction transaction) throws DataAccessException;

    Transaction update(Transaction transaction) throws DataAccessException;

    Transaction getById(long transactionId) throws DataAccessException;

    void lockById(long transactionId) throws DataAccessException;

    void unlockById(long transactionId) throws DataAccessException;

    List<Transaction> getAll() throws DataAccessException;

    void deleteAll() throws DataAccessException;
}
