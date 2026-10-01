package com.jasonvennin.compliance.infrastructure.persistence.mongo.compliance;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataComplianceResultRepository
        extends MongoRepository<ComplianceResultDocument, String> {

    Optional<ComplianceResultDocument> findByTransactionId(
            String transactionId
    );
}