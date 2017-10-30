package com.exchange.money.controllers;

import com.exchange.money.model.Account;
import com.exchange.money.model.Amount;
import com.exchange.money.repos.AccountNotFoundException;
import com.exchange.money.repos.AccountRepo;
import com.exchange.money.repos.DataAccessException;
import com.exchange.money.utils.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
public class AccountController {

    private final AccountRepo accountRepo;

    @Autowired
    public AccountController(AccountRepo accountRepo) {
        Assert.checkNotNull(accountRepo, "acountRepo cannot be null");
        this.accountRepo = accountRepo;
    }

    @RequestMapping(value = "/accounts", method = RequestMethod.GET)
    public List<Account> getAllAccounts() throws DataAccessException {
        return accountRepo.getAll();
    }

    @RequestMapping(value = "/accounts/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllAccounts(@PathVariable("id") long id) {
        try {
            Account account = accountRepo.getById(id);
            return ResponseEntity.ok(account);
        } catch (AccountNotFoundException ae) {
            return ResponseEntity.notFound().build();
        } catch (DataAccessException e) {
            return ResponseEntity.status(500).body("An unexpected error occurred. Could not access the account");
        }
    }

    @RequestMapping(value = "/accounts",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Account> getAllAccounts(@RequestBody Account account, UriComponentsBuilder b)
            throws DataAccessException {
        //account validation
        account = new Account(new Amount(account.getBalance().getValue(), account.getBalance().getCurrency()));
        Account newAccount = accountRepo.insert(account);
        return ResponseEntity
                .created(b.path("/accounts/{id}").buildAndExpand(newAccount.getId()).toUri())
                .body(newAccount);
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageConversionException.class})
    public ResponseEntity<ExceptionResponse> illegalArgumentExceptionHandler(RuntimeException re) {
        return ResponseEntity.badRequest().body(new ExceptionResponse(re));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> unexpectedExceptionHandler(Exception e) {
        return ResponseEntity.status(500).body(new ExceptionResponse(e));
    }
}
