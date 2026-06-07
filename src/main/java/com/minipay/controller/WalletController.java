package com.minipay.controller;

import com.minipay.dto.DepositRequest;
import com.minipay.dto.TransactionResponse;
import com.minipay.dto.TransferRequest;
import com.minipay.dto.WalletResponse;
import com.minipay.service.WalletService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/{userId}/deposit")
    public TransactionResponse deposit(@PathVariable @Positive Long userId,
                                       @Valid @RequestBody DepositRequest request,
                                       @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return walletService.deposit(userId, request.amount(), idempotencyKey);
    }

    @PostMapping("/transfer")
    public TransactionResponse transfer(@Valid @RequestBody TransferRequest request,
                                        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return walletService.transfer(request.fromUserId(), request.toUserId(), request.amount(), idempotencyKey);
    }

    @GetMapping("/{userId}/balance")
    public WalletResponse balance(@PathVariable @Positive Long userId) {
        return walletService.getBalance(userId);
    }
}
