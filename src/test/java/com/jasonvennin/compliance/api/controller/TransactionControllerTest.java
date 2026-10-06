package com.jasonvennin.compliance.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasonvennin.compliance.api.dto.CreateTransactionRequest;
import com.jasonvennin.compliance.api.exception.GlobalExceptionHandler;
import com.jasonvennin.compliance.application.exception.TransactionAlreadyExistsException;
import com.jasonvennin.compliance.application.exception.TransactionNotFoundException;
import com.jasonvennin.compliance.application.usecase.CreateTransactionUseCase;
import com.jasonvennin.compliance.application.usecase.GetTransactionUseCase;
import com.jasonvennin.compliance.application.usecase.GetTransactionsUseCase;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import com.jasonvennin.compliance.transaction.domain.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.web.OAuth2ResourceServerWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = TransactionController.class,
        excludeAutoConfiguration = {
                OAuth2ResourceServerAutoConfiguration.class,
                OAuth2ResourceServerWebSecurityAutoConfiguration.class
        }
)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CreateTransactionUseCase createTransactionUseCase;

    @MockitoBean
    private GetTransactionUseCase getTransactionUseCase;

    @MockitoBean
    private GetTransactionsUseCase getTransactionsUseCase;

    @Test
    void shouldCreateTransaction() throws Exception {

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

        when(createTransactionUseCase.execute(any()))
                .thenReturn(transaction());

        mockMvc.perform(
                        post("/api/transactions")
                                .contentType(APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("transaction-1"))
                .andExpect(jsonPath("$.customerId").value("customer-1"))
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(createTransactionUseCase)
                .execute(any());
    }

    @Test
    void shouldReturnTransactionAlreadyExists() throws Exception {

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

        when(createTransactionUseCase.execute(any()))
                .thenThrow(
                        new TransactionAlreadyExistsException(
                                "transaction-1"
                        )
                );

        mockMvc.perform(
                        post("/api/transactions")
                                .contentType(APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error")
                        .value("TRANSACTION_ALREADY_EXISTS"));

        verify(createTransactionUseCase)
                .execute(any());
    }

    @Test
    void shouldReturnBadRequestWhenCreateRequestIsInvalid()
            throws Exception {

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        "",
                        "",
                        "",
                        BigDecimal.ZERO,
                        "",
                        null,
                        "",
                        ""
                );

        mockMvc.perform(
                        post("/api/transactions")
                                .contentType(APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createTransactionUseCase);
    }

    @Test
    void shouldGetTransactions() throws Exception {

        Transaction transaction = transaction();

        PageImpl<Transaction> page =
                new PageImpl<>(
                        List.of(transaction),
                        PageRequest.of(
                                0,
                                20,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "createdAt"
                                )
                        ),
                        1
                );

        when(getTransactionsUseCase.execute(
                eq("customer-1"),
                eq("PENDING"),
                eq("TRANSFER"),
                any()
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/transactions")
                                .param("customerId", "customer-1")
                                .param("status", "PENDING")
                                .param("type", "TRANSFER")
                                .param("page", "0")
                                .param("size", "20")
                                .param("sortBy", "createdAt")
                                .param("direction", "desc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id")
                        .value("transaction-1"))
                .andExpect(jsonPath("$.content[0].customerId")
                        .value("customer-1"))
                .andExpect(jsonPath("$.content[0].status")
                        .value("PENDING"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(getTransactionsUseCase).execute(
                eq("customer-1"),
                eq("PENDING"),
                eq("TRANSFER"),
                any()
        );
    }

    @Test
    void shouldGetTransaction() throws Exception {

        when(getTransactionUseCase.execute("transaction-1"))
                .thenReturn(transaction());

        mockMvc.perform(
                        get(
                                "/api/transactions/{transactionId}",
                                "transaction-1"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("transaction-1"))
                .andExpect(jsonPath("$.customerId")
                        .value("customer-1"))
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(getTransactionUseCase)
                .execute("transaction-1");
    }

    @Test
    void shouldReturnTransactionNotFound() throws Exception {

        when(getTransactionUseCase.execute("unknown"))
                .thenThrow(
                        new TransactionNotFoundException("unknown")
                );

        mockMvc.perform(
                        get(
                                "/api/transactions/{transactionId}",
                                "unknown"
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error")
                        .value("TRANSACTION_NOT_FOUND"));

        verify(getTransactionUseCase)
                .execute("unknown");
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