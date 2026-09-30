package com.jasonvennin.compliance.application.usecase;

import com.jasonvennin.compliance.application.exception.CustomerNotFoundException;
import com.jasonvennin.compliance.application.exception.TransactionNotFoundException;
import com.jasonvennin.compliance.application.port.ComplianceResultRepository;
import com.jasonvennin.compliance.application.port.CustomerRepository;
import com.jasonvennin.compliance.application.port.TransactionRepository;
import com.jasonvennin.compliance.compliance.domain.ComplianceEngine;
import com.jasonvennin.compliance.compliance.domain.ComplianceResult;
import com.jasonvennin.compliance.customer.domain.Customer;
import com.jasonvennin.compliance.transaction.domain.Transaction;

public class EvaluateTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final ComplianceResultRepository complianceResultRepository;
    private final ComplianceEngine complianceEngine;

    public EvaluateTransactionUseCase(TransactionRepository transactionRepository, CustomerRepository customerRepository, ComplianceResultRepository complianceResultRepository, ComplianceEngine complianceEngine) {
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
        this.complianceResultRepository = complianceResultRepository;
        this.complianceEngine = complianceEngine;
    }

    public ComplianceResult evaluate(String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new TransactionNotFoundException(transactionId));
        Customer customer = customerRepository.findById(transaction.customerId()).orElseThrow(() -> new CustomerNotFoundException(transaction.customerId()));
        ComplianceResult result = complianceEngine.evaluate(transaction, customer);
        complianceResultRepository.save(result);
        return result;
    }
}
