package com.jasonvennin.compliance.compliance.domain;

import com.jasonvennin.compliance.rule.domain.RuleViolation;

import java.util.List;

public class RiskScoreCalculator {

    public RiskScore calculate(List<RuleViolation> violations) {

        int score = violations.stream()
                .mapToInt(this::scoreFor)
                .sum();

        return new RiskScore(Math.min(score, 100));
    }

    private int scoreFor(RuleViolation violation) {
        return switch (violation.severity()) {
            case LOW -> 10;
            case MEDIUM -> 25;
            case HIGH -> 50;
            case CRITICAL -> 100;
        };
    }
}
