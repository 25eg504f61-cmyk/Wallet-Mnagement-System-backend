package com.mansi.wallet_system.controller;

import com.mansi.wallet_system.dto.ApiResponse;
import com.mansi.wallet_system.entity.Transaction;
import com.mansi.wallet_system.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Transaction>> createTransaction(@RequestBody Transaction transaction) {

        Transaction saved = transactionService.saveTransaction(transaction);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Transaction created successfully", saved)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Transaction>>> getAllTransactions(Pageable pageable) {

        Page<Transaction> transactions = transactionService.getAllTransactions(pageable);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Transactions fetched successfully", transactions)
        );
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<ApiResponse<List<Transaction>>> getTransactionsByWalletId(@PathVariable Long walletId) {

        List<Transaction> transactions = transactionService.getTransactionsByWalletId(walletId);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Wallet transactions fetched successfully", transactions)
        );
    }
}
