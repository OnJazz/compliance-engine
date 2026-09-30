package com.jasonvennin.compliance.compliance.domain;

import com.jasonvennin.compliance.rule.domain.RuleViolation;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;

import java.util.List;

public record ComplianceResult(
        String transactionId,
        TransactionStatus status,
        RiskScore riskScore,
        List<RuleViolation> violations
) {
}
