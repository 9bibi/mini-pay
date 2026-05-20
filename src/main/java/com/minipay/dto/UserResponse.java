package com.minipay.dto;

import com.minipay.model.User;

public record UserResponse(
        Long id,
        String name,
        String email,
        Long walletId
) {
    public static UserResponse from(User user) {
        Long walletId = user.getWallet() == null ? null : user.getWallet().getId();
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), walletId);
    }
}
