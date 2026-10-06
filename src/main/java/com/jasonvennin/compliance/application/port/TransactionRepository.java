package com.jasonvennin.compliance.application.port;

import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import com.jasonvennin.compliance.transaction.domain.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TransactionRepository {

    Optional<Transaction> findById(String id);

    Transaction save(Transaction transaction);

    Page<Transaction> findAll(
            String customerId,
            TransactionStatus status,
            TransactionType type,
            Pageable pageable
    );
}