package com.exchange.money.repos;

public class CouldNotLockResourceException extends DataAccessException {
    public CouldNotLockResourceException(String message) {
        super(message);
    }
}
