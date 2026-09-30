package com.jasonvennin.compliance.transaction.domain;

import java.time.Instant;
import java.util.List;

public interface TransactionHistory {
    List<Transaction> findPreviousTransactions(
            String customerId,
            Instant from,
            Instant before
    );
}
