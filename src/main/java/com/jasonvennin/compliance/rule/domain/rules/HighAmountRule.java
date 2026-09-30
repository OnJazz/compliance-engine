package com.jasonvennin.compliance.rule.domain.rules;

import com.jasonvennin.compliance.customer.domain.Customer;
import com.jasonvennin.compliance.rule.domain.ComplianceRule;
import com.jasonvennin.compliance.rule.domain.RuleResult;
import com.jasonvennin.compliance.rule.domain.RuleViolation;
import com.jasonvennin.compliance.rule.domain.Severity;
import com.jasonvennin.compliance.transaction.domain.Transaction;

import java.math.BigDecimal;

public class HighAmountRule implements ComplianceRule {

    private static final BigDecimal THRESHOLD = new BigDecimal("100000");

    @Override
    public String code() {
        return "HIGH_AMOUNT";
    }

    @Override
    public RuleResult evaluate(
            Transaction transaction,
            Customer customer
    ) {

        if (transaction.amount().compareTo(THRESHOLD) > 0) {
            return new RuleResult.Violation(
                    new RuleViolation(
                            code(),
                            Severity.HIGH,
                            "Transaction exceeds configured threshold"
                    )
            );
        }

        return new RuleResult.Success();
    }
}
