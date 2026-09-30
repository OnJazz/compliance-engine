package com.jasonvennin.compliance.infrastructure.persistence.mongo.transaction;

import com.jasonvennin.compliance.application.port.TransactionRepository;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MongoTransactionRepository
        implements TransactionRepository {

    private final SpringDataTransactionRepository repository;

    public MongoTransactionRepository(
            SpringDataTransactionRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public Optional<Transaction> findById(String id) {
        return repository.findById(id)
                .map(TransactionDocument::toDomain);
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionDocument document =
                TransactionDocument.fromDomain(transaction);

        return repository.save(document).toDomain();
    }
}
