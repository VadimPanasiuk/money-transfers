package com.exchange.money.controllers;

import com.exchange.money.model.Amount;
import com.exchange.money.model.Transaction;
import com.exchange.money.repos.DataAccessException;
import com.exchange.money.repos.TransactionNotFoundException;
import com.exchange.money.repos.TransactionRepo;
import com.exchange.money.services.CreateTransactionResult;
import com.exchange.money.services.ExecuteTransactionResult;
import com.exchange.money.services.TransactionService;
import com.exchange.money.utils.Assert;
import com.exchange.money.services.TransactionServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
public class TransactionController {

    private final TransactionRepo transactionRepo;
    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionRepo transactionRepo, TransactionService transactionService) {
        Assert.checkNotNull(transactionRepo, "transactionRepo cannot be null");
        Assert.checkNotNull(transactionService, "transactionService cannot be null");
        this.transactionRepo = transactionRepo;
        this.transactionService = transactionService;
    }

    @RequestMapping(value = "/transactions",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Transaction> getAll() throws DataAccessException {
        return transactionRepo.getAll();
    }

    @RequestMapping(value = "/transactions/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getById(@PathVariable("id") long id) throws DataAccessException {
        try {
            return ResponseEntity.ok(transactionRepo.getById(id));
        } catch (TransactionNotFoundException tnfe) {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/transactions",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createTransaction(@RequestBody Transaction transaction,
                                               UriComponentsBuilder builder)
            throws TransactionServiceException {

        // validation
        Assert.checkNotNull(transaction, "transaction cannot be null");
        Assert.checkNotNull(transaction.getAmount(), "transaction needs to have an amount property");
        transaction = new Transaction(transaction.getSourceId(), transaction.getDestinationId(),
                new Amount(transaction.getAmount().getValue(), transaction.getAmount().getCurrency()));

        CreateTransactionResult result = transactionService
                .createTransaction(transaction.getSourceId(), transaction.getDestinationId(), transaction.getAmount());

        if (result.getStatus().equals(CreateTransactionResult.CreationStatus.SUCCESS)) {
            return ResponseEntity
                    .created(builder.path("/transactions/{id}")
                            .buildAndExpand(result.getTransaction().getId()).toUri())
                    .body(result);
        } else {
            return ResponseEntity
                    .status(extractStatus(result.getStatus().getStatusCode()))
                    .body(result);
        }
    }

    @RequestMapping(value = "/transactions/{id}/executed",
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> executeTransaction(@PathVariable("id") long transactionId)
            throws TransactionServiceException {
        ExecuteTransactionResult result = transactionService.executeTransaction(transactionId);
        return ResponseEntity.status(extractStatus(result.getStatus().getStatusCode())).body(result);
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageConversionException.class})
    public ResponseEntity<ExceptionResponse> illegalArgumentExceptionHandler(RuntimeException re) {
        return ResponseEntity.badRequest().body(new ExceptionResponse(re));
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ExceptionResponse> unexpectedExceptionHandler(Exception e) {
        return ResponseEntity.status(500).body(new ExceptionResponse(e));
    }

    private int extractStatus(int internalStatusCode) {
        return internalStatusCode / 10;
    }
}
