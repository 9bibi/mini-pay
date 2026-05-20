package com.minipay.service;

import com.minipay.dto.TransactionResponse;
import com.minipay.dto.WalletResponse;
import com.minipay.exception.BadRequestException;
import com.minipay.exception.ResourceNotFoundException;
import com.minipay.model.Transaction;
import com.minipay.model.TransactionStatus;
import com.minipay.model.TransactionType;
import com.minipay.model.Wallet;
import com.minipay.repository.TransactionRepository;
import com.minipay.repository.WalletRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public WalletResponse getBalance(Long userId) {
        return WalletResponse.from(findWalletByUserId(userId));
    }

    @Transactional
    public TransactionResponse deposit(Long userId, BigDecimal amount) {
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

    @Transactional
    public TransactionResponse transfer(Long fromUserId, Long toUserId, BigDecimal amount) {
        if (fromUserId.equals(toUserId)) {
            return saveFailedTransfer(fromUserId, toUserId, amount, "Cannot transfer money to the same wallet");
        }

        Wallet fromWallet = findWalletByUserId(fromUserId);
        Wallet toWallet = findWalletByUserId(toUserId);

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            return saveFailedTransfer(fromUserId, toUserId, amount, "Insufficient balance");
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
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user id " + userId));
    }
}
