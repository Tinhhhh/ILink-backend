package com.exe201.ilink.model.enums;

public enum PaymentStatus {
    PAID("PAID"),
    PENDING("PENDING"),
    PROCESSING("PROCESSING"),
    CANCELLED("CANCELLED");

    private final String status;

    PaymentStatus(String status) {
        this.status = status;
    }
}
