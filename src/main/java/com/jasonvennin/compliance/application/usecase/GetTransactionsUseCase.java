package com.jasonvennin.compliance.application.usecase;

import com.jasonvennin.compliance.application.port.TransactionRepository;
import com.jasonvennin.compliance.transaction.domain.Transaction;
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
        return transactionRepository.findAll(
                customerId,
                status,
                type,
                pageable
        );
    }
}