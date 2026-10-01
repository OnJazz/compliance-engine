package com.jasonvennin.compliance.api.dto;

import com.jasonvennin.compliance.transaction.domain.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateTransactionRequest(

        @NotBlank
        String id,

        @NotBlank
        String externalId,

        @NotBlank
        String customerId,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount,

        @NotBlank
        String currency,

        @NotNull
        TransactionType type,

        @NotBlank
        String country,

        @NotBlank
        String beneficiaryCountry
) {}