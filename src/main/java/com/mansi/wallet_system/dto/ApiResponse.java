package com.mansi.wallet_system.dto;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

public class ApiResponse<T> {
    @Schema(example = "true")
    private boolean success;

    @Schema(example = "Transactions fetched successfully")
    private String message;

    private T data;

    @Schema(example = "2026-07-31T17:45:00")
    private LocalDateTime timestamp;


    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
