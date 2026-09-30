package com.jasonvennin.compliance.infrastructure.persistence.mongo.compliance;

import com.jasonvennin.compliance.compliance.domain.ComplianceResult;
import com.jasonvennin.compliance.compliance.domain.RiskScore;
import com.jasonvennin.compliance.rule.domain.RuleViolation;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "compliance_results")
public record ComplianceResultDocument(
        @Id
        String transactionId,

        TransactionStatus status,

        int riskScore,

        List<RuleViolation> violations
) {

    public static ComplianceResultDocument fromDomain(
            ComplianceResult result
    ) {
        return new ComplianceResultDocument(
                result.transactionId(),
                result.status(),
                result.riskScore().value(),
                result.violations()
        );
    }

    public ComplianceResult toDomain() {
        return new ComplianceResult(
                transactionId,
                status,
                new RiskScore(riskScore),
                violations
        );
    }
}
