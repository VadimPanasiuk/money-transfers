package com.exchange.money.repos;

import com.exchange.money.model.Transaction;

public class TransactionInsertException extends DataAccessException {

    public TransactionInsertException(Transaction transaction) {
        super("Could not insert transaction [" + transaction + "]");
    }
}
