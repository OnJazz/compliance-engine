package com.jasonvennin.compliance.rule.domain;

import com.jasonvennin.compliance.customer.domain.Customer;
import com.jasonvennin.compliance.customer.domain.RiskLevel;
import com.jasonvennin.compliance.rule.domain.rules.HighRiskCustomerRule;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import com.jasonvennin.compliance.transaction.domain.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class HighRiskCustomerRuleTest {

    private final HighRiskCustomerRule rule =
            new HighRiskCustomerRule();

    @Test
    void shouldReturnSuccessForLowRiskCustomer() {
        Customer customer = customer(RiskLevel.LOW);

        Transaction transaction = transaction("60000");

        RuleResult result = rule.evaluate(transaction, customer);

        assertInstanceOf(RuleResult.Success.class, result);
    }

    @Test
    void shouldReturnSuccessForMediumRiskCustomer() {
        Customer customer = customer(RiskLevel.MEDIUM);

        Transaction transaction = transaction("60000");

        RuleResult result = rule.evaluate(transaction, customer);

        assertInstanceOf(RuleResult.Success.class, result);
    }

    @Test
    void shouldReturnSuccessWhenHighRiskCustomerAmountIsBelowThreshold() {
        Customer customer = customer(RiskLevel.HIGH);

        Transaction transaction = transaction("50000");

        RuleResult result = rule.evaluate(transaction, customer);

        assertInstanceOf(RuleResult.Success.class, result);
    }

    @Test
    void shouldReturnViolationWhenHighRiskCustomerAmountIsAboveThreshold() {
        Customer customer = customer(RiskLevel.HIGH);

        Transaction transaction = transaction("50000.01");

        RuleResult result = rule.evaluate(transaction, customer);

        assertInstanceOf(RuleResult.Violation.class, result);

        var violation = ((RuleResult.Violation) result).violation();

        assertEquals("HIGH_RISK_CUSTOMER", violation.ruleCode());
        assertEquals(Severity.HIGH, violation.severity());
    }

    private Customer customer(RiskLevel riskLevel) {
        return new Customer(
                "customer-1",
                "CUST-001",
                "John",
                "Doe",
                "CH",
                riskLevel
        );
    }

    private Transaction transaction(String amount) {
        return new Transaction(
                "transaction-1",
                "TX-001",
                "customer-1",
                new BigDecimal(amount),
                "CHF",
                TransactionType.TRANSFER,
                "CH",
                "FR",
                Instant.now(),
                TransactionStatus.PENDING
        );
    }
}
