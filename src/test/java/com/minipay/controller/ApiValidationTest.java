package com.minipay.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiValidationTest {

    @Autowired
    private MockMvc mockMvc;

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
}
