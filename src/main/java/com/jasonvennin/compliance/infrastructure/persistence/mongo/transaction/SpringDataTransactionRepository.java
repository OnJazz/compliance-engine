package com.jasonvennin.compliance.infrastructure.persistence.mongo.transaction;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface SpringDataTransactionRepository
        extends MongoRepository<TransactionDocument, String> {
}
