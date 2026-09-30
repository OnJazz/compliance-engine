package com.jasonvennin.compliance.transaction.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Transaction(
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
}
