package com.jasonvennin.compliance.rule.domain;

import com.jasonvennin.compliance.customer.domain.Customer;
import com.jasonvennin.compliance.customer.domain.RiskLevel;
import com.jasonvennin.compliance.rule.domain.rules.RestrictedCountryRule;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import com.jasonvennin.compliance.transaction.domain.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RestrictedCountryRuleTest {

    private final RestrictedCountryRule rule =
            new RestrictedCountryRule(Set.of("IR", "KP", "SY"));

    private final Customer customer = new Customer(
            "customer-1",
            "CUST-001",
            "John",
            "Doe",
            "CH",
            RiskLevel.LOW
    );

    @Test
    void shouldReturnSuccessForAllowedCountry() {
        Transaction transaction = transaction("FR");

        RuleResult result = rule.evaluate(transaction, customer);

        assertInstanceOf(RuleResult.Success.class, result);
    }

    @Test
    void shouldReturnViolationForRestrictedCountry() {
        Transaction transaction = transaction("IR");

        RuleResult result = rule.evaluate(transaction, customer);

        assertInstanceOf(RuleResult.Violation.class, result);

        var violation = ((RuleResult.Violation) result).violation();

        assertEquals("RESTRICTED_COUNTRY", violation.ruleCode());
        assertEquals(Severity.CRITICAL, violation.severity());
        assertEquals(
                "Beneficiary country is restricted",
                violation.message()
        );
    }

    private Transaction transaction(String beneficiaryCountry) {
        return new Transaction(
                "transaction-1",
                "TX-001",
                "customer-1",
                new BigDecimal("1000"),
                "CHF",
                TransactionType.TRANSFER,
                "CH",
                beneficiaryCountry,
                Instant.now(),
                TransactionStatus.PENDING
        );
    }
}
