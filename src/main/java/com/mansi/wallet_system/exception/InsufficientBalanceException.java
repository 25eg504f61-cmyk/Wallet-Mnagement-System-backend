package com.mansi.wallet_system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(Long walletId, java.math.BigDecimal requested, java.math.BigDecimal available) {
        super("Insufficient balance in wallet " + walletId
                + ": requested=" + requested.toPlainString()
                + ", available=" + available.toPlainString());
    }
}
