package com.minipay.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.minipay.dto.CreateUserRequest;
import com.minipay.dto.TransactionResponse;
import com.minipay.dto.UserResponse;
import com.minipay.dto.WalletResponse;
import com.minipay.exception.InsufficientFundsException;
import com.minipay.exception.SameWalletTransferException;
import com.minipay.exception.WalletNotFoundException;
import com.minipay.model.Transaction;
import com.minipay.model.TransactionStatus;
import com.minipay.model.TransactionType;
import com.minipay.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class WalletServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void depositAndTransferUpdatesBalances() {
        UserResponse sender = userService.createUser(new CreateUserRequest("Amina", "amina@test.com"));
        UserResponse receiver = userService.createUser(new CreateUserRequest("Dias", "dias@test.com"));

        walletService.deposit(sender.id(), new BigDecimal("100.00"));
        TransactionResponse transfer = walletService.transfer(sender.id(), receiver.id(), new BigDecimal("35.50"));

        WalletResponse senderWallet = walletService.getBalance(sender.id());
        WalletResponse receiverWallet = walletService.getBalance(receiver.id());

        assertThat(transfer.status()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(senderWallet.balance()).isEqualByComparingTo("64.50");
        assertThat(receiverWallet.balance()).isEqualByComparingTo("35.50");
    }

    @Test
    void transferFailsWhenBalanceIsTooLow() {
        UserResponse sender = userService.createUser(new CreateUserRequest("Mira", "mira@test.com"));
        UserResponse receiver = userService.createUser(new CreateUserRequest("Nurlan", "nurlan@test.com"));

        assertThatThrownBy(() -> walletService.transfer(sender.id(), receiver.id(), new BigDecimal("10.00")))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("Insufficient balance");

        assertThat(transactionRepository.findByFromUserIdOrToUserIdOrderByCreatedAtDesc(sender.id(), sender.id()))
                .anySatisfy(transaction -> {
                    assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.FAILED);
                    assertThat(transaction.getDescription()).isEqualTo("Insufficient balance");
                });
        assertThat(walletService.getBalance(sender.id()).balance()).isEqualByComparingTo("0.00");
        assertThat(walletService.getBalance(receiver.id()).balance()).isEqualByComparingTo("0.00");
    }

    @Test
    void transferFailsWhenSendingToSameWallet() {
        UserResponse user = userService.createUser(new CreateUserRequest("Sara", "sara@test.com"));
        walletService.deposit(user.id(), new BigDecimal("50.00"));

        assertThatThrownBy(() -> walletService.transfer(user.id(), user.id(), new BigDecimal("10.00")))
                .isInstanceOf(SameWalletTransferException.class)
                .hasMessage("Cannot transfer money to the same wallet");

        assertThat(transactionRepository.findByFromUserIdOrToUserIdOrderByCreatedAtDesc(user.id(), user.id()))
                .anySatisfy(transaction -> {
                    assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.FAILED);
                    assertThat(transaction.getDescription()).isEqualTo("Cannot transfer money to the same wallet");
                });
        assertThat(walletService.getBalance(user.id()).balance()).isEqualByComparingTo("50.00");
    }

    @Test
    void balanceFailsWhenWalletDoesNotExist() {
        assertThatThrownBy(() -> walletService.getBalance(999L))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessage("Wallet not found for user id 999");
    }

    @Test
    void transactionHistoryIsReturnedNewestFirst() {
        UserResponse sender = userService.createUser(new CreateUserRequest("Timur", "timur@test.com"));
        UserResponse receiver = userService.createUser(new CreateUserRequest("Dana", "dana@test.com"));

        walletService.deposit(sender.id(), new BigDecimal("100.00"));
        walletService.transfer(sender.id(), receiver.id(), new BigDecimal("20.00"));

        List<Transaction> history = transactionRepository
                .findByFromUserIdOrToUserIdOrderByCreatedAtDesc(sender.id(), sender.id());

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(history.get(1).getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(history.get(0).getCreatedAt()).isAfterOrEqualTo(history.get(1).getCreatedAt());
    }
}
