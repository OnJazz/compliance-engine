package com.jasonvennin.compliance.infrastructure.persistence.mongo.customer;

import com.jasonvennin.compliance.customer.domain.Customer;
import com.jasonvennin.compliance.customer.domain.RiskLevel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataMongoTest
@Testcontainers
@Import(MongoCustomerRepository.class)
class MongoCustomerRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo =
            new MongoDBContainer("mongo:8.0");

    @Autowired
    private MongoCustomerRepository customerRepository;

    @Autowired
    private SpringDataCustomerRepository springDataRepository;

    @Test
    void shouldSaveAndFindCustomer() {

        Customer customer = new Customer(
                "customer-1",
                "EXT-001",
                "Jason",
                "Vennin",
                "CH",
                RiskLevel.LOW
        );

        Customer saved = saveCustomer(customer);

        assertThat(saved)
                .isEqualTo(customer);

        Customer found = customerRepository
                .findById(customer.id())
                .orElseThrow();

        assertThat(found)
                .isEqualTo(customer);
    }

    @Test
    void shouldReturnEmptyWhenCustomerDoesNotExist() {

        assertThat(customerRepository.findById("unknown"))
                .isEmpty();
    }

    @Test
    void shouldPersistCustomerAsMongoDocument() {

        Customer customer = new Customer(
                "customer-2",
                "EXT-002",
                "John",
                "Doe",
                "FR",
                RiskLevel.HIGH
        );

        customerRepository.save(customer);

        CustomerDocument document = springDataRepository
                .findById(customer.id())
                .orElseThrow();

        assertThat(document.id())
                .isEqualTo(customer.id());

        assertThat(document.externalId())
                .isEqualTo(customer.externalId());

        assertThat(document.firstName())
                .isEqualTo(customer.firstName());

        assertThat(document.lastName())
                .isEqualTo(customer.lastName());

        assertThat(document.country())
                .isEqualTo(customer.country());

        assertThat(document.riskLevel())
                .isEqualTo(customer.riskLevel());
    }

    private Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }
}
