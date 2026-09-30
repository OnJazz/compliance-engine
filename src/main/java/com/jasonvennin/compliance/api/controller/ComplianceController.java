package com.jasonvennin.compliance.api.controller;

import com.jasonvennin.compliance.api.dto.ComplianceResultResponse;
import com.jasonvennin.compliance.application.usecase.EvaluateTransactionUseCase;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.jasonvennin.compliance.api.exception.ErrorResponse;

@RestController
@RequestMapping("/api/compliance")
@Validated
public class ComplianceController {

    private final EvaluateTransactionUseCase evaluateTransactionUseCase;

    public ComplianceController(
            EvaluateTransactionUseCase evaluateTransactionUseCase
    ) {
        this.evaluateTransactionUseCase = evaluateTransactionUseCase;
    }

    @Operation(
            summary = "Evaluate a transaction",
            description = "Evaluates a transaction against all configured compliance rules."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction successfully evaluated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ComplianceResultResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid transaction ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction or customer not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping("/transactions/{transactionId}/evaluate")
    public ResponseEntity<ComplianceResultResponse> evaluateTransaction(
            @Parameter(
                    description = "Internal transaction identifier",
                    example = "transaction-1"
            )
            @PathVariable @NotBlank String transactionId
    ) {
        var result =
                evaluateTransactionUseCase.evaluate(transactionId);

        return ResponseEntity.ok(
                ComplianceResultResponse.fromDomain(result)
        );
    }
}
