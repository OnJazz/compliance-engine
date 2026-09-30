package com.jasonvennin.compliance.api.controller;

import com.jasonvennin.compliance.application.port.CustomerRepository;
import com.jasonvennin.compliance.application.port.TransactionRepository;
import com.jasonvennin.compliance.infrastructure.persistence.mongo.compliance.SpringDataComplianceResultRepository;
import com.jasonvennin.compliance.infrastructure.persistence.mongo.customer.SpringDataCustomerRepository;
import com.jasonvennin.compliance.infrastructure.persistence.mongo.transaction.SpringDataTransactionRepository;
import com.jasonvennin.compliance.infrastructure.security.JwtService;
import com.jasonvennin.compliance.customer.domain.Customer;
import com.jasonvennin.compliance.customer.domain.RiskLevel;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import com.jasonvennin.compliance.transaction.domain.TransactionType;
import com.jasonvennin.compliance.user.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ComplianceControllerIntegrationTest {

    @Container
    static MongoDBContainer mongo =
            new MongoDBContainer("mongo:8.0");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private SpringDataCustomerRepository springDataCustomerRepository;

    @Autowired
    private SpringDataTransactionRepository springDataTransactionRepository;

    @Autowired
    private SpringDataComplianceResultRepository
            springDataComplianceResultRepository;

    @Autowired
    private JwtService jwtService;

    @DynamicPropertySource
    static void configureMongo(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.mongodb.uri",
                mongo::getReplicaSetUrl
        );
    }

    @BeforeEach
    void setUp() {
        springDataComplianceResultRepository.deleteAll();
        springDataTransactionRepository.deleteAll();
        springDataCustomerRepository.deleteAll();
    }

    @Test
    void shouldReturnUnauthorizedWithoutJwt() throws Exception {

        mockMvc.perform(
                        post(
                                "/api/compliance/transactions/{transactionId}/evaluate",
                                "transaction-1"
                        )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWithInvalidJwt() throws Exception {

        mockMvc.perform(
                        post(
                                "/api/compliance/transactions/{transactionId}/evaluate",
                                "transaction-1"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer invalid-token"
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldEvaluateTransactionWithValidJwt() throws Exception {

        Customer customer = new Customer(
                "customer-1",
                "external-customer-1",
                "John",
                "Doe",
                "CH",
                RiskLevel.LOW
        );

        Transaction transaction = new Transaction(
                "transaction-1",
                "external-transaction-1",
                "customer-1",
                new BigDecimal("1000"),
                "CHF",
                TransactionType.TRANSFER,
                "CH",
                "CH",
                Instant.parse("2026-09-28T07:00:00Z"),
                TransactionStatus.PENDING
        );

        customerRepository.save(customer);
        transactionRepository.save(transaction);

        String token = jwtService.generateToken(
                "admin",
                Set.of(Role.ADMIN, Role.USER)
        );

        mockMvc.perform(
                        post(
                                "/api/compliance/transactions/{transactionId}/evaluate",
                                "transaction-1"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId")
                        .value("transaction-1"))
                .andExpect(jsonPath("$.status")
                        .value("APPROVED"))
                .andExpect(jsonPath("$.riskScore")
                        .value(0))
                .andExpect(jsonPath("$.violations")
                        .isEmpty());
    }

    @Test
    void shouldReturnTransactionNotFoundWithValidJwt() throws Exception {

        String token = jwtService.generateToken(
                "admin",
                Set.of(Role.ADMIN, Role.USER)
        );

        mockMvc.perform(
                        post(
                                "/api/compliance/transactions/{transactionId}/evaluate",
                                "unknown"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value("TRANSACTION_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Transaction not found: unknown"));
    }

    @Test
    void shouldReturnCustomerNotFoundWithValidJwt() throws Exception {

        Transaction transaction = new Transaction(
                "transaction-1",
                "external-transaction-1",
                "missing-customer",
                new BigDecimal("1000"),
                "CHF",
                TransactionType.TRANSFER,
                "CH",
                "CH",
                Instant.parse("2026-09-28T07:00:00Z"),
                TransactionStatus.PENDING
        );

        transactionRepository.save(transaction);

        String token = jwtService.generateToken(
                "admin",
                Set.of(Role.ADMIN, Role.USER)
        );

        mockMvc.perform(
                        post(
                                "/api/compliance/transactions/{transactionId}/evaluate",
                                "transaction-1"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value("CUSTOMER_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Customer not found: missing-customer"));
    }

    @Test
    void shouldBlockRestrictedCountryTransactionWithValidJwt()
            throws Exception {

        Customer customer = new Customer(
                "customer-1",
                "external-customer-1",
                "John",
                "Doe",
                "CH",
                RiskLevel.LOW
        );

        Transaction transaction = new Transaction(
                "transaction-1",
                "external-transaction-1",
                "customer-1",
                new BigDecimal("1000"),
                "CHF",
                TransactionType.TRANSFER,
                "CH",
                "IR",
                Instant.parse("2026-09-28T07:00:00Z"),
                TransactionStatus.PENDING
        );

        customerRepository.save(customer);
        transactionRepository.save(transaction);

        String token = jwtService.generateToken(
                "admin",
                Set.of(Role.ADMIN, Role.USER)
        );

        mockMvc.perform(
                        post(
                                "/api/compliance/transactions/{transactionId}/evaluate",
                                "transaction-1"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId")
                        .value("transaction-1"))
                .andExpect(jsonPath("$.status")
                        .value("BLOCKED"))
                .andExpect(jsonPath("$.riskScore")
                        .value(100))
                .andExpect(jsonPath("$.violations[0].ruleCode")
                        .value("RESTRICTED_COUNTRY"))
                .andExpect(jsonPath("$.violations[0].severity")
                        .value("CRITICAL"));
    }

    @Test
    void shouldRequireAuthenticationForComplianceEndpoint()
            throws Exception {

        Customer customer = new Customer(
                "customer-1",
                "external-customer-1",
                "John",
                "Doe",
                "CH",
                RiskLevel.LOW
        );

        Transaction transaction = new Transaction(
                "transaction-1",
                "external-transaction-1",
                "customer-1",
                new BigDecimal("1000"),
                "CHF",
                TransactionType.TRANSFER,
                "CH",
                "CH",
                Instant.parse("2026-09-28T07:00:00Z"),
                TransactionStatus.PENDING
        );

        customerRepository.save(customer);
        transactionRepository.save(transaction);

        mockMvc.perform(
                        post(
                                "/api/compliance/transactions/{transactionId}/evaluate",
                                "transaction-1"
                        )
                )
                .andExpect(status().isUnauthorized());
    }
}
