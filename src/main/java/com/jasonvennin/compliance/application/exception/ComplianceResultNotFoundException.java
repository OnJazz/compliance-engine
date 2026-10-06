package com.jasonvennin.compliance.application.exception;

public class ComplianceResultNotFoundException extends RuntimeException {

    public ComplianceResultNotFoundException(String transactionId) {
        super(
                "Compliance result not found for transaction: "
                        + transactionId
        );
    }
}