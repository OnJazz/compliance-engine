package com.jasonvennin.compliance.application.usecase;

import com.jasonvennin.compliance.application.port.TransactionRepository;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import com.jasonvennin.compliance.transaction.domain.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetTransactionsUseCaseTest {

    @Test
    void shouldReturnTransactions() {

        TransactionRepository transactionRepository =
                mock(TransactionRepository.class);

        GetTransactionsUseCase useCase =
                new GetTransactionsUseCase(transactionRepository);

        Pageable pageable = PageRequest.of(
                0,
                20,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        Transaction transaction = transaction();

        Page<Transaction> expectedPage =
                new PageImpl<>(
                        List.of(transaction),
                        pageable,
                        1
                );

        when(transactionRepository.findAll(
                "customer-1",
                "PENDING",
                "TRANSFER",
                pageable
        )).thenReturn(expectedPage);

        Page<Transaction> result =
                useCase.execute(
                        "customer-1",
                        "PENDING",
                        "TRANSFER",
                        pageable
                );

        assertEquals(
                expectedPage,
                result
        );

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                "transaction-1",
                result.getContent()
                        .getFirst()
                        .id()
        );

        verify(transactionRepository).findAll(
                eq("customer-1"),
                eq("PENDING"),
                eq("TRANSFER"),
                eq(pageable)
        );
    }

    @Test
    void shouldReturnEmptyPageWhenNoTransactionExists() {

        TransactionRepository transactionRepository =
                mock(TransactionRepository.class);

        GetTransactionsUseCase useCase =
                new GetTransactionsUseCase(transactionRepository);

        Pageable pageable = PageRequest.of(0, 20);

        Page<Transaction> emptyPage =
                new PageImpl<>(
                        List.of(),
                        pageable,
                        0
                );

        when(transactionRepository.findAll(
                null,
                null,
                null,
                pageable
        )).thenReturn(emptyPage);

        Page<Transaction> result =
                useCase.execute(
                        null,
                        null,
                        null,
                        pageable
                );

        assertEquals(
                0,
                result.getTotalElements()
        );

        assertEquals(
                0,
                result.getContent().size()
        );

        verify(transactionRepository).findAll(
                eq(null),
                eq(null),
                eq(null),
                eq(pageable)
        );
    }

    private Transaction transaction() {

        return new Transaction(
                "transaction-1",
                "TRANSACTION-001",
                "customer-1",
                BigDecimal.valueOf(1_000),
                "EUR",
                TransactionType.TRANSFER,
                "FR",
                "FR",
                Instant.parse("2026-01-01T10:00:00Z"),
                TransactionStatus.PENDING
        );
    }
}