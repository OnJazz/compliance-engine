package com.jasonvennin.compliance.rule.domain;

public record RuleViolation(
        String ruleCode,
        Severity severity,
        String message
) {
}
