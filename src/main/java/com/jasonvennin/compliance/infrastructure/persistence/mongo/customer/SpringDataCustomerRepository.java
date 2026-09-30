package com.jasonvennin.compliance.infrastructure.persistence.mongo.customer;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringDataCustomerRepository
        extends MongoRepository<CustomerDocument, String> {
}
