package com.jasonvennin.compliance.api.dto;

import com.jasonvennin.compliance.compliance.domain.ComplianceResult;
import com.jasonvennin.compliance.rule.domain.RuleViolation;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ComplianceResultResponse(
        @Schema(example = "transaction-1")
        String transactionId,

        @Schema(
                description = "Final transaction status",
                example = "MANUAL_REVIEW"
        )
        TransactionStatus status,

        @Schema(
                description = "Calculated risk score between 0 and 100",
                example = "50"
        )
        int riskScore,

        @Schema(
                description = "List of rule violations",
                example = "[{\"ruleId\": \"rule-1\", \"description\": \"Rule 1 description\"}]"
        )
        List<RuleViolation> violations
) {

    public static ComplianceResultResponse fromDomain(
            ComplianceResult result
    ) {
        return new ComplianceResultResponse(
                result.transactionId(),
                result.status(),
                result.riskScore().value(),
                result.violations()
        );
    }
}
