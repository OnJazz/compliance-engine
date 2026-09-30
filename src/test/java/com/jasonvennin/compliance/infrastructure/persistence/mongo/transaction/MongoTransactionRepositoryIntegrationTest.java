package com.jasonvennin.compliance.infrastructure.persistence.mongo.transaction;

import com.jasonvennin.compliance.infrastructure.persistence.mongo.customer.MongoCustomerRepository;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import com.jasonvennin.compliance.transaction.domain.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataMongoTest
@Testcontainers
@Import(MongoTransactionRepository.class)
class MongoTransactionRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo =
            new MongoDBContainer("mongo:8.0");

    @Autowired
    private MongoTransactionRepository transactionRepository;

    @Autowired
    private SpringDataTransactionRepository springDataRepository;

    @Test
    void shouldSaveAndFindTransaction() {

        Transaction transaction = createTransaction(
                "transaction-1",
                "customer-1",
                Instant.parse("2026-09-24T10:00:00Z")
        );

        Transaction saved = transactionRepository.save(transaction);

        assertThat(saved)
                .isEqualTo(transaction);

        Transaction found = transactionRepository
                .findById(transaction.id())
                .orElseThrow();

        assertThat(found)
                .isEqualTo(transaction);
    }

    @Test
    void shouldReturnEmptyWhenTransactionDoesNotExist() {

        assertThat(transactionRepository.findById("unknown"))
                .isEmpty();
    }

    @Test
    void shouldPersistTransactionAsMongoDocument() {

        Transaction transaction = createTransaction(
                "transaction-2",
                "customer-2",
                Instant.parse("2026-09-24T11:00:00Z")
        );

        transactionRepository.save(transaction);

        TransactionDocument document = springDataRepository
                .findById(transaction.id())
                .orElseThrow();

        assertThat(document.id())
                .isEqualTo(transaction.id());

        assertThat(document.externalId())
                .isEqualTo(transaction.externalId());

        assertThat(document.customerId())
                .isEqualTo(transaction.customerId());

        assertThat(document.amount())
                .isEqualByComparingTo(transaction.amount());

        assertThat(document.currency())
                .isEqualTo(transaction.currency());

        assertThat(document.type())
                .isEqualTo(transaction.type());

        assertThat(document.country())
                .isEqualTo(transaction.country());

        assertThat(document.beneficiaryCountry())
                .isEqualTo(transaction.beneficiaryCountry());

        assertThat(document.createdAt())
                .isEqualTo(transaction.createdAt());

        assertThat(document.status())
                .isEqualTo(transaction.status());
    }

    @Test
    void shouldUpdateExistingTransaction() {

        Transaction transaction = createTransaction(
                "transaction-3",
                "customer-3",
                Instant.parse("2026-09-24T12:00:00Z")
        );

        transactionRepository.save(transaction);

        Transaction updated = new Transaction(
                transaction.id(),
                transaction.externalId(),
                transaction.customerId(),
                new BigDecimal("75000"),
                transaction.currency(),
                transaction.type(),
                transaction.country(),
                transaction.beneficiaryCountry(),
                transaction.createdAt(),
                TransactionStatus.BLOCKED
        );

        transactionRepository.save(updated);

        Transaction found = transactionRepository
                .findById(transaction.id())
                .orElseThrow();

        assertThat(found)
                .isEqualTo(updated);
    }

    private Transaction createTransaction(
            String id,
            String customerId,
            Instant createdAt
    ) {
        return new Transaction(
                id,
                "EXT-" + id,
                customerId,
                new BigDecimal("1000.00"),
                "CHF",
                TransactionType.TRANSFER,
                "CH",
                "FR",
                createdAt,
                TransactionStatus.PENDING
        );
    }
}
