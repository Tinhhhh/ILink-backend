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

    public static boolean contains(String status) {
        for (PostStatus ps : PostStatus.values()) {
            if (ps.status.equals(status)) {
                return true;
            }
        }
        return false;
    }

}
