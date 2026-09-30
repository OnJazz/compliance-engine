package com.jasonvennin.compliance.compliance.domain;

import com.jasonvennin.compliance.customer.domain.Customer;
import com.jasonvennin.compliance.customer.domain.RiskLevel;
import com.jasonvennin.compliance.rule.domain.ComplianceRule;
import com.jasonvennin.compliance.rule.domain.RuleResult;
import com.jasonvennin.compliance.rule.domain.RuleViolation;
import com.jasonvennin.compliance.rule.domain.Severity;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import com.jasonvennin.compliance.transaction.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static com.mongodb.internal.connection.tlschannel.util.Util.assertTrue;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ComplianceEngineTest {

    private ComplianceRule rule;
    private RiskScoreCalculator riskScoreCalculator;
    private ComplianceEngine engine;

    private final Customer customer = new Customer(
            "customer-1",
            "CUST-001",
            "John",
            "Doe",
            "CH",
            RiskLevel.LOW
    );

    @BeforeEach
    void setUp() {
        rule = mock(ComplianceRule.class);
        riskScoreCalculator = mock(RiskScoreCalculator.class);

        engine = new ComplianceEngine(
                List.of(rule),
                riskScoreCalculator
        );
    }

    @Test
    void shouldApproveTransactionWhenThereAreNoViolations() {
        Transaction transaction = transaction();

        when(rule.evaluate(transaction, customer))
                .thenReturn(new RuleResult.Success());

        when(riskScoreCalculator.calculate(List.of()))
                .thenReturn(new RiskScore(0));

        ComplianceResult result =
                engine.evaluate(transaction, customer);

        assertEquals(
                TransactionStatus.APPROVED,
                result.status()
        );

        assertEquals(0, result.riskScore().value());
        assertTrue(result.violations().isEmpty());
    }

    @Test
    void shouldBlockTransactionWhenThereIsCriticalViolation() {
        Transaction transaction = transaction();

        RuleViolation violation = new RuleViolation(
                "RESTRICTED_COUNTRY",
                Severity.CRITICAL,
                "Beneficiary country is restricted"
        );

        when(rule.evaluate(transaction, customer))
                .thenReturn(new RuleResult.Violation(violation));

        when(riskScoreCalculator.calculate(List.of(violation)))
                .thenReturn(new RiskScore(100));

        ComplianceResult result =
                engine.evaluate(transaction, customer);

        assertEquals(
                TransactionStatus.BLOCKED,
                result.status()
        );

        assertEquals(100, result.riskScore().value());
        assertEquals(List.of(violation), result.violations());
    }

    @Test
    void shouldRequireManualReviewWhenRiskScoreIsAtLeastFifty() {
        Transaction transaction = transaction();

        RuleViolation violation = new RuleViolation(
                "HIGH_AMOUNT",
                Severity.HIGH,
                "Transaction exceeds configured threshold"
        );

        when(rule.evaluate(transaction, customer))
                .thenReturn(new RuleResult.Violation(violation));

        when(riskScoreCalculator.calculate(List.of(violation)))
                .thenReturn(new RiskScore(50));

        ComplianceResult result =
                engine.evaluate(transaction, customer);

        assertEquals(
                TransactionStatus.MANUAL_REVIEW,
                result.status()
        );

        assertEquals(50, result.riskScore().value());
    }

    @Test
    void shouldApproveTransactionWhenRiskScoreIsBelowFifty() {
        Transaction transaction = transaction();

        RuleViolation violation = new RuleViolation(
                "SOME_RULE",
                Severity.LOW,
                "Low severity violation"
        );

        when(rule.evaluate(transaction, customer))
                .thenReturn(new RuleResult.Violation(violation));

        when(riskScoreCalculator.calculate(List.of(violation)))
                .thenReturn(new RiskScore(10));

        ComplianceResult result =
                engine.evaluate(transaction, customer);

        assertEquals(
                TransactionStatus.APPROVED,
                result.status()
        );
    }

    @Test
    void shouldEvaluateAllRules() {
        ComplianceRule firstRule = mock(ComplianceRule.class);
        ComplianceRule secondRule = mock(ComplianceRule.class);

        ComplianceEngine engine = new ComplianceEngine(
                List.of(firstRule, secondRule),
                riskScoreCalculator
        );

        Transaction transaction = transaction();

        when(firstRule.evaluate(transaction, customer))
                .thenReturn(new RuleResult.Success());

        when(secondRule.evaluate(transaction, customer))
                .thenReturn(new RuleResult.Success());

        when(riskScoreCalculator.calculate(List.of()))
                .thenReturn(new RiskScore(0));

        engine.evaluate(transaction, customer);

        verify(firstRule).evaluate(transaction, customer);
        verify(secondRule).evaluate(transaction, customer);
    }

    @Test
    void shouldPassOnlyViolationsToRiskScoreCalculator() {
        ComplianceRule successfulRule = mock(ComplianceRule.class);
        ComplianceRule violatingRule = mock(ComplianceRule.class);

        ComplianceEngine engine = new ComplianceEngine(
                List.of(successfulRule, violatingRule),
                riskScoreCalculator
        );

        Transaction transaction = transaction();

        RuleViolation violation = new RuleViolation(
                "HIGH_AMOUNT",
                Severity.HIGH,
                "Transaction exceeds threshold"
        );

        when(successfulRule.evaluate(transaction, customer))
                .thenReturn(new RuleResult.Success());

        when(violatingRule.evaluate(transaction, customer))
                .thenReturn(new RuleResult.Violation(violation));

        when(riskScoreCalculator.calculate(List.of(violation)))
                .thenReturn(new RiskScore(50));

        engine.evaluate(transaction, customer);

        verify(riskScoreCalculator)
                .calculate(List.of(violation));
    }

    private Transaction transaction() {
        return new Transaction(
                "transaction-1",
                "TX-001",
                "customer-1",
                new BigDecimal("1000"),
                "CHF",
                TransactionType.TRANSFER,
                "CH",
                "FR",
                Instant.parse("2026-09-24T10:00:00Z"),
                TransactionStatus.PENDING
        );
    }
}
