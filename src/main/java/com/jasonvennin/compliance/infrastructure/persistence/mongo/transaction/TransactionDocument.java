package com.jasonvennin.compliance.infrastructure.persistence.mongo.transaction;

import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import com.jasonvennin.compliance.transaction.domain.TransactionType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "transactions")
public record TransactionDocument(
        @Id
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

    public static TransactionDocument fromDomain(Transaction transaction) {
        return new TransactionDocument(
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

    public Transaction toDomain() {
        return new Transaction(
                id,
                externalId,
                customerId,
                amount,
                currency,
                type,
                country,
                beneficiaryCountry,
                createdAt,
                status
        );
    }
}
