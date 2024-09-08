package com.exe201.ilink.service;

import com.exe201.ilink.enums.EmailTemplateName;
import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;

public interface EmailService {
    void sendEmail(
            String to,
            String accountName,
            EmailTemplateName emailTemplateName,
            String confirmationUrl,
            String activationCode,
            String subject
    );
}
