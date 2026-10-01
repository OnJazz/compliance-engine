package com.jasonvennin.compliance.application.usecase;

import com.jasonvennin.compliance.api.dto.CreateTransactionRequest;
import com.jasonvennin.compliance.application.exception.TransactionAlreadyExistsException;
import com.jasonvennin.compliance.application.port.TransactionRepository;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CreateTransactionUseCase {

    private final TransactionRepository transactionRepository;

    public CreateTransactionUseCase(
            TransactionRepository transactionRepository
    ) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction execute(CreateTransactionRequest request) {

        if (transactionRepository.findById(request.id()).isPresent()) {
            throw new TransactionAlreadyExistsException(request.id());
        }

        Transaction transaction = new Transaction(
                request.id(),
                request.externalId(),
                request.customerId(),
                request.amount(),
                request.currency(),
                request.type(),
                request.country(),
                request.beneficiaryCountry(),
                Instant.now(),
                TransactionStatus.PENDING
        );

        return transactionRepository.save(transaction);
    }
}