package com.exchange.money.repos;

import com.exchange.money.model.Account;

import java.util.List;

public interface AccountRepo {

    Account insert(Account account) throws DataAccessException;

    Account getById(long accountId) throws DataAccessException;

    Account update(Account account) throws DataAccessException;

    void lockById(long accountId) throws DataAccessException;

    void unlockById(long accountId) throws DataAccessException;

    List<Account> getAll() throws DataAccessException;

    void deleteAll() throws DataAccessException;
}
