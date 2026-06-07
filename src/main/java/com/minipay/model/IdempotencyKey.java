package com.minipay.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key_value", nullable = false, unique = true, length = 120)
    private String keyValue;

    @Column(nullable = false, length = 64)
    private String requestHash;

    @Column(nullable = false)
    private Long transactionId;

    @Column(nullable = false)
    private Instant createdAt;

    protected IdempotencyKey() {
    }

    public IdempotencyKey(String keyValue, String requestHash, Long transactionId) {
        this.keyValue = keyValue;
        this.requestHash = requestHash;
        this.transactionId = transactionId;
        this.createdAt = Instant.now();
    }

    public String getKeyValue() {
        return keyValue;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public Long getTransactionId() {
        return transactionId;
    }
}
