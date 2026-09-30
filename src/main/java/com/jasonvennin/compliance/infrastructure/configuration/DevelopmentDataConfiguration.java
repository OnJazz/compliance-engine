package com.jasonvennin.compliance.infrastructure.configuration;

import com.jasonvennin.compliance.application.port.CustomerRepository;
import com.jasonvennin.compliance.application.port.TransactionRepository;
import com.jasonvennin.compliance.application.port.UserRepository;
import com.jasonvennin.compliance.customer.domain.Customer;
import com.jasonvennin.compliance.customer.domain.RiskLevel;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import com.jasonvennin.compliance.transaction.domain.TransactionType;
import com.jasonvennin.compliance.user.domain.Role;
import com.jasonvennin.compliance.user.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

@Configuration
@Profile("dev")
public class DevelopmentDataConfiguration {

    private static final Logger log =
            LoggerFactory.getLogger(DevelopmentDataConfiguration.class);

    @Bean
    CommandLineRunner createDevelopmentData(
            UserRepository userRepository,
            CustomerRepository customerRepository,
            TransactionRepository transactionRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            log.info("Starting development data initialization...");

            createAdminUser(
                    userRepository,
                    passwordEncoder
            );

            createCustomer(
                    customerRepository
            );

            createTransaction(
                    transactionRepository
            );

            log.info("Development data initialization completed.");
        };
    }

    private void createAdminUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        if (userRepository
                .findByUsername("admin")
                .isEmpty()) {

            User admin = new User(
                    null,
                    "admin",
                    passwordEncoder.encode("password"),
                    Set.of(Role.ADMIN, Role.USER),
                    true
            );

            userRepository.save(admin);

            log.info(
                    "Development user created: username=admin, roles=[ADMIN, USER]"
            );

        } else {
            log.info(
                    "Development user already exists: username=admin"
            );
        }
    }

    private void createCustomer(
            CustomerRepository customerRepository
    ) {
        if (customerRepository
                .findById("customer-1")
                .isEmpty()) {

            Customer customer = new Customer(
                    "customer-1",
                    "CUSTOMER-001",
                    "John",
                    "Doe",
                    "FR",
                    RiskLevel.LOW
            );

            customerRepository.save(customer);

            log.info(
                    "Development customer created: id=customer-1, riskLevel=LOW"
            );

        } else {
            log.info(
                    "Development customer already exists: id=customer-1"
            );
        }
    }

    private void createTransaction(
            TransactionRepository transactionRepository
    ) {
        if (transactionRepository
                .findById("transaction-1")
                .isEmpty()) {

            Transaction transaction = new Transaction(
                    "transaction-1",
                    "TRANSACTION-001",
                    "customer-1",
                    BigDecimal.valueOf(1_000),
                    "EUR",
                    TransactionType.TRANSFER,
                    "FR",
                    "FR",
                    Instant.now(),
                    TransactionStatus.PENDING
            );

            transactionRepository.save(transaction);

            log.info(
                    "Development transaction created: " +
                            "id=transaction-1, customerId=customer-1, amount=1000 EUR, " +
                            "type=TRANSFER, country=FR, beneficiaryCountry=FR, status=PENDING"
            );

        } else {
            log.info(
                    "Development transaction already exists: id=transaction-1"
            );
        }
    }
}