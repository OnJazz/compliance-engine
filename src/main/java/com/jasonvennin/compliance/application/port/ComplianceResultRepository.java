package com.jasonvennin.compliance.application.port;

import com.jasonvennin.compliance.compliance.domain.ComplianceResult;

import java.util.Optional;

public interface ComplianceResultRepository {

    ComplianceResult save(ComplianceResult result);

    Optional<ComplianceResult> findByTransactionId(String transactionId);
}
