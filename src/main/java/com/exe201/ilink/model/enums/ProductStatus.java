package com.exe201.ilink.model.enums;

import lombok.Getter;

@Getter
public enum ProductStatus {
    PENDING("PENDING"),
    ACTIVE("ACTIVE"),
    REJECT("REJECT"),
    CLOSE("CLOSE");

    private final String status;

    ProductStatus(String status) {
        this.status = status;
    }
}
