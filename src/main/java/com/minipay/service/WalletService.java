package com.minipay.service;

import com.minipay.dto.TransactionResponse;
import com.minipay.dto.WalletResponse;
import com.minipay.exception.BadRequestException;
import com.minipay.exception.IdempotencyConflictException;
import com.minipay.exception.InsufficientFundsException;
import com.minipay.exception.SameWalletTransferException;
import com.minipay.exception.WalletNotFoundException;
import com.minipay.model.IdempotencyKey;
import com.minipay.model.Transaction;
import com.minipay.model.TransactionStatus;
import com.minipay.model.TransactionType;
import com.minipay.model.Wallet;
import com.minipay.repository.IdempotencyKeyRepository;
import com.minipay.repository.TransactionRepository;
import com.minipay.repository.WalletRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public WalletService(WalletRepository walletRepository, TransactionRepository transactionRepository,
                         IdempotencyKeyRepository idempotencyKeyRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    @Transactional(readOnly = true)
    public WalletResponse getBalance(Long userId) {
        return WalletResponse.from(findWalletByUserId(userId));
    }

    @Transactional
    public TransactionResponse deposit(Long userId, BigDecimal amount) {
        return deposit(userId, amount, null);
    }

    @Transactional
    public TransactionResponse deposit(Long userId, BigDecimal amount, String idempotencyKey) {
        String requestHash = requestHash("DEPOSIT", userId, null, amount);
        return executeIdempotently(idempotencyKey, requestHash, () -> performDeposit(userId, amount));
    }

    private TransactionResponse performDeposit(Long userId, BigDecimal amount) {
        Wallet wallet = findWalletByUserId(userId);
        wallet.deposit(amount);

        Transaction transaction = new Transaction(
                null,
                userId,
                amount,
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCESS,
                "Deposit to wallet"
        );

        walletRepository.save(wallet);
        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional(noRollbackFor = {InsufficientFundsException.class, SameWalletTransferException.class})
    public TransactionResponse transfer(Long fromUserId, Long toUserId, BigDecimal amount) {
        return transfer(fromUserId, toUserId, amount, null);
    }

    @Transactional(noRollbackFor = {InsufficientFundsException.class, SameWalletTransferException.class})
    public TransactionResponse transfer(Long fromUserId, Long toUserId, BigDecimal amount, String idempotencyKey) {
        String requestHash = requestHash("TRANSFER", fromUserId, toUserId, amount);
        return executeIdempotently(idempotencyKey, requestHash, () -> performTransfer(fromUserId, toUserId, amount));
    }

    private TransactionResponse performTransfer(Long fromUserId, Long toUserId, BigDecimal amount) {
        if (fromUserId.equals(toUserId)) {
            saveFailedTransfer(fromUserId, toUserId, amount, "Cannot transfer money to the same wallet");
            throw new SameWalletTransferException("Cannot transfer money to the same wallet");
        }

        Wallet fromWallet = findWalletByUserId(fromUserId);
        Wallet toWallet = findWalletByUserId(toUserId);

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            saveFailedTransfer(fromUserId, toUserId, amount, "Insufficient balance");
            throw new InsufficientFundsException("Insufficient balance");
        }

        fromWallet.withdraw(amount);
        toWallet.deposit(amount);

        Transaction transaction = new Transaction(
                fromUserId,
                toUserId,
                amount,
                TransactionType.TRANSFER,
                TransactionStatus.SUCCESS,
                "Transfer completed"
        );

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);
        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    private TransactionResponse executeIdempotently(String idempotencyKey, String requestHash,
                                                    Supplier<TransactionResponse> action) {
        Optional<String> normalizedKey = normalizeIdempotencyKey(idempotencyKey);
        if (normalizedKey.isEmpty()) {
            return action.get();
        }

        Optional<IdempotencyKey> existingKey = idempotencyKeyRepository.findByKeyValue(normalizedKey.get());
        if (existingKey.isPresent()) {
            IdempotencyKey existing = existingKey.get();
            if (!existing.getRequestHash().equals(requestHash)) {
                throw new IdempotencyConflictException("Idempotency key was already used for a different request");
            }
            return transactionRepository.findById(existing.getTransactionId())
                    .map(TransactionResponse::from)
                    .orElseThrow(() -> new IllegalStateException("Idempotent transaction was not found"));
        }

        TransactionResponse response = action.get();
        idempotencyKeyRepository.save(new IdempotencyKey(normalizedKey.get(), requestHash, response.id()));
        return response;
    }

    private Optional<String> normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null) {
            return Optional.empty();
        }

        String trimmedKey = idempotencyKey.trim();
        if (trimmedKey.isBlank()) {
            throw new BadRequestException("Idempotency-Key must not be blank");
        }
        if (trimmedKey.length() > 120) {
            throw new BadRequestException("Idempotency-Key must be 120 characters or fewer");
        }
        return Optional.of(trimmedKey);
    }

    private String requestHash(String operation, Long fromUserId, Long toUserId, BigDecimal amount) {
        String body = operation
                + ":" + fromUserId
                + ":" + toUserId
                + ":" + amount.stripTrailingZeros().toPlainString();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(body.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private TransactionResponse saveFailedTransfer(Long fromUserId, Long toUserId, BigDecimal amount, String reason) {
        Transaction transaction = new Transaction(
                fromUserId,
                toUserId,
                amount,
                TransactionType.TRANSFER,
                TransactionStatus.FAILED,
                reason
        );
        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    private Wallet findWalletByUserId(Long userId) {
        if (userId == null) {
            throw new BadRequestException("User id is required");
        }
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user id " + userId));
    }
}
