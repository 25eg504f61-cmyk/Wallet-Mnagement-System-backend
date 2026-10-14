package com.mansi.wallet_system.service;

import com.mansi.wallet_system.dto.WalletSummaryDTO;
import com.mansi.wallet_system.entity.LedgerEntry;
import com.mansi.wallet_system.entity.Transaction;
import com.mansi.wallet_system.entity.Wallet;
import com.mansi.wallet_system.exception.InsufficientBalanceException;
import com.mansi.wallet_system.exception.WalletNotFoundException;
import com.mansi.wallet_system.repository.LedgerEntryRepository;
import com.mansi.wallet_system.repository.TransactionRepository;
import com.mansi.wallet_system.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public WalletService(WalletRepository walletRepository,
                         TransactionRepository transactionRepository,
                         LedgerEntryRepository ledgerEntryRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    public Wallet createWallet(Wallet wallet) {
        return walletRepository.save(wallet);
    }

    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    public Wallet getWalletById(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException(id));
    }

    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));
    }

    public Wallet updateWallet(Wallet wallet) {
        return walletRepository.save(wallet);
    }

    public WalletSummaryDTO getWalletSummaryByUserId(Long userId) {
        Wallet wallet = getWalletByUserId(userId);
        BigDecimal totalReceived = ledgerEntryRepository.getTotalAmountByTypeAndWalletId(wallet.getId(), "CREDIT");
        BigDecimal totalSent = ledgerEntryRepository.getTotalAmountByTypeAndWalletId(wallet.getId(), "DEBIT");
        return new WalletSummaryDTO(wallet, totalReceived, totalSent);
    }

    @Transactional
    public Transaction transferMoney(Long fromWalletId, Long toWalletId, BigDecimal amount, String idempotencyKey) {

        // Check Idempotency: If this exact request was already processed, return the cached result.
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            java.util.Optional<Transaction> existingTx = transactionRepository.findByIdempotencyKey(idempotencyKey);
            if (existingTx.isPresent()) {
                logger.info("Idempotency key matched! Returning existing transaction. Key: {}", idempotencyKey);
                return existingTx.get();
            }
        }

        // Guard: self-transfer
        if (fromWalletId.equals(toWalletId)) {
            throw new IllegalArgumentException("Cannot transfer money to the same wallet");
        }

        logger.info("Transfer started: fromWallet={}, toWallet={}, amount={}",
                fromWalletId, toWalletId, amount.toPlainString());

        // Lock wallets in ascending ID order to prevent deadlocks
        Long firstId = fromWalletId < toWalletId ? fromWalletId : toWalletId;
        Long secondId = fromWalletId < toWalletId ? toWalletId : fromWalletId;

        Wallet firstWallet = walletRepository.findByIdWithLock(firstId)
                .orElseThrow(() -> new WalletNotFoundException(firstId));
        Wallet secondWallet = walletRepository.findByIdWithLock(secondId)
                .orElseThrow(() -> new WalletNotFoundException(secondId));

        // Assign to logical sender/receiver after lock acquisition
        Wallet fromWallet = fromWalletId.equals(firstId) ? firstWallet : secondWallet;
        Wallet toWallet = toWalletId.equals(firstId) ? firstWallet : secondWallet;

        // Check sufficient balance
        if (fromWallet.getBalance().compareTo(amount) < 0) {
            logger.warn("Transfer failed: fromWallet={}, amount={}, reason=Insufficient balance",
                    fromWalletId, amount.toPlainString());
            throw new InsufficientBalanceException(fromWalletId, amount, fromWallet.getBalance());
        }

        // Debit sender, credit receiver
        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        // Record transaction
        LocalDateTime now = LocalDateTime.now();

        Transaction transaction = new Transaction();
        transaction.setFromWalletId(fromWalletId);
        transaction.setToWalletId(toWalletId);
        transaction.setAmount(amount);
        transaction.setStatus("SUCCESS");
        transaction.setTransactionTime(now);
        transaction.setIdempotencyKey(idempotencyKey);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Double-entry ledger
        LedgerEntry debitEntry = new LedgerEntry();
        debitEntry.setWallet(fromWallet);
        debitEntry.setAmount(amount);
        debitEntry.setType("DEBIT");
        debitEntry.setDescription("Transfer to wallet ID " + toWalletId);
        debitEntry.setTransactionId(savedTransaction.getId());
        debitEntry.setTransactionTime(now);
        ledgerEntryRepository.save(debitEntry);

        LedgerEntry creditEntry = new LedgerEntry();
        creditEntry.setWallet(toWallet);
        creditEntry.setAmount(amount);
        creditEntry.setType("CREDIT");
        creditEntry.setDescription("Transfer from wallet ID " + fromWalletId);
        creditEntry.setTransactionId(savedTransaction.getId());
        creditEntry.setTransactionTime(now);
        ledgerEntryRepository.save(creditEntry);

        logger.info("Transfer successful: txId={}, fromWallet={}, toWallet={}, amount={}",
                savedTransaction.getId(), fromWalletId, toWalletId, amount.toPlainString());

        return savedTransaction;
    }

    @Transactional
    public Transaction depositMoney(Long walletId, BigDecimal amount, String provider, String externalTransactionId) {
        Wallet wallet = walletRepository.findByIdWithLock(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        // Add money
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        LocalDateTime now = LocalDateTime.now();

        // Record a generic "System to Wallet" transaction. Since it's external, fromWalletId is null.
        Transaction transaction = new Transaction();
        transaction.setFromWalletId(null);
        transaction.setToWalletId(walletId);
        transaction.setAmount(amount);
        transaction.setStatus("SUCCESS");
        transaction.setTransactionTime(now);
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Record in ledger
        LedgerEntry creditEntry = new LedgerEntry();
        creditEntry.setWallet(wallet);
        creditEntry.setAmount(amount);
        creditEntry.setType("CREDIT");
        creditEntry.setDescription("Deposit via " + provider + " (Ref: " + externalTransactionId + ")");
        creditEntry.setTransactionId(savedTransaction.getId());
        creditEntry.setTransactionTime(now);
        ledgerEntryRepository.save(creditEntry);

        logger.info("Deposit successful: txId={}, walletId={}, amount={}, provider={}",
                savedTransaction.getId(), walletId, amount.toPlainString(), provider);

        return savedTransaction;
    }
}
