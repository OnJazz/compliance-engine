package com.jasonvennin.compliance.api.controller;

import com.jasonvennin.compliance.api.dto.CreateTransactionRequest;
import com.jasonvennin.compliance.api.dto.TransactionPageResponse;
import com.jasonvennin.compliance.api.dto.TransactionResponse;
import com.jasonvennin.compliance.application.usecase.CreateTransactionUseCase;
import com.jasonvennin.compliance.application.usecase.GetTransactionUseCase;
import com.jasonvennin.compliance.application.usecase.GetTransactionsUseCase;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final GetTransactionUseCase getTransactionUseCase;
    private final GetTransactionsUseCase getTransactionsUseCase;

    public TransactionController(
            CreateTransactionUseCase createTransactionUseCase,
            GetTransactionUseCase getTransactionUseCase,
            GetTransactionsUseCase getTransactionsUseCase
    ) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.getTransactionUseCase = getTransactionUseCase;
        this.getTransactionsUseCase = getTransactionsUseCase;
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

    @GetMapping
    public ResponseEntity<TransactionPageResponse> getAll(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort.Direction sortDirection =
                Sort.Direction.fromString(direction);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        Page<Transaction> transactions =
                getTransactionsUseCase.execute(
                        customerId,
                        status,
                        type,
                        pageable
                );

        return ResponseEntity.ok(
                TransactionPageResponse.from(transactions)
        );
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