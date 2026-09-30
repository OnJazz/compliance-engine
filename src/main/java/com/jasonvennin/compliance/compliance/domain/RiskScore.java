package com.jasonvennin.compliance.compliance.domain;

public record RiskScore(int value) {

    public RiskScore {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("Risk score must be between 0 and 100");
        }
    }
}
