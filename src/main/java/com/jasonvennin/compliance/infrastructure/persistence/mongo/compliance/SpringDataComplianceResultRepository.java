package com.jasonvennin.compliance.infrastructure.persistence.mongo.compliance;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringDataComplianceResultRepository
        extends MongoRepository<ComplianceResultDocument, String> {
}
