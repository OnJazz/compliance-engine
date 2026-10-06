package com.jasonvennin.compliance.application.port;

import com.jasonvennin.compliance.transaction.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TransactionRepository {

    Optional<Transaction> findById(String id);

    Transaction save(Transaction transaction);

    Page<Transaction> findAll(
            String customerId,
            String status,
            String type,
            Pageable pageable
    );
}
