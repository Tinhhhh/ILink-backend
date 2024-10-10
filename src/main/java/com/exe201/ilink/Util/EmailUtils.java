package com.exe201.ilink.Util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EmailUtils {

    public static String activeCodeMessage(String name, String host, String code) {
        String msg = "Hello " + name + ",\n\n" +
            "Welcome to ILink! Please click the link below to activate your account:\n\n" +
            getVerificationUrl(host, code) + "\n\n" +
            "Thank you for choosing ILink!\n\n" +
            "Sincerely,\n" +
            "The ILink Team";
        return msg;
    }

    public static String getVerificationUrl(String host, String token) {
        return host + "?code=" + token;
    }
}
