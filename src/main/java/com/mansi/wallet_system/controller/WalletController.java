package com.mansi.wallet_system.controller;

import com.mansi.wallet_system.dto.ApiResponse;
import com.mansi.wallet_system.dto.TransferRequest;
import com.mansi.wallet_system.dto.DepositRequest;
import com.mansi.wallet_system.dto.WalletSummaryDTO;
import com.mansi.wallet_system.entity.Transaction;
import com.mansi.wallet_system.entity.Wallet;
import com.mansi.wallet_system.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    // Create wallet
    @PostMapping
    public ApiResponse<Wallet> createWallet(@Valid @RequestBody Wallet wallet) {
        Wallet savedWallet = walletService.createWallet(wallet);

        return new ApiResponse<>(
                true,
                "Wallet created successfully",
                savedWallet
        );
    }

    // Get all wallets
    @GetMapping
    public ApiResponse<List<Wallet>> getAllWallets() {
        List<Wallet> wallets = walletService.getAllWallets();

        return new ApiResponse<>(
                true,
                "Wallets fetched successfully",
                wallets
        );
    }

    // Get wallet by id
    @GetMapping("/{id}")
    public ApiResponse<Wallet> getWalletById(@PathVariable Long id) {
        Wallet wallet = walletService.getWalletById(id);

        return new ApiResponse<>(
                true,
                "Wallet fetched successfully",
                wallet
        );
    }

    // Get wallet by userId
    @GetMapping("/user/{userId}")
    public ApiResponse<Wallet> getWalletByUserId(@PathVariable Long userId) {
        Wallet wallet = walletService.getWalletByUserId(userId);

        return new ApiResponse<>(
                true,
                "Wallet fetched successfully",
                wallet
        );
    }

    // Get wallet summary (including total received and total sent) by userId
    @GetMapping("/user/{userId}/summary")
    public ApiResponse<WalletSummaryDTO> getWalletSummaryByUserId(@PathVariable Long userId) {
        WalletSummaryDTO summary = walletService.getWalletSummaryByUserId(userId);

        return new ApiResponse<>(
                true,
                "Wallet summary fetched successfully",
                summary
        );
    }

    // Transfer money
    @PostMapping("/transfer")
    public ApiResponse<Transaction> transferMoney(
            @RequestHeader(value = "Idempotency-Key", required = true)
            String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {

        Transaction result = walletService.transferMoney(
                request.getSenderWalletId(),
                request.getReceiverWalletId(),
                request.getAmount(),
                idempotencyKey
        );

        return new ApiResponse<>(
                true,
                "Transfer completed successfully",
                result
        );
    }

    // Deposit money (Mock Payment Gateway Webhook)
    @PostMapping("/{id}/deposit")
    public ApiResponse<Transaction> depositMoney(
            @PathVariable Long id,
            @Valid @RequestBody DepositRequest request) {

        Transaction result = walletService.depositMoney(
                id,
                request.getAmount(),
                request.getProvider(),
                request.getExternalTransactionId()
        );

        return new ApiResponse<>(
                true,
                "Deposit completed successfully",
                result
        );
    }
}
