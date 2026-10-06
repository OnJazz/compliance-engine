package com.jasonvennin.compliance.api.controller;

import com.jasonvennin.compliance.api.exception.GlobalExceptionHandler;
import com.jasonvennin.compliance.application.exception.CustomerNotFoundException;
import com.jasonvennin.compliance.application.exception.TransactionNotFoundException;
import com.jasonvennin.compliance.application.usecase.EvaluateTransactionUseCase;
import com.jasonvennin.compliance.compliance.domain.ComplianceResult;
import com.jasonvennin.compliance.compliance.domain.RiskScore;
import com.jasonvennin.compliance.rule.domain.RuleViolation;
import com.jasonvennin.compliance.rule.domain.Severity;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.web.OAuth2ResourceServerWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.jasonvennin.compliance.application.usecase.GetComplianceResultUseCase;
import com.jasonvennin.compliance.application.exception.ComplianceResultNotFoundException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ComplianceController.class,
        excludeAutoConfiguration = {
                OAuth2ResourceServerAutoConfiguration.class,
                OAuth2ResourceServerWebSecurityAutoConfiguration.class
        }
)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class ComplianceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EvaluateTransactionUseCase evaluateTransactionUseCase;

    @MockitoBean
    private GetComplianceResultUseCase getComplianceResultUseCase;

    @Test
    void shouldEvaluateTransaction() throws Exception {
        ComplianceResult result = new ComplianceResult(
                "transaction-1",
                TransactionStatus.APPROVED,
                new RiskScore(0),
                List.of()
        );

        when(evaluateTransactionUseCase.evaluate("transaction-1"))
                .thenReturn(result);

        mockMvc.perform(
                        post(
                                "/api/compliance/transactions/{transactionId}/evaluate",
                                "transaction-1"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("transaction-1"))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.riskScore").value(0))
                .andExpect(jsonPath("$.violations").isEmpty());

        verify(evaluateTransactionUseCase).evaluate("transaction-1");
    }

    @Test
    void shouldReturnTransactionNotFound() throws Exception {
        when(evaluateTransactionUseCase.evaluate("unknown"))
                .thenThrow(new TransactionNotFoundException("unknown"));

        mockMvc.perform(
                        post(
                                "/api/compliance/transactions/{transactionId}/evaluate",
                                "unknown"
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("TRANSACTION_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(
                        "Transaction not found: unknown"
                ));
    }

    @Test
    void shouldReturnCustomerNotFound() throws Exception {
        when(evaluateTransactionUseCase.evaluate("transaction-1"))
                .thenThrow(new CustomerNotFoundException("customer-1"));

        mockMvc.perform(
                        post(
                                "/api/compliance/transactions/{transactionId}/evaluate",
                                "transaction-1"
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("CUSTOMER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(
                        "Customer not found: customer-1"
                ));
    }

    @Test
    void shouldReturnBadRequestWhenTransactionIdIsBlank() throws Exception {
        mockMvc.perform(
                        post(
                                "/api/compliance/transactions/{transactionId}/evaluate",
                                "   "
                        )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verifyNoInteractions(evaluateTransactionUseCase);
    }

    @Test
    void shouldReturnHighRiskResult() throws Exception {
        RuleViolation violation = new RuleViolation(
                "HIGH_AMOUNT",
                Severity.HIGH,
                "Transaction amount exceeds the configured threshold"
        );

        ComplianceResult result = new ComplianceResult(
                "transaction-1",
                TransactionStatus.MANUAL_REVIEW,
                new RiskScore(50),
                List.of(violation)
        );

        when(evaluateTransactionUseCase.evaluate("transaction-1"))
                .thenReturn(result);

        mockMvc.perform(
                        post(
                                "/api/compliance/transactions/{transactionId}/evaluate",
                                "transaction-1"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("transaction-1"))
                .andExpect(jsonPath("$.status").value("MANUAL_REVIEW"))
                .andExpect(jsonPath("$.riskScore").value(50))
                .andExpect(jsonPath("$.violations[0].ruleCode").value("HIGH_AMOUNT"))
                .andExpect(jsonPath("$.violations[0].severity").value("HIGH"));
    }

    @Test
    void shouldReturnComplianceResult() throws Exception {
        ComplianceResult result = new ComplianceResult(
                "transaction-1",
                TransactionStatus.APPROVED,
                new RiskScore(0),
                List.of()
        );

        when(getComplianceResultUseCase.execute("transaction-1"))
                .thenReturn(result);

        mockMvc.perform(
                        get(
                                "/api/compliance/transactions/{transactionId}",
                                "transaction-1"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("transaction-1"))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.riskScore").value(0))
                .andExpect(jsonPath("$.violations").isEmpty());

        verify(getComplianceResultUseCase)
                .execute("transaction-1");
    }

    @Test
    void shouldReturnComplianceResultNotFound() throws Exception {
        when(getComplianceResultUseCase.execute("unknown"))
                .thenThrow(
                        new ComplianceResultNotFoundException("unknown")
                );

        mockMvc.perform(
                        get(
                                "/api/compliance/transactions/{transactionId}",
                                "unknown"
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error")
                        .value("COMPLIANCE_RESULT_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Compliance result not found for transaction: unknown"
                        ));
    }

    @Test
    void shouldReturnBadRequestWhenComplianceResultTransactionIdIsBlank()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/compliance/transactions/{transactionId}",
                                "   "
                        )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verifyNoInteractions(getComplianceResultUseCase);
    }
}