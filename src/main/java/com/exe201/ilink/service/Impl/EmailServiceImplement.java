package com.exe201.ilink.service.Impl;

import com.exe201.ilink.model.enums.EmailTemplateName;
import com.exe201.ilink.model.payload.dto.OrderProductDTO;
import com.exe201.ilink.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.exe201.ilink.Util.EmailUtils.activeCodeMessage;


@Service
@RequiredArgsConstructor
public class EmailServiceImplement implements EmailService {

    public static final String UTF_8_CODING = "UTF-8";
    public static final String NEW_USER_ACCOUNT_ACTIVATION = "New User Account Activation";
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${application.email.active.url}")
    private String activationUrl;

    @Value("${spring.mail.username}")
    private String sender;

    @Async
    public void sendEmail(
        String to,
        String accountName,
        EmailTemplateName emailTemplateName,
        String confirmationUrl,
        String activationCode,
        String subject) throws MessagingException, UnsupportedEncodingException {

        String senderName = "Customer Service Team at ILink";
        String templateName;
        if (emailTemplateName == null) {
            templateName = "confirm-email";
        } else {
            templateName = emailTemplateName.getName();
        }

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
            mimeMessage,
            MimeMessageHelper.MULTIPART_MODE_MIXED,
            StandardCharsets.UTF_8.name()
        );

        Map<String, Object> properties = new HashMap<>();
        properties.put("accountName", accountName);
        properties.put("confirmationUrl", confirmationUrl);
        properties.put("activationCode", activationCode);

        Context context = new Context();
        context.setVariables(properties);

        helper.setFrom("kgrill.customerservice@gmail.com", senderName);
        helper.setTo(to);
        helper.setSubject(subject);

        String template = templateEngine.process(templateName, context);
        helper.setText(template, true);

        mailSender.send(mimeMessage);


    }


    @Override
    @Async
    public void sendSimpleEmail(String name, String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject(NEW_USER_ACCOUNT_ACTIVATION);
            message.setFrom(sender);
            message.setTo(to);
            message.setText(activeCodeMessage(name, activationUrl, code));
            mailSender.send(message);
        } catch (Exception exception) {
            System.out.println("Error: " + exception.getMessage());
            throw new RuntimeException("Error: " + exception.getMessage());
        }
    }

    @Override
    public void sendMimeMessageWithEmbeddedFiles(String name, String to, String code) throws MessagingException {
        try {
            MimeMessage message = getMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_CODING);
            helper.setSubject(NEW_USER_ACCOUNT_ACTIVATION);
            helper.setPriority(1);
            helper.setFrom(sender);
            helper.setTo(to);
            helper.setText(activeCodeMessage(name, activationUrl, code));
            //Embedded image
            mailSender.send(message);
        } catch (Exception exception) {
            System.out.println("Error: " + exception.getMessage());
            throw new RuntimeException("Error: " + exception.getMessage());
        }

    }

    @Override
    @Async
    public void sendMimeMessageWithHtml(String name, String to, String content, String template, String subject) throws MessagingException {
        try {
            String senderNickName = "Customer Service Team at Souvi";
            Context context = new Context();
            context.setVariable("username", name);
            context.setVariable("content", content);
            extractTemplate(to, template, subject, senderNickName, context);
        } catch (Exception exception) {
            System.out.println("Error: " + exception.getMessage());
            throw new RuntimeException("Error: " + exception.getMessage());
        }
    }

    @Override
    @Async
    public void sendMimeMessageForSeller(String seller, String buyer, String date, String time, String sendTo, String code, List<OrderProductDTO> product, int totalPrice, String customer, String phone, String address, String template, String subject) throws MessagingException {
        try {
            String senderNickName = "Customer Service Team at Souvi";
            Context context = new Context();
            context.setVariable("sellerName", seller);
            context.setVariable("buyerName", buyer);
            context.setVariable("date", date);
            context.setVariable("time", time);
            context.setVariable("orderCode", code);
            context.setVariable("productList", product);
            context.setVariable("totalPrice", totalPrice);
            context.setVariable("customer", customer);
            context.setVariable("phone", phone);
            context.setVariable("address", address);
            extractTemplate(sendTo, template, subject, senderNickName, context);
        } catch (Exception exception) {
            System.out.println("Error: " + exception.getMessage());
            throw new RuntimeException("Error: " + exception.getMessage());
        }
    }

    private void extractTemplate(String sentTo, String template, String subject, String senderNickName, Context context) throws MessagingException, UnsupportedEncodingException {
        String text = templateEngine.process(template, context);
        MimeMessage message = getMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_CODING);
        helper.setSubject(subject);
        helper.setPriority(1);
        helper.setFrom(sender, senderNickName);
        helper.setTo(sentTo);
        helper.setText(text, true);
        mailSender.send(message);
    }

    public MimeMessage getMessage() {
        return mailSender.createMimeMessage();
    }

}
