package com.exe201.ilink.service;

import com.exe201.ilink.model.enums.EmailTemplateName;
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
    ) throws MessagingException, UnsupportedEncodingException;

    void sendSimpleEmail(String name, String to, String token);

    void sendMimeMessageWithEmbeddedFiles(String name, String to,String code) throws MessagingException;

    void sendMimeMessageWithHtml(String name, String to,String code) throws MessagingException;
}
