package com.exe201.ilink.model.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PAID("PAID"),
    PENDING("PENDING"),
    PROCESSING("PROCESSING"),
    CANCELLED("CANCELLED");

    private final String status;

    PaymentStatus(String status) {
        this.status = status;
    }

    public static boolean isContains(String text) {
        for (PaymentStatus p : PaymentStatus.values()) {
            if (p.status.equalsIgnoreCase(text)) {
                return true;
            }
        }
        return false;
    }
}
