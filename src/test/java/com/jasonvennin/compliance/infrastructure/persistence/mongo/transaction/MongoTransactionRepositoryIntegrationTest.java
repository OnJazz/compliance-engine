package com.jasonvennin.compliance.infrastructure.persistence.mongo.transaction;

import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import com.jasonvennin.compliance.transaction.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.remove(
                new Query(),
                TransactionDocument.class
        );
    }

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

    @Test
    void shouldReturnPaginatedTransactions() {

        saveTransactions(
                createTransaction(
                        "transaction-1",
                        "customer-1",
                        Instant.parse("2026-09-24T10:00:00Z")
                ),
                createTransaction(
                        "transaction-2",
                        "customer-2",
                        Instant.parse("2026-09-24T11:00:00Z")
                ),
                createTransaction(
                        "transaction-3",
                        "customer-3",
                        Instant.parse("2026-09-24T12:00:00Z")
                )
        );

        Page<Transaction> result =
                transactionRepository.findAll(
                        null,
                        null,
                        null,
                        PageRequest.of(
                                0,
                                2,
                                Sort.by(
                                        Sort.Direction.ASC,
                                        "createdAt"
                                )
                        )
                );

        assertThat(result.getContent())
                .hasSize(2);

        assertThat(result.getTotalElements())
                .isEqualTo(3);

        assertThat(result.getTotalPages())
                .isEqualTo(2);

        assertThat(result.getContent().get(0).id())
                .isEqualTo("transaction-1");

        assertThat(result.getContent().get(1).id())
                .isEqualTo("transaction-2");
    }

    @Test
    void shouldFilterTransactionsByCustomerId() {

        saveTransactions(
                createTransaction(
                        "transaction-1",
                        "customer-1",
                        Instant.parse("2026-09-24T10:00:00Z")
                ),
                createTransaction(
                        "transaction-2",
                        "customer-1",
                        Instant.parse("2026-09-24T11:00:00Z")
                ),
                createTransaction(
                        "transaction-3",
                        "customer-2",
                        Instant.parse("2026-09-24T12:00:00Z")
                )
        );

        Page<Transaction> result =
                transactionRepository.findAll(
                        "customer-1",
                        null,
                        null,
                        PageRequest.of(0, 20)
                );

        assertThat(result.getContent())
                .hasSize(2);

        assertThat(result.getTotalElements())
                .isEqualTo(2);

        assertThat(
                result.getContent()
                        .stream()
                        .allMatch(
                                transaction ->
                                        transaction.customerId()
                                                .equals("customer-1")
                        )
        )
                .isTrue();
    }

    @Test
    void shouldFilterTransactionsByStatus() {
        Transaction pending = createTransaction(
                "transaction-1",
                "customer-1",
                Instant.parse("2026-09-24T10:00:00Z")
        );

        Transaction blocked = new Transaction(
                "transaction-2",
                "EXT-transaction-2",
                "customer-2",
                new BigDecimal("1000.00"),
                "CHF",
                TransactionType.TRANSFER,
                "CH",
                "FR",
                Instant.parse("2026-09-24T11:00:00Z"),
                TransactionStatus.BLOCKED
        );

        saveTransactions(pending, blocked);

        Page<Transaction> result =
                transactionRepository.findAll(
                        null,
                        "BLOCKED",
                        null,
                        PageRequest.of(0, 20)
                );

        assertThat(result.getContent())
                .hasSize(1);

        assertThat(result.getTotalElements())
                .isEqualTo(1);

        assertThat(result.getContent().get(0).id())
                .isEqualTo("transaction-2");

        assertThat(result.getContent().get(0).status())
                .isEqualTo(TransactionStatus.BLOCKED);
    }

    @Test
    void shouldFilterTransactionsByType() {

        Transaction transfer = createTransaction(
                "transaction-1",
                "customer-1",
                Instant.parse("2026-09-24T10:00:00Z")
        );

        Transaction payment = new Transaction(
                "transaction-2",
                "EXT-transaction-2",
                "customer-2",
                new BigDecimal("1000.00"),
                "CHF",
                TransactionType.PAYMENT,
                "CH",
                "FR",
                Instant.parse("2026-09-24T11:00:00Z"),
                TransactionStatus.PENDING
        );

        saveTransactions(transfer, payment);

        Page<Transaction> result =
                transactionRepository.findAll(
                        null,
                        null,
                        "PAYMENT",
                        PageRequest.of(0, 20)
                );

        assertThat(result.getContent())
                .hasSize(1);

        assertThat(result.getTotalElements())
                .isEqualTo(1);

        assertThat(result.getContent().get(0).id())
                .isEqualTo("transaction-2");

        assertThat(result.getContent().get(0).type())
                .isEqualTo(TransactionType.PAYMENT);
    }

    @Test
    void shouldApplyMultipleFiltersAndSortTransactions() {

        Transaction first = createTransaction(
                "transaction-1",
                "customer-1",
                Instant.parse("2026-09-24T10:00:00Z")
        );

        Transaction second = new Transaction(
                "transaction-2",
                "EXT-transaction-2",
                "customer-1",
                new BigDecimal("2000.00"),
                "CHF",
                TransactionType.TRANSFER,
                "CH",
                "FR",
                Instant.parse("2026-09-24T12:00:00Z"),
                TransactionStatus.BLOCKED
        );

        Transaction otherCustomer = new Transaction(
                "transaction-3",
                "EXT-transaction-3",
                "customer-2",
                new BigDecimal("3000.00"),
                "CHF",
                TransactionType.TRANSFER,
                "CH",
                "FR",
                Instant.parse("2026-09-24T13:00:00Z"),
                TransactionStatus.BLOCKED
        );

        saveTransactions(
                first,
                second,
                otherCustomer
        );

        Page<Transaction> result =
                transactionRepository.findAll(
                        "customer-1",
                        "BLOCKED",
                        "TRANSFER",
                        PageRequest.of(
                                0,
                                20,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "createdAt"
                                )
                        )
                );

        assertThat(result.getContent())
                .hasSize(1);

        assertThat(result.getTotalElements())
                .isEqualTo(1);

        assertThat(result.getContent().get(0).id())
                .isEqualTo("transaction-2");

        assertThat(result.getContent().get(0).customerId())
                .isEqualTo("customer-1");

        assertThat(result.getContent().get(0).status())
                .isEqualTo(TransactionStatus.BLOCKED);

        assertThat(result.getContent().get(0).type())
                .isEqualTo(TransactionType.TRANSFER);
    }

    private void saveTransactions(Transaction... transactions) {
        for (Transaction transaction : transactions) {
            transactionRepository.save(transaction);
        }
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