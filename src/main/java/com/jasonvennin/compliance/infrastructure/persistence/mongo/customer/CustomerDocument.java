package com.jasonvennin.compliance.infrastructure.persistence.mongo.customer;

import com.jasonvennin.compliance.customer.domain.Customer;
import com.jasonvennin.compliance.customer.domain.RiskLevel;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "customers")
public record CustomerDocument(
        @Id
        String id,

        String externalId,

        String firstName,

        String lastName,

        String country,

        RiskLevel riskLevel
) {

    public static CustomerDocument fromDomain(Customer customer) {
        return new CustomerDocument(
                customer.id(),
                customer.externalId(),
                customer.firstName(),
                customer.lastName(),
                customer.country(),
                customer.riskLevel()
        );
    }

    public Customer toDomain() {
        return new Customer(
                id,
                externalId,
                firstName,
                lastName,
                country,
                riskLevel
        );
    }
}
