package com.minipay.dto;

import com.minipay.model.TransactionStatus;
import com.minipay.model.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentEvent(
        String eventType,
        Long transactionId,
        Long fromUserId,
        Long toUserId,
        BigDecimal amount,
        TransactionType type,
        TransactionStatus status,
        Instant createdAt
) {
    public static PaymentEvent from(TransactionResponse transaction) {
        return new PaymentEvent(
                transaction.type() + "_" + transaction.status(),
                transaction.id(),
                transaction.fromUserId(),
                transaction.toUserId(),
                transaction.amount(),
                transaction.type(),
                transaction.status(),
                transaction.createdAt()
        );
    }
}
