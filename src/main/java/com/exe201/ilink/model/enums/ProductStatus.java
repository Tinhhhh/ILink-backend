package com.exe201.ilink.model.enums;

import lombok.Getter;

@Getter
public enum ProductStatus {
    PENDING("PENDING"),
    ACTIVE("ACTIVE"),
    REJECTED("REJECTED"),
    CLOSED("CLOSED");

    private final String status;

    ProductStatus(String status) {
        this.status = status;
    }

    public static boolean isContains(String text) {
        for (ProductStatus b : ProductStatus.values()) {
            if (b.status.equalsIgnoreCase(text)) {
                return true;
            }
        }
        return false;
    }
}
