package com.jasonvennin.compliance.customer.domain;

public record Customer(
        String id,
        String externalId,
        String firstName,
        String lastName,
        String country,
        RiskLevel riskLevel
) {
}
