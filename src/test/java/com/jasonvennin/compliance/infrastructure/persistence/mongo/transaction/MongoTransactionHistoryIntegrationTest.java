package com.jasonvennin.compliance.infrastructure.persistence.mongo.transaction;

import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionHistory;
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
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataMongoTest
@Testcontainers
@Import(MongoTransactionHistory.class)
class MongoTransactionHistoryIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo =
            new MongoDBContainer("mongo:8.0");

    @Autowired
    private TransactionHistory transactionHistory;

    @Autowired
    private SpringDataTransactionRepository repository;

    @Test
    void shouldFindPreviousTransactionsWithinTimeWindow() {

        String customerId = "customer-1";

        Instant currentTime =
                Instant.parse("2026-09-24T10:10:00Z");

        Transaction transaction1 = createTransaction(
                "transaction-1",
                customerId,
                Instant.parse("2026-09-24T10:01:00Z")
        );

        Transaction transaction2 = createTransaction(
                "transaction-2",
                customerId,
                Instant.parse("2026-09-24T10:05:00Z")
        );

        Transaction transaction3 = createTransaction(
                "transaction-3",
                customerId,
                Instant.parse("2026-09-24T09:50:00Z")
        );

        save(transaction1);
        save(transaction2);
        save(transaction3);

        Instant from = currentTime.minusSeconds(600);

        List<Transaction> result =
                transactionHistory.findPreviousTransactions(
                        customerId,
                        from,
                        currentTime
                );

        assertThat(result)
                .containsExactlyInAnyOrder(
                        transaction1,
                        transaction2
                );
    }

    @Test
    void shouldNotIncludeTransactionAtUpperBoundary() {

        String customerId = "customer-2";

        Instant currentTime =
                Instant.parse("2026-09-24T10:10:00Z");

        Transaction transactionAtBoundary = createTransaction(
                "transaction-boundary",
                customerId,
                currentTime
        );

        save(transactionAtBoundary);

        Instant from = currentTime.minusSeconds(600);

        List<Transaction> result =
                transactionHistory.findPreviousTransactions(
                        customerId,
                        from,
                        currentTime
                );

        assertThat(result)
                .isEmpty();
    }

    @Test
    void shouldNotReturnTransactionsFromAnotherCustomer() {

        String customerId = "customer-3";

        Instant currentTime =
                Instant.parse("2026-09-24T10:10:00Z");

        Transaction transaction =
                createTransaction(
                        "transaction-other-customer",
                        "customer-4",
                        Instant.parse("2026-09-24T10:05:00Z")
                );

        save(transaction);

        Instant from = currentTime.minusSeconds(600);

        List<Transaction> result =
                transactionHistory.findPreviousTransactions(
                        customerId,
                        from,
                        currentTime
                );

        assertThat(result)
                .isEmpty();
    }

    @Test
    void shouldNotReturnTransactionsOutsideTimeWindow() {

        String customerId = "customer-5";

        Instant currentTime =
                Instant.parse("2026-09-24T10:10:00Z");

        Transaction oldTransaction =
                createTransaction(
                        "transaction-old",
                        customerId,
                        Instant.parse("2026-09-24T09:59:00Z")
                );

        save(oldTransaction);

        Instant from = currentTime.minusSeconds(600);

        List<Transaction> result =
                transactionHistory.findPreviousTransactions(
                        customerId,
                        from,
                        currentTime
                );

        assertThat(result)
                .isEmpty();
    }

    private void save(Transaction transaction) {
        repository.save(
                TransactionDocument.fromDomain(transaction)
        );
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
