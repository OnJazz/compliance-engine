package com.jasonvennin.compliance.application.usecase;

import com.jasonvennin.compliance.application.port.ComplianceResultRepository;
import com.jasonvennin.compliance.application.port.CustomerRepository;
import com.jasonvennin.compliance.application.port.TransactionRepository;
import com.jasonvennin.compliance.compliance.domain.ComplianceResult;
import com.jasonvennin.compliance.rule.domain.Severity;
import com.jasonvennin.compliance.customer.domain.Customer;
import com.jasonvennin.compliance.customer.domain.RiskLevel;
import com.jasonvennin.compliance.infrastructure.persistence.mongo.compliance.SpringDataComplianceResultRepository;
import com.jasonvennin.compliance.infrastructure.persistence.mongo.customer.SpringDataCustomerRepository;
import com.jasonvennin.compliance.infrastructure.persistence.mongo.transaction.SpringDataTransactionRepository;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import com.jasonvennin.compliance.transaction.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class EvaluateTransactionUseCaseIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo =
            new MongoDBContainer("mongo:8.0");

    @Autowired
    private EvaluateTransactionUseCase evaluateTransactionUseCase;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ComplianceResultRepository complianceResultRepository;

    @Autowired
    private SpringDataCustomerRepository springDataCustomerRepository;

    @Autowired
    private SpringDataTransactionRepository springDataTransactionRepository;

    @Autowired
    private SpringDataComplianceResultRepository springDataComplianceResultRepository;

    @BeforeEach
    void cleanDatabase() {
        springDataComplianceResultRepository.deleteAll();
        springDataTransactionRepository.deleteAll();
        springDataCustomerRepository.deleteAll();
    }

    @Test
    void shouldApproveNormalTransaction() {

        Customer customer = createCustomer(
                "customer-1",
                RiskLevel.LOW
        );

        Transaction transaction = createTransaction(
                "transaction-1",
                customer.id(),
                new BigDecimal("1000"),
                "CH",
                Instant.parse("2026-09-24T10:00:00Z")
        );

        save(customer, transaction);

        ComplianceResult result =
                evaluateTransactionUseCase.evaluate(transaction.id());

        assertThat(result.transactionId())
                .isEqualTo(transaction.id());

        assertThat(result.status())
                .isEqualTo(TransactionStatus.APPROVED);

        assertThat(result.riskScore().value())
                .isZero();

        assertThat(result.violations())
                .isEmpty();

        ComplianceResult persistedResult =
                springDataComplianceResultRepository
                        .findById(transaction.id())
                        .orElseThrow()
                        .toDomain();

        assertThat(persistedResult)
                .isEqualTo(result);
    }

    @Test
    void shouldRequireManualReviewForHighAmount() {

        Customer customer = createCustomer(
                "customer-2",
                RiskLevel.LOW
        );

        Transaction transaction = createTransaction(
                "transaction-2",
                customer.id(),
                new BigDecimal("150000"),
                "CH",
                Instant.parse("2026-09-24T10:00:00Z")
        );

        save(customer, transaction);

        ComplianceResult result =
                evaluateTransactionUseCase.evaluate(transaction.id());

        assertThat(result.status())
                .isEqualTo(TransactionStatus.MANUAL_REVIEW);

        assertThat(result.riskScore().value())
                .isEqualTo(50);

        assertThat(result.violations())
                .hasSize(1);

        assertThat(result.violations().getFirst().ruleCode())
                .isEqualTo("HIGH_AMOUNT");

        assertThat(result.violations().getFirst().severity())
                .isEqualTo(Severity.HIGH);
    }

    @Test
    void shouldBlockTransactionForRestrictedCountry() {

        Customer customer = createCustomer(
                "customer-3",
                RiskLevel.LOW
        );

        Transaction transaction = createTransaction(
                "transaction-3",
                customer.id(),
                new BigDecimal("10000"),
                "IR",
                Instant.parse("2026-09-24T10:00:00Z")
        );

        save(customer, transaction);

        ComplianceResult result =
                evaluateTransactionUseCase.evaluate(transaction.id());

        assertThat(result.status())
                .isEqualTo(TransactionStatus.BLOCKED);

        assertThat(result.riskScore().value())
                .isEqualTo(100);

        assertThat(result.violations())
                .hasSize(1);

        assertThat(result.violations().getFirst().ruleCode())
                .isEqualTo("RESTRICTED_COUNTRY");

        assertThat(result.violations().getFirst().severity())
                .isEqualTo(Severity.CRITICAL);
    }

    @Test
    void shouldRequireManualReviewForHighRiskCustomer() {

        Customer customer = createCustomer(
                "customer-4",
                RiskLevel.HIGH
        );

        Transaction transaction = createTransaction(
                "transaction-4",
                customer.id(),
                new BigDecimal("75000"),
                "CH",
                Instant.parse("2026-09-24T10:00:00Z")
        );

        save(customer, transaction);

        ComplianceResult result =
                evaluateTransactionUseCase.evaluate(transaction.id());

        assertThat(result.status())
                .isEqualTo(TransactionStatus.MANUAL_REVIEW);

        assertThat(result.riskScore().value())
                .isEqualTo(50);

        assertThat(result.violations())
                .hasSize(1);

        assertThat(result.violations().getFirst().ruleCode())
                .isEqualTo("HIGH_RISK_CUSTOMER");

        assertThat(result.violations().getFirst().severity())
                .isEqualTo(Severity.HIGH);
    }

    @Test
    void shouldRequireManualReviewForHighTransactionVelocity() {

        String customerId = "customer-5";

        Customer customer = createCustomer(
                customerId,
                RiskLevel.LOW
        );

        saveCustomer(customer);

        Instant currentTime =
                Instant.parse("2026-09-24T10:10:00Z");

        for (int i = 1; i <= 5; i++) {
            Transaction previousTransaction =
                    createTransaction(
                            "previous-" + i,
                            customerId,
                            new BigDecimal("1000"),
                            "CH",
                            Instant.parse(
                                    "2026-09-24T10:0" + i + ":00Z"
                            )
                    );

            transactionRepository.save(previousTransaction);
        }

        Transaction currentTransaction =
                createTransaction(
                        "transaction-5",
                        customerId,
                        new BigDecimal("1000"),
                        "CH",
                        currentTime
                );

        transactionRepository.save(currentTransaction);

        ComplianceResult result =
                evaluateTransactionUseCase.evaluate(
                        currentTransaction.id()
                );

        assertThat(result.status())
                .isEqualTo(TransactionStatus.MANUAL_REVIEW);

        assertThat(result.riskScore().value())
                .isEqualTo(50);

        assertThat(result.violations())
                .hasSize(1);

        assertThat(result.violations().getFirst().ruleCode())
                .isEqualTo("TRANSACTION_VELOCITY");
    }

    private void save(
            Customer customer,
            Transaction transaction
    ) {
        saveCustomer(customer);
        transactionRepository.save(transaction);
    }

    private void saveCustomer(Customer customer) {
        customerRepository.save(customer);
    }

    private Customer createCustomer(
            String id,
            RiskLevel riskLevel
    ) {
        return new Customer(
                id,
                "EXT-" + id,
                "John",
                "Doe",
                "CH",
                riskLevel
        );
    }

    private Transaction createTransaction(
            String id,
            String customerId,
            BigDecimal amount,
            String beneficiaryCountry,
            Instant createdAt
    ) {
        return new Transaction(
                id,
                "EXT-" + id,
                customerId,
                amount,
                "CHF",
                TransactionType.TRANSFER,
                "CH",
                beneficiaryCountry,
                createdAt,
                TransactionStatus.PENDING
        );
    }
}
