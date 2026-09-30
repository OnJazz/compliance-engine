package com.jasonvennin.compliance.rule.domain.rules;

import com.jasonvennin.compliance.customer.domain.Customer;
import com.jasonvennin.compliance.customer.domain.RiskLevel;
import com.jasonvennin.compliance.rule.domain.ComplianceRule;
import com.jasonvennin.compliance.rule.domain.RuleResult;
import com.jasonvennin.compliance.rule.domain.RuleViolation;
import com.jasonvennin.compliance.rule.domain.Severity;
import com.jasonvennin.compliance.transaction.domain.Transaction;

import java.math.BigDecimal;

public class HighRiskCustomerRule implements ComplianceRule {

    private static final BigDecimal THRESHOLD = new BigDecimal("50000");

    @Override
    public String code() {
        return "HIGH_RISK_CUSTOMER";
    }

    @Override
    public RuleResult evaluate(
            Transaction transaction,
            Customer customer
    ) {
        boolean highRiskCustomer =
                customer.riskLevel() == RiskLevel.HIGH;

        boolean amountAboveThreshold =
                transaction.amount().compareTo(THRESHOLD) > 0;

        if (highRiskCustomer && amountAboveThreshold) {
            return new RuleResult.Violation(
                    new RuleViolation(
                            code(),
                            Severity.HIGH,
                            "High-risk customer with transaction above threshold"
                    )
            );
        }

        return new RuleResult.Success();
    }
}
