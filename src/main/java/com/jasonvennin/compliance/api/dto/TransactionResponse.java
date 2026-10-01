package com.jasonvennin.compliance.api.dto;

import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import com.jasonvennin.compliance.transaction.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        String id,
        String externalId,
        String customerId,
        BigDecimal amount,
        String currency,
        TransactionType type,
        String country,
        String beneficiaryCountry,
        Instant createdAt,
        TransactionStatus status
) {

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.id(),
                transaction.externalId(),
                transaction.customerId(),
                transaction.amount(),
                transaction.currency(),
                transaction.type(),
                transaction.country(),
                transaction.beneficiaryCountry(),
                transaction.createdAt(),
                transaction.status()
        );
    }
}