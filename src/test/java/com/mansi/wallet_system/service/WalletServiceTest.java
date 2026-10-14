package com.mansi.wallet_system.service;

import com.mansi.wallet_system.entity.Transaction;
import com.mansi.wallet_system.entity.Wallet;
import com.mansi.wallet_system.exception.InsufficientBalanceException;
import com.mansi.wallet_system.exception.WalletNotFoundException;
import com.mansi.wallet_system.repository.LedgerEntryRepository;
import com.mansi.wallet_system.repository.TransactionRepository;
import com.mansi.wallet_system.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @InjectMocks
    private WalletService walletService;

    @Test
    void testTransferMoneySuccess() {

        Wallet sender = new Wallet();
        sender.setId(1L);
        sender.setBalance(new BigDecimal("5000.00"));

        Wallet receiver = new Wallet();
        receiver.setId(2L);
        receiver.setBalance(new BigDecimal("2000.00"));

        when(walletRepository.findByIdWithLock(1L)).thenReturn(Optional.of(sender));
        when(walletRepository.findByIdWithLock(2L)).thenReturn(Optional.of(receiver));

        Transaction mockTransaction = new Transaction();
        mockTransaction.setId(1L);
        mockTransaction.setStatus("SUCCESS");
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);

        Transaction result = walletService.transferMoney(sender.getId(), receiver.getId(), new BigDecimal("1000.00"), "test-idempotency-key");

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(new BigDecimal("4000.00"), sender.getBalance());
        assertEquals(new BigDecimal("3000.00"), receiver.getBalance());

        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(ledgerEntryRepository, times(2)).save(any());
    }

    @Test
    void testTransferMoney_InsufficientBalance() {

        Wallet sender = new Wallet();
        sender.setId(1L);
        sender.setBalance(new BigDecimal("500.00"));

        Wallet receiver = new Wallet();
        receiver.setId(2L);
        receiver.setBalance(new BigDecimal("2000.00"));

        when(walletRepository.findByIdWithLock(1L)).thenReturn(Optional.of(sender));
        when(walletRepository.findByIdWithLock(2L)).thenReturn(Optional.of(receiver));

        assertThrows(InsufficientBalanceException.class, () ->
                walletService.transferMoney(sender.getId(), receiver.getId(), new BigDecimal("1000.00"), "test-idempotency-key")
        );

        // Balances must remain unchanged
        assertEquals(new BigDecimal("500.00"), sender.getBalance());
        assertEquals(new BigDecimal("2000.00"), receiver.getBalance());
    }

    @Test
    void testTransferMoney_SelfTransfer() {

        assertThrows(IllegalArgumentException.class, () ->
                walletService.transferMoney(1L, 1L, new BigDecimal("100.00"), "test-idempotency-key")
        );
    }

    @Test
    void testTransferMoney_WalletNotFound() {

        when(walletRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () ->
                walletService.transferMoney(1L, 2L, new BigDecimal("100.00"), "test-key")
        );
    }

    @Test
    void testDepositMoney_Success() {
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setBalance(new BigDecimal("100.00"));

        when(walletRepository.findByIdWithLock(1L)).thenReturn(Optional.of(wallet));

        Transaction mockTransaction = new Transaction();
        mockTransaction.setId(10L);
        mockTransaction.setStatus("SUCCESS");
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);

        Transaction result = walletService.depositMoney(1L, new BigDecimal("500.00"), "Razorpay", "ext-tx-123");

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(new BigDecimal("600.00"), wallet.getBalance());

        verify(walletRepository, times(1)).save(wallet);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(ledgerEntryRepository, times(1)).save(any());
    }

    @Test
    void testDepositMoney_NegativeAmount() {
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setBalance(new BigDecimal("100.00"));

        when(walletRepository.findByIdWithLock(1L)).thenReturn(Optional.of(wallet));

        assertThrows(IllegalArgumentException.class, () ->
                walletService.depositMoney(1L, new BigDecimal("-50.00"), "Razorpay", "ext-tx-123")
        );
    }
}
