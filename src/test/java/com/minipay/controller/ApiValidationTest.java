package com.minipay.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.minipay.dto.CreateUserRequest;
import com.minipay.dto.UserResponse;
import com.minipay.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "minipay")
class ApiValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    void createUserRejectsBlankEmail() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Amina",
                                  "email": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fields.email").exists());
    }

    @Test
    void createUserRejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Amina",
                                  "email": "not-an-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fields.email").exists());
    }

    @Test
    void createUserRejectsDuplicateEmail() throws Exception {
        String body = """
                {
                  "name": "Amina",
                  "email": "duplicate@example.com"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }

    @Test
    void transferRejectsNegativeAmount() throws Exception {
        mockMvc.perform(post("/api/wallets/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromUserId": 1,
                                  "toUserId": 2,
                                  "amount": -10.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fields.amount").exists());
    }

    @Test
    void transferRejectsNegativeUserId() throws Exception {
        mockMvc.perform(post("/api/wallets/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromUserId": -1,
                                  "toUserId": 2,
                                  "amount": 10.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fields.fromUserId").exists());
    }

    @Test
    void balanceRejectsNegativeUserId() throws Exception {
        mockMvc.perform(get("/api/wallets/-1/balance"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void balanceReturnsNotFoundForMissingWallet() throws Exception {
        mockMvc.perform(get("/api/wallets/999/balance"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Wallet not found for user id 999"));
    }

    @Test
    @WithAnonymousUser
    void apiRequestsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/wallets/1/balance"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void depositReturnsConflictWhenIdempotencyKeyIsReusedForDifferentRequest() throws Exception {
        UserResponse user = userService.createUser(
                new CreateUserRequest("Ida", "ida-idempotency@test.com"));

        mockMvc.perform(post("/api/wallets/{userId}/deposit", user.id())
                        .header("Idempotency-Key", "api-conflict-key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 10.00
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/wallets/{userId}/deposit", user.id())
                        .header("Idempotency-Key", "api-conflict-key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 20.00
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Idempotency key was already used for a different request"));
    }
}
