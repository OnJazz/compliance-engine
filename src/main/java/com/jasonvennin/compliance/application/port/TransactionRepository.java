package com.jasonvennin.compliance.application.port;

import com.jasonvennin.compliance.transaction.domain.Transaction;

import java.util.Optional;

public interface TransactionRepository {

    Optional<Transaction> findById(String id);

    Transaction save(Transaction transaction);
}
