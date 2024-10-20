package com.exe201.ilink.service;

import com.exe201.ilink.model.enums.EmailTemplateName;
import com.exe201.ilink.model.payload.dto.OrderProductDTO;
import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;
import java.util.List;

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

    void sendMimeMessageWithEmbeddedFiles(String name, String to, String code) throws MessagingException;

    void sendMimeMessageWithHtml(String name, String to, String content, String template, String subject) throws MessagingException;

    void sendMimeMessageForSeller(String seller, String buyer, String sellerEmail, String date, String time, String code, List<OrderProductDTO> product, int totalPrice, String customer, String phone, String address, String template, String subject) throws MessagingException;
}
