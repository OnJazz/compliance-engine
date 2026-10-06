package com.jasonvennin.compliance.application.usecase;

import com.jasonvennin.compliance.application.exception.ComplianceResultNotFoundException;
import com.jasonvennin.compliance.application.port.ComplianceResultRepository;
import com.jasonvennin.compliance.compliance.domain.ComplianceResult;
import com.jasonvennin.compliance.compliance.domain.RiskScore;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetComplianceResultUseCaseTest {

    @Mock
    private ComplianceResultRepository complianceResultRepository;

    @InjectMocks
    private GetComplianceResultUseCase getComplianceResultUseCase;

    @Test
    void shouldReturnComplianceResult() {

        ComplianceResult result = new ComplianceResult(
                "transaction-1",
                TransactionStatus.APPROVED,
                new RiskScore(0),
                List.of()
        );

        when(
                complianceResultRepository
                        .findByTransactionId("transaction-1")
        ).thenReturn(Optional.of(result));

        ComplianceResult returned =
                getComplianceResultUseCase.execute("transaction-1");

        assertEquals(result, returned);

        verify(complianceResultRepository)
                .findByTransactionId("transaction-1");
    }

    @Test
    void shouldThrowWhenComplianceResultDoesNotExist() {

        when(
                complianceResultRepository
                        .findByTransactionId("unknown")
        ).thenReturn(Optional.empty());

        assertThrows(
                ComplianceResultNotFoundException.class,
                () -> getComplianceResultUseCase.execute("unknown")
        );

        verify(complianceResultRepository)
                .findByTransactionId("unknown");
    }
}