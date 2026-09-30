package com.jasonvennin.compliance.rule.domain;

import com.jasonvennin.compliance.customer.domain.Customer;
import com.jasonvennin.compliance.transaction.domain.Transaction;

public interface ComplianceRule {
    String code();

    RuleResult evaluate(
            Transaction transaction,
            Customer customer
    );
}
