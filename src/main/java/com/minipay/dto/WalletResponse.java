package com.minipay.dto;

import com.minipay.model.Wallet;
import java.math.BigDecimal;

public record WalletResponse(
        Long walletId,
        Long userId,
        BigDecimal balance
) {
    public static WalletResponse from(Wallet wallet) {
        return new WalletResponse(wallet.getId(), wallet.getUser().getId(), wallet.getBalance());
    }
}
