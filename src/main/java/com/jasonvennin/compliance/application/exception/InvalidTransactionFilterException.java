package com.jasonvennin.compliance.application.exception;

public class InvalidTransactionFilterException extends RuntimeException {

    public InvalidTransactionFilterException(
            String filter,
            String value
    ) {
        super(
                "Invalid value '%s' for filter '%s'"
                        .formatted(value, filter)
        );
    }
}