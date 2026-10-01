package com.jasonvennin.compliance.application.usecase;

import com.jasonvennin.compliance.application.exception.TransactionNotFoundException;
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
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTransactionUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private GetTransactionUseCase getTransactionUseCase;

    @Test
    void shouldReturnTransaction() {

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

        when(transactionRepository.findById("transaction-1"))
                .thenReturn(Optional.of(transaction));

        Transaction result =
                getTransactionUseCase.execute("transaction-1");

        assertEquals(transaction, result);

        verify(transactionRepository)
                .findById("transaction-1");
    }

    @Test
    void shouldThrowWhenTransactionDoesNotExist() {

        when(transactionRepository.findById("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                TransactionNotFoundException.class,
                () -> getTransactionUseCase.execute("unknown")
        );

        verify(transactionRepository)
                .findById("unknown");
    }
}