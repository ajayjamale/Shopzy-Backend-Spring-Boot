package com.ajay.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username:}")
    private String senderEmail;


    public void sendVerificationOtpEmail(String userEmail, String otp, String subject, String text) throws MessagingException, MailSendException {


        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");


            helper.setSubject(subject);
            helper.setText(text+otp, true);
            helper.setTo(userEmail);
            if (!senderEmail.isBlank()) {
                helper.setFrom(senderEmail);
            }
            javaMailSender.send(mimeMessage);
        } catch (MailException e) {
            throw new MailSendException("Failed to send email: " + e.getMessage(), e);
        }
    }
}
