package com.mansi.wallet_system.service;

import com.mansi.wallet_system.entity.Transaction;
import com.mansi.wallet_system.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void testSaveTransaction() {
        Transaction tx = new Transaction();
        tx.setAmount(new BigDecimal("100.00"));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction saved = transactionService.saveTransaction(tx);
        assertNotNull(saved);
        assertNotNull(saved.getTransactionTime());
        verify(transactionRepository, times(1)).save(tx);
    }

    @Test
    void testGetTransactionsByWalletId() {
        Transaction tx1 = new Transaction();
        tx1.setId(1L);
        tx1.setFromWalletId(10L);
        tx1.setToWalletId(20L);

        Page<Transaction> page = new PageImpl<>(List.of(tx1));
        when(transactionRepository.findByFromWalletIdOrToWalletId(eq(10L), eq(10L), any(Pageable.class)))
                .thenReturn(page);

        List<Transaction> result = transactionService.getTransactionsByWalletId(10L);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
}
