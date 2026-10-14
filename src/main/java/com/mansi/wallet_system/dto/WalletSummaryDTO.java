package com.mansi.wallet_system.dto;

import com.mansi.wallet_system.entity.Wallet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletSummaryDTO {
    private Wallet wallet;
    private BigDecimal totalReceived;
    private BigDecimal totalSent;
}
