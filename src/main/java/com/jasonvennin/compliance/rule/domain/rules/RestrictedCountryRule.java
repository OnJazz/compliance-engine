package com.jasonvennin.compliance.rule.domain.rules;

import com.jasonvennin.compliance.customer.domain.Customer;
import com.jasonvennin.compliance.rule.domain.ComplianceRule;
import com.jasonvennin.compliance.rule.domain.RuleResult;
import com.jasonvennin.compliance.rule.domain.RuleViolation;
import com.jasonvennin.compliance.rule.domain.Severity;
import com.jasonvennin.compliance.transaction.domain.Transaction;

import java.util.Set;

public class RestrictedCountryRule implements ComplianceRule {

    private final Set<String> restrictedCountries;

    public RestrictedCountryRule(Set<String> restrictedCountries) {
        this.restrictedCountries = Set.copyOf(restrictedCountries);
    }

    @Override
    public String code() {
        return "RESTRICTED_COUNTRY";
    }

    @Override
    public RuleResult evaluate(
            Transaction transaction,
            Customer customer
    ) {
        if (restrictedCountries.contains(transaction.beneficiaryCountry())) {
            return new RuleResult.Violation(
                    new RuleViolation(
                            code(),
                            Severity.CRITICAL,
                            "Beneficiary country is restricted"
                    )
            );
        }

        return new RuleResult.Success();
    }
}
