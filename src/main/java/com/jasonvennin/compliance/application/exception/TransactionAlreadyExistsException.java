package com.jasonvennin.compliance.application.exception;

public class TransactionAlreadyExistsException extends RuntimeException {

    public TransactionAlreadyExistsException(String transactionId) {
        super("Transaction already exists: " + transactionId);
    }
}