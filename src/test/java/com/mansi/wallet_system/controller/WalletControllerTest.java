package com.mansi.wallet_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mansi.wallet_system.dto.TransferRequest;
import com.mansi.wallet_system.entity.Transaction;
import com.mansi.wallet_system.entity.Wallet;
import com.mansi.wallet_system.security.SecurityConfig;
import com.mansi.wallet_system.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
@Import(SecurityConfig.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WalletService walletService;

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testCreateWallet() throws Exception {

        Wallet wallet = new Wallet();
        wallet.setOwnerName("Mansi");
        wallet.setBalance(new BigDecimal("5000.00"));

        when(walletService.createWallet(any(Wallet.class))).thenReturn(wallet);

        mockMvc.perform(post("/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wallet)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ownerName").value("Mansi"))
                .andExpect(jsonPath("$.data.balance").value(5000.00));
    }

    @Test
    void testGetAllWallets() throws Exception {

        Wallet wallet = new Wallet();
        wallet.setOwnerName("Mansi");
        wallet.setBalance(new BigDecimal("5000.00"));

        when(walletService.getAllWallets()).thenReturn(Arrays.asList(wallet));

        mockMvc.perform(get("/wallets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].ownerName").value("Mansi"));
    }

    @Test
    void testTransferMoney() throws Exception {

        TransferRequest request = new TransferRequest();
        request.setSenderWalletId(1L);
        request.setReceiverWalletId(2L);
        request.setAmount(new BigDecimal("1000.00"));

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setFromWalletId(1L);
        transaction.setToWalletId(2L);
        transaction.setAmount(new BigDecimal("1000.00"));
        transaction.setStatus("SUCCESS");

        when(walletService.transferMoney(any(Long.class), any(Long.class), any(BigDecimal.class), any(String.class)))
                .thenReturn(transaction);

        mockMvc.perform(post("/wallets/transfer")
                        .header("Idempotency-Key", "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }
}
