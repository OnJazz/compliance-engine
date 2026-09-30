package com.jasonvennin.compliance.rule.domain;

public sealed interface RuleResult
        permits RuleResult.Success, RuleResult.Violation {

    record Success() implements RuleResult {
    }

    record Violation(RuleViolation violation) implements RuleResult {
    }
}
