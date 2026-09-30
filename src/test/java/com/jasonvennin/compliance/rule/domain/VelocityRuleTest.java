package com.jasonvennin.compliance.rule.domain;

import com.jasonvennin.compliance.customer.domain.Customer;
import com.jasonvennin.compliance.customer.domain.RiskLevel;
import com.jasonvennin.compliance.rule.domain.rules.VelocityRule;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionHistory;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import com.jasonvennin.compliance.transaction.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VelocityRuleTest {

    private TransactionHistory transactionHistory;
    private VelocityRule rule;

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
        transactionHistory = mock(TransactionHistory.class);
        rule = new VelocityRule(transactionHistory);
    }

    @Test
    void shouldReturnSuccessWhenThereAreNoPreviousTransactions() {
        Transaction transaction = transaction();

        when(transactionHistory.findPreviousTransactions(
                eq("customer-1"),
                any(),
                eq(transaction.createdAt())
        )).thenReturn(List.of());

        RuleResult result = rule.evaluate(transaction, customer);

        assertInstanceOf(RuleResult.Success.class, result);
    }

    @Test
    void shouldReturnSuccessWhenThereAreFourPreviousTransactions() {
        Transaction transaction = transaction();

        when(transactionHistory.findPreviousTransactions(
                eq("customer-1"),
                any(),
                eq(transaction.createdAt())
        )).thenReturn(transactions(4));

        RuleResult result = rule.evaluate(transaction, customer);

        assertInstanceOf(RuleResult.Success.class, result);
    }

    @Test
    void shouldReturnViolationWhenThereAreFivePreviousTransactions() {
        Transaction transaction = transaction();

        when(transactionHistory.findPreviousTransactions(
                eq("customer-1"),
                any(),
                eq(transaction.createdAt())
        )).thenReturn(transactions(5));

        RuleResult result = rule.evaluate(transaction, customer);

        assertInstanceOf(RuleResult.Violation.class, result);

        var violation = ((RuleResult.Violation) result).violation();

        assertEquals("TRANSACTION_VELOCITY", violation.ruleCode());
        assertEquals(Severity.HIGH, violation.severity());
    }

    @Test
    void shouldQueryHistoryForCurrentCustomer() {
        Transaction transaction = transaction();

        when(transactionHistory.findPreviousTransactions(
                anyString(),
                any(),
                any()
        )).thenReturn(List.of());

        rule.evaluate(transaction, customer);

        verify(transactionHistory).findPreviousTransactions(
                eq("customer-1"),
                any(),
                eq(transaction.createdAt())
        );
    }

    private List<Transaction> transactions(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> transaction())
                .toList();
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
