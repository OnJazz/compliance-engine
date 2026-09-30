package com.jasonvennin.compliance.application.usecase;

import com.jasonvennin.compliance.application.exception.CustomerNotFoundException;
import com.jasonvennin.compliance.application.exception.TransactionNotFoundException;
import com.jasonvennin.compliance.application.port.ComplianceResultRepository;
import com.jasonvennin.compliance.application.port.CustomerRepository;
import com.jasonvennin.compliance.application.port.TransactionRepository;
import com.jasonvennin.compliance.compliance.domain.ComplianceEngine;
import com.jasonvennin.compliance.compliance.domain.ComplianceResult;
import com.jasonvennin.compliance.compliance.domain.RiskScore;
import com.jasonvennin.compliance.customer.domain.Customer;
import com.jasonvennin.compliance.customer.domain.RiskLevel;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import com.jasonvennin.compliance.transaction.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class EvaluateTransactionUseCaseTest {

    private TransactionRepository transactionRepository;
    private CustomerRepository customerRepository;
    private ComplianceResultRepository complianceResultRepository;
    private ComplianceEngine complianceEngine;

    private EvaluateTransactionUseCase useCase;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        customerRepository = mock(CustomerRepository.class);
        complianceResultRepository = mock(ComplianceResultRepository.class);
        complianceEngine = mock(ComplianceEngine.class);

        useCase = new EvaluateTransactionUseCase(
                transactionRepository,
                customerRepository,
                complianceResultRepository,
                complianceEngine
        );
    }

    @Test
    void shouldEvaluateTransactionAndSaveResult() {
        Transaction transaction = transaction();
        Customer customer = customer();

        ComplianceResult expectedResult = new ComplianceResult(
                transaction.id(),
                TransactionStatus.APPROVED,
                new RiskScore(0),
                List.of()
        );

        when(transactionRepository.findById(transaction.id()))
                .thenReturn(Optional.of(transaction));

        when(customerRepository.findById(customer.id()))
                .thenReturn(Optional.of(customer));

        when(complianceEngine.evaluate(transaction, customer))
                .thenReturn(expectedResult);

        when(complianceResultRepository.save(expectedResult))
                .thenReturn(expectedResult);

        ComplianceResult result =
                useCase.evaluate(transaction.id());

        assertEquals(expectedResult, result);

        verify(transactionRepository)
                .findById(transaction.id());

        verify(customerRepository)
                .findById(customer.id());

        verify(complianceEngine)
                .evaluate(transaction, customer);

        verify(complianceResultRepository)
                .save(expectedResult);
    }

    @Test
    void shouldThrowTransactionNotFoundExceptionWhenTransactionDoesNotExist() {
        String transactionId = "transaction-404";

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        TransactionNotFoundException exception = assertThrows(
                TransactionNotFoundException.class,
                () -> useCase.evaluate(transactionId)
        );

        assertEquals(
                "Transaction not found: " + transactionId,
                exception.getMessage()
        );

        verify(transactionRepository)
                .findById(transactionId);

        verifyNoInteractions(
                customerRepository,
                complianceEngine,
                complianceResultRepository
        );
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
        Transaction transaction = transaction();

        when(transactionRepository.findById(transaction.id()))
                .thenReturn(Optional.of(transaction));

        when(customerRepository.findById(transaction.customerId()))
                .thenReturn(Optional.empty());

        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> useCase.evaluate(transaction.id())
        );

        assertEquals(
                "Customer not found: " + transaction.customerId(),
                exception.getMessage()
        );

        verify(transactionRepository)
                .findById(transaction.id());

        verify(customerRepository)
                .findById(transaction.customerId());

        verifyNoInteractions(
                complianceEngine,
                complianceResultRepository
        );
    }

    @Test
    void shouldNotSaveResultWhenComplianceEvaluationFails() {
        Transaction transaction = transaction();
        Customer customer = customer();

        when(transactionRepository.findById(transaction.id()))
                .thenReturn(Optional.of(transaction));

        when(customerRepository.findById(customer.id()))
                .thenReturn(Optional.of(customer));

        when(complianceEngine.evaluate(transaction, customer))
                .thenThrow(new RuntimeException("Compliance engine failure"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> useCase.evaluate(transaction.id())
        );

        assertEquals(
                "Compliance engine failure",
                exception.getMessage()
        );

        verify(complianceEngine)
                .evaluate(transaction, customer);

        verifyNoInteractions(complianceResultRepository);
    }

    private Customer customer() {
        return new Customer(
                "customer-1",
                "CUST-001",
                "John",
                "Doe",
                "CH",
                RiskLevel.LOW
        );
    }

    private Transaction transaction() {
        return new Transaction(
                "transaction-1",
                "TX-001",
                "customer-1",
                new BigDecimal("1000"),
                "CHF",
                TransactionType.TRANSFER,
                "CH",
                "FR",
                Instant.parse("2026-09-24T10:00:00Z"),
                TransactionStatus.PENDING
        );
    }
}