package com.jasonvennin.compliance.rule.domain.rules;

import com.jasonvennin.compliance.customer.domain.Customer;
import com.jasonvennin.compliance.rule.domain.ComplianceRule;
import com.jasonvennin.compliance.rule.domain.RuleResult;
import com.jasonvennin.compliance.rule.domain.RuleViolation;
import com.jasonvennin.compliance.rule.domain.Severity;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionHistory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class VelocityRule implements ComplianceRule {

    private static final int MAX_TRANSACTIONS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(10);

    private final TransactionHistory transactionHistory;

    public VelocityRule(TransactionHistory transactionHistory) {
        this.transactionHistory = transactionHistory;
    }

    @Override
    public String code() {
        return "TRANSACTION_VELOCITY";
    }

    @Override
    public RuleResult evaluate(
            Transaction transaction,
            Customer customer
    ) {
        Instant from = transaction.createdAt().minus(WINDOW);

        List<Transaction> previousTransactions =
                transactionHistory.findPreviousTransactions(
                        customer.id(),
                        from,
                        transaction.createdAt()
                );

        if (previousTransactions.size() >= MAX_TRANSACTIONS) {
            return new RuleResult.Violation(
                    new RuleViolation(
                            code(),
                            Severity.HIGH,
                            "Customer exceeded transaction velocity limit"
                    )
            );
        }

        return new RuleResult.Success();
    }
}
