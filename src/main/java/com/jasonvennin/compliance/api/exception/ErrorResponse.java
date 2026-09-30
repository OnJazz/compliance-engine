package com.jasonvennin.compliance.api.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard API error response")
public record ErrorResponse(

        @Schema(example = "404")
        int status,

        @Schema(example = "TRANSACTION_NOT_FOUND")
        String error,

        @Schema(example = "Transaction not found: transaction-1")
        String message
) {}
