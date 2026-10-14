package com.mansi.wallet_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mansi.wallet_system.dto.TransferRequest;
import com.mansi.wallet_system.entity.Wallet;
import com.mansi.wallet_system.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WalletControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void transferMoney_apiCall() throws Exception {
        Wallet sender = new Wallet();
        sender.setOwnerName("Sender User");
        sender.setBalance(new BigDecimal("500.00"));
        sender = walletRepository.save(sender);

        Wallet receiver = new Wallet();
        receiver.setOwnerName("Receiver User");
        receiver.setBalance(new BigDecimal("100.00"));
        receiver = walletRepository.save(receiver);

        TransferRequest request = new TransferRequest();
        request.setSenderWalletId(sender.getId());
        request.setReceiverWalletId(receiver.getId());
        request.setAmount(new BigDecimal("50.00"));

        mockMvc.perform(post("/wallets/transfer")
                        .header("Idempotency-Key", "integration-test-" + System.currentTimeMillis())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }
}
