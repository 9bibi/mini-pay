package com.minipay.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransferRequest(
        @NotNull @Positive Long fromUserId,
        @NotNull @Positive Long toUserId,
        @NotNull @Positive BigDecimal amount
) {
}
