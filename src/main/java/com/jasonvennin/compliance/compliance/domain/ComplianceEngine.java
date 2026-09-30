package com.jasonvennin.compliance.compliance.domain;

import com.jasonvennin.compliance.customer.domain.Customer;
import com.jasonvennin.compliance.rule.domain.ComplianceRule;
import com.jasonvennin.compliance.rule.domain.RuleResult;
import com.jasonvennin.compliance.rule.domain.RuleViolation;
import com.jasonvennin.compliance.rule.domain.Severity;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;

import java.util.List;

public class ComplianceEngine {

    private final List<ComplianceRule> rules;
    private final RiskScoreCalculator riskScoreCalculator;

    public ComplianceEngine(
            List<ComplianceRule> rules,
            RiskScoreCalculator riskScoreCalculator
    ) {
        this.rules = List.copyOf(rules);
        this.riskScoreCalculator = riskScoreCalculator;
    }

    public ComplianceResult evaluate(
            Transaction transaction,
            Customer customer
    ) {

        List<RuleViolation> violations = rules.stream()
                .map(rule -> rule.evaluate(transaction, customer))
                .filter(RuleResult.Violation.class::isInstance)
                .map(RuleResult.Violation.class::cast)
                .map(RuleResult.Violation::violation)
                .toList();

        RiskScore riskScore =
                riskScoreCalculator.calculate(violations);

        TransactionStatus status =
                determineStatus(violations, riskScore);

        return new ComplianceResult(
                transaction.id(),
                status,
                riskScore,
                violations
        );
    }

    // PRIVATE METHODS
    private TransactionStatus determineStatus(
            List<RuleViolation> violations,
            RiskScore riskScore
    ) {

        boolean criticalViolation = violations.stream()
                .anyMatch(v -> v.severity() == Severity.CRITICAL);

        if (criticalViolation) {
            return TransactionStatus.BLOCKED;
        }

        if (riskScore.value() >= 50) {
            return TransactionStatus.MANUAL_REVIEW;
        }

        return TransactionStatus.APPROVED;
    }
}
