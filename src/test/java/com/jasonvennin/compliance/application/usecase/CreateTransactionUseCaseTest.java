package com.jasonvennin.compliance.application.usecase;

import com.jasonvennin.compliance.api.dto.CreateTransactionRequest;
import com.jasonvennin.compliance.application.exception.TransactionAlreadyExistsException;
import com.jasonvennin.compliance.application.port.TransactionRepository;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import com.jasonvennin.compliance.transaction.domain.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTransactionUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CreateTransactionUseCase createTransactionUseCase;

    @Test
    void shouldCreateTransaction() {

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        "transaction-1",
                        "TRANSACTION-001",
                        "customer-1",
                        BigDecimal.valueOf(1_000),
                        "EUR",
                        TransactionType.TRANSFER,
                        "FR",
                        "FR"
                );

        when(transactionRepository.findById("transaction-1"))
                .thenReturn(Optional.empty());

        Transaction savedTransaction = new Transaction(
                "transaction-1",
                "transaction-1",
                "customer-1",
                BigDecimal.valueOf(1_000),
                "EUR",
                TransactionType.TRANSFER,
                "FR",
                "FR",
                java.time.Instant.now(),
                TransactionStatus.PENDING
        );

        when(transactionRepository.save(
                org.mockito.ArgumentMatchers.any(Transaction.class)
        )).thenReturn(savedTransaction);

        Transaction result =
                createTransactionUseCase.execute(request);

        assertNotNull(result);
        assertEquals("transaction-1", result.id());
        assertEquals("customer-1", result.customerId());
        assertEquals(
                BigDecimal.valueOf(1_000),
                result.amount()
        );
        assertEquals(TransactionStatus.PENDING, result.status());

        verify(transactionRepository)
                .findById("transaction-1");

        verify(transactionRepository)
                .save(org.mockito.ArgumentMatchers.any(Transaction.class));
    }

    @Test
    void shouldThrowWhenTransactionAlreadyExists() {

        Transaction existingTransaction = new Transaction(
                "transaction-1",
                "transaction-1",
                "customer-1",
                BigDecimal.valueOf(1_000),
                "EUR",
                TransactionType.TRANSFER,
                "FR",
                "FR",
                java.time.Instant.now(),
                TransactionStatus.PENDING
        );

        when(transactionRepository.findById("transaction-1"))
                .thenReturn(Optional.of(existingTransaction));

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        "transaction-1",
                        "TRANSACTION-001",
                        "customer-1",
                        BigDecimal.valueOf(1_000),
                        "EUR",
                        TransactionType.TRANSFER,
                        "FR",
                        "FR"
                );

        assertThrows(
                TransactionAlreadyExistsException.class,
                () -> createTransactionUseCase.execute(request)
        );

        verify(transactionRepository)
                .findById("transaction-1");

        verify(transactionRepository, never())
                .save(org.mockito.ArgumentMatchers.any(Transaction.class));
    }
}