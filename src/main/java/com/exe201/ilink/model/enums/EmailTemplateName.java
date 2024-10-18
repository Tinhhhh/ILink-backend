package com.exe201.ilink.model.enums;

import lombok.Getter;

@Getter
public enum EmailTemplateName {
    ACTIVATE_ACCOUNT("ACTIVATE_ACCOUNT"),
    FORGOT_PASSWORD("FORGOT_PASSWORD"),
    INFORM_SHOP_OWNER("INFORM_SHOP_OWNER"),
    NEW_ORDER_INFORM("NEW_ORDER_INFORM"),
    ORDER_INFORM("ORDER_INFORM");

    private final String name;

    EmailTemplateName(String name) {
        this.name = name;
    }
}
