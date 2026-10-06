package com.jasonvennin.compliance.api.dto;

import com.jasonvennin.compliance.transaction.domain.Transaction;
import org.springframework.data.domain.Page;

import java.util.List;

public record TransactionPageResponse(
        List<TransactionResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static TransactionPageResponse from(
            Page<Transaction> page
    ) {
        return new TransactionPageResponse(
                page.getContent()
                        .stream()
                        .map(TransactionResponse::from)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}