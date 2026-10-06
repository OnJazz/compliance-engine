package com.jasonvennin.compliance.application.usecase;

import com.jasonvennin.compliance.application.exception.ComplianceResultNotFoundException;
import com.jasonvennin.compliance.application.port.ComplianceResultRepository;
import com.jasonvennin.compliance.compliance.domain.ComplianceResult;
import org.springframework.stereotype.Service;

@Service
public class GetComplianceResultUseCase {

    private final ComplianceResultRepository complianceResultRepository;

    public GetComplianceResultUseCase(
            ComplianceResultRepository complianceResultRepository
    ) {
        this.complianceResultRepository = complianceResultRepository;
    }

    public ComplianceResult execute(String transactionId) {
        return complianceResultRepository
                .findByTransactionId(transactionId)
                .orElseThrow(() ->
                        new ComplianceResultNotFoundException(transactionId)
                );
    }
}