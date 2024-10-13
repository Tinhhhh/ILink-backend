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

    public static boolean contains(String role) {
        for (RoleName rn : RoleName.values()) {
            if (rn.roleName.equals(role)) {
                return true;
            }
        }
        return false;
    }
}
