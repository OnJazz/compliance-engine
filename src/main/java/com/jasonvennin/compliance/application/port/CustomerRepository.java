package com.jasonvennin.compliance.application.port;

import com.jasonvennin.compliance.customer.domain.Customer;

import java.util.Optional;

public interface CustomerRepository {

    Optional<Customer> findById(String id);

    Customer save(Customer customer);
}
