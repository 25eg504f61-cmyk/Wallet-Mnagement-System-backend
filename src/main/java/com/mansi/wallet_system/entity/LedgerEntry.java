package com.mansi.wallet_system.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    private String type; // CREDIT or DEBIT

    private String description;

    private Long transactionId;

    private LocalDateTime transactionTime;

    @ManyToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;
}
