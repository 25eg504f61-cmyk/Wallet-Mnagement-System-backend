package com.mansi.wallet_system.controller;

import com.mansi.wallet_system.entity.LedgerEntry;
import com.mansi.wallet_system.repository.LedgerEntryRepository;
import com.mansi.wallet_system.repository.WalletRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public AdminController(WalletRepository walletRepository, LedgerEntryRepository ledgerEntryRepository) {
        this.walletRepository = walletRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @GetMapping("/system-summary")
    public Map<String, Object> getSystemSummary() {
        long totalWallets = walletRepository.count();
        BigDecimal totalBalance = walletRepository.getTotalSystemBalance();
        
        if (totalBalance == null) {
            totalBalance = BigDecimal.ZERO;
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalWallets", totalWallets);
        summary.put("totalBalance", totalBalance);
        return summary;
    }

    @GetMapping("/all-transactions")
    public List<LedgerEntry> getAllTransactions() {
        return ledgerEntryRepository.findAll();
    }
}
