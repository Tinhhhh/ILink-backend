package com.exe201.ilink.service.Impl;

import com.exe201.ilink.enums.EmailTemplateName;
import com.exe201.ilink.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;


@Service
@RequiredArgsConstructor
public class EmailServiceImplement implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendEmail(
            String to,
            String accountName,
            EmailTemplateName emailTemplateName,
            String confirmationUrl,
            String activationCode,
            String subject) {

        String  senderName = "Customer Service Team at ILink";
        String templateName;
        if (emailTemplateName == null) {
            templateName = "confirm-email";
        } else {
            templateName = emailTemplateName.getName();
        }

        MimeMessage mimeMessage = mailSender.createMimeMessage();

    }
}
