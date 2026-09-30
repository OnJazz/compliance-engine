package com.jasonvennin.compliance.infrastructure.persistence.mongo.compliance;

import com.jasonvennin.compliance.compliance.domain.ComplianceResult;
import com.jasonvennin.compliance.compliance.domain.RiskScore;
import com.jasonvennin.compliance.infrastructure.persistence.mongo.customer.MongoCustomerRepository;
import com.jasonvennin.compliance.rule.domain.RuleViolation;
import com.jasonvennin.compliance.rule.domain.Severity;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers
@Import(MongoComplianceResultRepository.class)
class MongoComplianceResultRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo =
            new MongoDBContainer("mongo:8.0");

    @Autowired
    private MongoComplianceResultRepository complianceResultRepository;

    @Autowired
    private SpringDataComplianceResultRepository springDataRepository;

    @Test
    void shouldSaveAndFindComplianceResult() {

        ComplianceResult result = createComplianceResult();

        ComplianceResult saved =
                complianceResultRepository.save(result);

        assertThat(saved)
                .isEqualTo(result);

        ComplianceResultDocument document =
                springDataRepository
                        .findById(result.transactionId())
                        .orElseThrow();

        ComplianceResult reconstructed =
                document.toDomain();

        assertThat(reconstructed)
                .isEqualTo(result);
    }

    @Test
    void shouldPersistRiskScoreAsInteger() {

        ComplianceResult result =
                new ComplianceResult(
                        "transaction-2",
                        TransactionStatus.MANUAL_REVIEW,
                        new RiskScore(75),
                        List.of()
                );

        complianceResultRepository.save(result);

        ComplianceResultDocument document =
                springDataRepository
                        .findById(result.transactionId())
                        .orElseThrow();

        assertThat(document.riskScore())
                .isEqualTo(75);
    }

    @Test
    void shouldPersistViolations() {

        RuleViolation violation =
                new RuleViolation(
                        "HIGH_AMOUNT",
                        Severity.HIGH,
                        "Transaction exceeds configured threshold"
                );

        ComplianceResult result =
                new ComplianceResult(
                        "transaction-3",
                        TransactionStatus.MANUAL_REVIEW,
                        new RiskScore(50),
                        List.of(violation)
                );

        complianceResultRepository.save(result);

        ComplianceResultDocument document =
                springDataRepository
                        .findById(result.transactionId())
                        .orElseThrow();

        assertThat(document.violations())
                .containsExactly(violation);
    }

    @Test
    void shouldReplaceExistingComplianceResult() {

        ComplianceResult first =
                new ComplianceResult(
                        "transaction-4",
                        TransactionStatus.APPROVED,
                        new RiskScore(0),
                        List.of()
                );

        complianceResultRepository.save(first);

        ComplianceResult second =
                new ComplianceResult(
                        "transaction-4",
                        TransactionStatus.BLOCKED,
                        new RiskScore(100),
                        List.of(
                                new RuleViolation(
                                        "RESTRICTED_COUNTRY",
                                        Severity.CRITICAL,
                                        "Beneficiary country is restricted"
                                )
                        )
                );

        complianceResultRepository.save(second);

        ComplianceResultDocument document =
                springDataRepository
                        .findById("transaction-4")
                        .orElseThrow();

        assertThat(document.toDomain())
                .isEqualTo(second);
    }

    private ComplianceResult createComplianceResult() {

        List<RuleViolation> violations = List.of(
                new RuleViolation(
                        "HIGH_AMOUNT",
                        Severity.HIGH,
                        "Transaction exceeds configured threshold"
                ),
                new RuleViolation(
                        "HIGH_RISK_CUSTOMER",
                        Severity.HIGH,
                        "High-risk customer with transaction above threshold"
                )
        );

        return new ComplianceResult(
                "transaction-1",
                TransactionStatus.MANUAL_REVIEW,
                new RiskScore(100),
                violations
        );
    }
}
