package com.jasonvennin.compliance.infrastructure.persistence.mongo.customer;

import com.jasonvennin.compliance.application.port.CustomerRepository;
import com.jasonvennin.compliance.customer.domain.Customer;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MongoCustomerRepository implements CustomerRepository {

    private final SpringDataCustomerRepository repository;

    public MongoCustomerRepository(SpringDataCustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Customer> findById(String id) {
        return repository.findById(id)
                .map(CustomerDocument::toDomain);
    }

    @Override
    public Customer save(Customer customer) {
        CustomerDocument document =
                CustomerDocument.fromDomain(customer);

        return repository.save(document)
                .toDomain();
    }
}
