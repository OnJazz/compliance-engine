package com.jasonvennin.compliance.application.usecase;

import com.jasonvennin.compliance.application.exception.InvalidTransactionFilterException;
import com.jasonvennin.compliance.application.port.TransactionRepository;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import com.jasonvennin.compliance.transaction.domain.TransactionStatus;
import com.jasonvennin.compliance.transaction.domain.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class GetTransactionsUseCase {

    private final TransactionRepository transactionRepository;

    public GetTransactionsUseCase(
            TransactionRepository transactionRepository
    ) {
        this.transactionRepository = transactionRepository;
    }

    public Page<Transaction> execute(
            String customerId,
            String status,
            String type,
            Pageable pageable
    ) {
        TransactionStatus transactionStatus =
                parseStatus(status);

        TransactionType transactionType =
                parseType(type);

        return transactionRepository.findAll(
                customerId,
                transactionStatus,
                transactionType,
                pageable
        );
    }

    private TransactionStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return TransactionStatus.valueOf(
                    status.toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw new InvalidTransactionFilterException(
                    "status",
                    status
            );
        }
    }

    private TransactionType parseType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }

        try {
            return TransactionType.valueOf(
                    type.toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw new InvalidTransactionFilterException(
                    "type",
                    type
            );
        }
    }
}