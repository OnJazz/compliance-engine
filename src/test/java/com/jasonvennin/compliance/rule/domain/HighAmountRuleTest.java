package com.jasonvennin.compliance.rule.domain;

import com.jasonvennin.compliance.customer.domain.Customer;
import com.jasonvennin.compliance.rule.domain.rules.HighAmountRule;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class HighAmountRuleTest {

    private final HighAmountRule rule = new HighAmountRule();

    private final Customer customer = new Customer(
            "customer-1",
            "CUST-001",
            "John",
            "Doe",
            "CH",
            com.jasonvennin.compliance.customer.domain.RiskLevel.LOW
    );

    @Test
    void shouldReturnSuccessWhenAmountIsBelowThreshold() {
        Transaction transaction = transaction("99999.99");

        RuleResult result = rule.evaluate(transaction, customer);

        assertInstanceOf(RuleResult.Success.class, result);
    }

    @Test
    void shouldReturnSuccessWhenAmountEqualsThreshold() {
        Transaction transaction = transaction("100000");

        RuleResult result = rule.evaluate(transaction, customer);

        assertInstanceOf(RuleResult.Success.class, result);
    }

    @Test
    void shouldReturnViolationWhenAmountIsAboveThreshold() {
        Transaction transaction = transaction("100000.01");

        RuleResult result = rule.evaluate(transaction, customer);

        assertInstanceOf(RuleResult.Violation.class, result);

        var violation = ((RuleResult.Violation) result).violation();

        assertEquals("HIGH_AMOUNT", violation.ruleCode());
        assertEquals(
                com.jasonvennin.compliance.rule.domain.Severity.HIGH,
                violation.severity()
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
                com.jasonvennin.compliance.transaction.domain.TransactionStatus.PENDING
        );
    }
}
