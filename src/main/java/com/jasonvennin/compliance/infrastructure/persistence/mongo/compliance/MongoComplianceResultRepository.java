package com.jasonvennin.compliance.infrastructure.persistence.mongo.compliance;

import com.jasonvennin.compliance.application.port.ComplianceResultRepository;
import com.jasonvennin.compliance.compliance.domain.ComplianceResult;
import org.springframework.stereotype.Repository;

@Repository
public class MongoComplianceResultRepository
        implements ComplianceResultRepository {

    private final SpringDataComplianceResultRepository repository;

    public MongoComplianceResultRepository(
            SpringDataComplianceResultRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public ComplianceResult save(ComplianceResult result) {
        ComplianceResultDocument document =
                ComplianceResultDocument.fromDomain(result);

        return repository.save(document).toDomain();
    }
}
