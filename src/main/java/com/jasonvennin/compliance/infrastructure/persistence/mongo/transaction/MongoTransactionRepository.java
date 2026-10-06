package com.jasonvennin.compliance.infrastructure.persistence.mongo.transaction;

import com.jasonvennin.compliance.application.port.TransactionRepository;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import com.jasonvennin.compliance.transaction.domain.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MongoTransactionRepository
        implements TransactionRepository {

    private final SpringDataTransactionRepository repository;
    private final MongoTemplate mongoTemplate;

    public MongoTransactionRepository(
            SpringDataTransactionRepository repository,
            MongoTemplate mongoTemplate
    ) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
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

    @Override
    public Page<Transaction> findAll(
            String customerId,
            TransactionStatus status,
            TransactionType type,
            Pageable pageable
    ) {
        List<Criteria> criteria = new ArrayList<>();

        if (customerId != null && !customerId.isBlank()) {
            criteria.add(
                    Criteria.where("customerId")
                            .is(customerId)
            );
        }

        if (status != null) {
            criteria.add(
                    Criteria.where("status")
                            .is(status)
            );
        }

        if (type != null) {
            criteria.add(
                    Criteria.where("type")
                            .is(type)
            );
        }

        Query query = new Query();

        if (!criteria.isEmpty()) {
            query.addCriteria(
                    new Criteria().andOperator(
                            criteria.toArray(new Criteria[0])
                    )
            );
        }

        long total = mongoTemplate.count(
                query,
                TransactionDocument.class
        );

        query.with(pageable);

        List<Transaction> transactions = mongoTemplate
                .find(query, TransactionDocument.class)
                .stream()
                .map(TransactionDocument::toDomain)
                .toList();

        return new PageImpl<>(
                transactions,
                pageable,
                total
        );
    }
}