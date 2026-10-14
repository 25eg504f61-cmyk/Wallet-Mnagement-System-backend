package com.mansi.wallet_system.repository;
import com.mansi.wallet_system.entity.LedgerEntry;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM LedgerEntry l WHERE l.wallet.id = :walletId AND l.type = :type")
    BigDecimal getTotalAmountByTypeAndWalletId(@Param("walletId") Long walletId, @Param("type") String type);
}
