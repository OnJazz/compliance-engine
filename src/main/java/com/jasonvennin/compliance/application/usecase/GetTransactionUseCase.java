package com.jasonvennin.compliance.application.usecase;

import com.jasonvennin.compliance.application.exception.TransactionNotFoundException;
import com.jasonvennin.compliance.application.port.TransactionRepository;
import com.jasonvennin.compliance.transaction.domain.Transaction;
import org.springframework.stereotype.Service;

@Service
public class GetTransactionUseCase {

    private final TransactionRepository transactionRepository;

    public GetTransactionUseCase(
            TransactionRepository transactionRepository
    ) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction execute(String transactionId) {
        return transactionRepository
                .findById(transactionId)
                .orElseThrow(() ->
                        new TransactionNotFoundException(transactionId)
                );
    }
}