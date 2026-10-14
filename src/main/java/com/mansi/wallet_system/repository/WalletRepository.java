package com.mansi.wallet_system.repository;

import com.mansi.wallet_system.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdWithLock(@org.springframework.data.repository.query.Param("id") Long id);

    Optional<Wallet> findByUserId(Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(w.balance) FROM Wallet w")
    java.math.BigDecimal getTotalSystemBalance();
}
