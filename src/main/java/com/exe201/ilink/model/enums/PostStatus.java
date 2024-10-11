package com.exe201.ilink.model.enums;

import lombok.Getter;

@Getter
public enum PostStatus {
    HIDDEN("HIDDEN"),
    ACTIVE("ACTIVE"),
    CLOSED("CLOSED");

    private final String status;

    PostStatus(String status) {
        this.status = status;
    }
}
