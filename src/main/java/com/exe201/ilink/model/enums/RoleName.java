package com.exe201.ilink.model.enums;

import lombok.Getter;

@Getter
public enum RoleName {
    ADMIN("ADMIN"),
    MANAGER("MANAGER"),
    SELLER("SELLER"),
    BUYER("BUYER");

    private final String roleName;

    RoleName(String roleName) {
        this.roleName = roleName;
    }
}
