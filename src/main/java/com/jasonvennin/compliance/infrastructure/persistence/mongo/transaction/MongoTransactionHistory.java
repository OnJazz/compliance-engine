package com.jasonvennin.compliance.infrastructure.persistence.mongo.transaction;

import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionHistory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public class MongoTransactionHistory implements TransactionHistory {

    private final MongoTemplate mongoTemplate;

    public MongoTransactionHistory(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Transaction> findPreviousTransactions(
            String customerId,
            Instant from,
            Instant before
    ) {
        Query query = new Query(
                Criteria.where("customerId")
                        .is(customerId)
                        .and("createdAt")
                        .gte(from)
                        .lt(before)
        );

        return mongoTemplate
                .find(query, TransactionDocument.class)
                .stream()
                .map(TransactionDocument::toDomain)
                .toList();
    }
}
