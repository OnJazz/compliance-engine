package com.jasonvennin.compliance.api.controller;

import com.jasonvennin.compliance.api.dto.ComplianceResultResponse;
import com.jasonvennin.compliance.api.exception.ErrorResponse;
import com.jasonvennin.compliance.application.usecase.EvaluateTransactionUseCase;
import com.jasonvennin.compliance.application.usecase.GetComplianceResultUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compliance")
@Validated
public class ComplianceController {

    private final EvaluateTransactionUseCase evaluateTransactionUseCase;
    private final GetComplianceResultUseCase getComplianceResultUseCase;

    public ComplianceController(
            EvaluateTransactionUseCase evaluateTransactionUseCase,
            GetComplianceResultUseCase getComplianceResultUseCase
    ) {
        this.evaluateTransactionUseCase = evaluateTransactionUseCase;
        this.getComplianceResultUseCase = getComplianceResultUseCase;
    }

    @Operation(
            summary = "Evaluate a transaction",
            description = """
                    Evaluates a transaction against all configured compliance rules.

                    The evaluation loads the transaction and its customer,
                    applies all compliance rules, calculates the risk score,
                    determines the resulting transaction status and stores
                    the compliance result.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
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
                    responseCode = "401",
                    description = "Authentication required or JWT token is invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authenticated user does not have sufficient permissions",
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
                    name = "transactionId",
                    description = "Internal transaction identifier",
                    required = true,
                    example = "transaction-1"
            )
            @PathVariable
            @NotBlank
            String transactionId
    ) {
        var result =
                evaluateTransactionUseCase.evaluate(transactionId);

        return ResponseEntity.ok(
                ComplianceResultResponse.fromDomain(result)
        );
    }

    @Operation(
            summary = "Get compliance result",
            description = """
                    Retrieves the compliance result previously calculated
                    for a transaction.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Compliance result found",
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
                    responseCode = "401",
                    description = "Authentication required or JWT token is invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authenticated user does not have sufficient permissions",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compliance result not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<ComplianceResultResponse> getComplianceResult(
            @Parameter(
                    name = "transactionId",
                    description = "Internal transaction identifier",
                    required = true,
                    example = "transaction-1"
            )
            @PathVariable
            @NotBlank
            String transactionId
    ) {
        var result =
                getComplianceResultUseCase.execute(transactionId);

        return ResponseEntity.ok(
                ComplianceResultResponse.fromDomain(result)
        );
    }
}