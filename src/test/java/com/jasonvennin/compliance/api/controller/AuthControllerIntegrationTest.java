package com.jasonvennin.compliance.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasonvennin.compliance.api.dto.LoginResponse;
import com.jasonvennin.compliance.application.port.CustomerRepository;
import com.jasonvennin.compliance.application.port.TransactionRepository;
import com.jasonvennin.compliance.application.port.UserRepository;
import com.jasonvennin.compliance.customer.domain.Customer;
import com.jasonvennin.compliance.customer.domain.RiskLevel;
import com.jasonvennin.compliance.infrastructure.persistence.mongo.customer.SpringDataCustomerRepository;
import com.jasonvennin.compliance.infrastructure.persistence.mongo.transaction.SpringDataTransactionRepository;
import com.jasonvennin.compliance.infrastructure.persistence.mongo.user.SpringDataUserRepository;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import com.jasonvennin.compliance.transaction.domain.TransactionType;
import com.jasonvennin.compliance.user.domain.Role;
import com.jasonvennin.compliance.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthControllerIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo =
            new MongoDBContainer("mongo:8.0");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpringDataUserRepository springDataUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private SpringDataCustomerRepository springDataCustomerRepository;

    @Autowired
    private SpringDataTransactionRepository springDataTransactionRepository;

    @BeforeEach
    void setUp() {
        springDataTransactionRepository.deleteAll();
        springDataCustomerRepository.deleteAll();
        springDataUserRepository.deleteAll();

        userRepository.save(
                new User(
                        null,
                        "admin",
                        passwordEncoder.encode("password"),
                        Set.of(Role.ADMIN, Role.USER),
                        true
                )
        );
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType("application/json")
                                .content("""
                                {
                                    "username": "admin",
                                    "password": "password"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldRejectInvalidPassword() throws Exception {

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType("application/json")
                                .content("""
                                {
                                    "username": "admin",
                                    "password": "wrong"
                                }
                                """)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUnknownUser() throws Exception {

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType("application/json")
                                .content("""
                                {
                                    "username": "unknown",
                                    "password": "password"
                                }
                                """)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGenerateJwt() throws Exception {

        MvcResult result = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType("application/json")
                                .content("""
                                {
                                    "username": "admin",
                                    "password": "password"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn();

        LoginResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LoginResponse.class
        );

        assertThat(response.token())
                .isNotBlank();
    }

    @Test
    void shouldLoginAndAccessProtectedComplianceEndpoint()
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

        MvcResult loginResult = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType("application/json")
                                .content("""
                                {
                                    "username": "admin",
                                    "password": "password"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                LoginResponse.class
        );

        String token = loginResponse.token();

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
                        .value("APPROVED"))
                .andExpect(jsonPath("$.riskScore")
                        .value(0))
                .andExpect(jsonPath("$.violations")
                        .isEmpty());
    }
}