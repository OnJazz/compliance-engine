package com.jasonvennin.compliance.api.controller;

import com.jasonvennin.compliance.api.dto.CreateTransactionRequest;
import com.jasonvennin.compliance.api.dto.TransactionResponse;
import com.jasonvennin.compliance.application.usecase.CreateTransactionUseCase;
import com.jasonvennin.compliance.application.usecase.GetTransactionUseCase;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final GetTransactionUseCase getTransactionUseCase;

    public TransactionController(
            CreateTransactionUseCase createTransactionUseCase,
            GetTransactionUseCase getTransactionUseCase
    ) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.getTransactionUseCase = getTransactionUseCase;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        Transaction transaction =
                createTransactionUseCase.execute(request);

        return ResponseEntity
                .status(201)
                .body(TransactionResponse.from(transaction));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> get(
            @PathVariable String transactionId
    ) {
        Transaction transaction =
                getTransactionUseCase.execute(transactionId);

        return ResponseEntity.ok(
                TransactionResponse.from(transaction)
        );
    }
}