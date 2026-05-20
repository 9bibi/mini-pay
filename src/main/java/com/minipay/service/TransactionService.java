package com.minipay.service;

import com.minipay.dto.TransactionResponse;
import com.minipay.repository.TransactionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getUserTransactions(Long userId) {
        return transactionRepository.findByFromUserIdOrToUserIdOrderByCreatedAtDesc(userId, userId)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }
}
