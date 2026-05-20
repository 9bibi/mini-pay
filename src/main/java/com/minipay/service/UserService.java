package com.minipay.service;

import com.minipay.dto.CreateUserRequest;
import com.minipay.dto.UserResponse;
import com.minipay.exception.BadRequestException;
import com.minipay.model.User;
import com.minipay.model.Wallet;
import com.minipay.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already registered");
        }

        User user = new User(request.name().trim(), email);
        Wallet wallet = new Wallet(user);
        user.setWallet(wallet);

        return UserResponse.from(userRepository.save(user));
    }
}
