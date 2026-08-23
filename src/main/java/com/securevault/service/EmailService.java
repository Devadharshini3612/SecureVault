package com.securevault.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Email Service for sending verification codes and notifications
 * 
 * Supports both real email sending (via SMTP) and console logging (for development)
 */
@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@securevault.com}")
    private String fromEmail;
    
    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;
    
    /**
     * Send 2FA verification code via email
     * 
     * @param toEmail Recipient email address
     * @param code 6-digit verification code
     */
    public void send2FACode(String toEmail, String code) {
        String subject = "SecureVault - Your Verification Code";
        String body = buildVerificationEmailBody(code);
        
        sendEmail(toEmail, subject, body);
    }
    
    /**
     * Send email (real SMTP or console logging)
     * 
     * @param toEmail Recipient email
     * @param subject Email subject
     * @param body Email body
     */
    public void sendEmail(String toEmail, String subject, String body) {
        if (emailEnabled && mailSender != null) {
            // Send real email via SMTP
            try {
                sendRealEmail(toEmail, subject, body);
            } catch (Exception e) {
                logger.error("Failed to send email to {}: {}", toEmail, e.getMessage());
                // Fallback to console logging
                logEmailToConsole(toEmail, subject, body);
            }
        } else {
            // Development mode: log to console
            logEmailToConsole(toEmail, subject, body);
        }
    }
    
    /**
     * Send actual email via SMTP
     */
    private void sendRealEmail(String toEmail, String subject, String body) throws Exception {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, false); // false = plain text, true = HTML
            
            mailSender.send(message);
            
            logger.info("✅ Email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            logger.error("❌ Failed to send email: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Log email to console (for development/demo)
     */
    private void logEmailToConsole(String toEmail, String subject, String body) {
        logger.info("====================================");
        logger.info("📧 EMAIL SENT TO: {}", toEmail);
        logger.info("📋 SUBJECT: {}", subject);
        logger.info("📄 BODY:");
        logger.info("{}", body);
        logger.info("====================================");
    }
    
    /**
     * Build HTML email body for verification code
     */
    private String buildVerificationEmailBody(String code) {
        return String.format(
            "Hello,\n\n" +
            "Your SecureVault verification code is:\n\n" +
            "    %s\n\n" +
            "This code will expire in 5 minutes.\n\n" +
            "If you didn't request this code, please ignore this email and ensure your account is secure.\n\n" +
            "Thank you,\n" +
            "SecureVault Team\n\n" +
            "---\n" +
            "This is an automated message. Please do not reply to this email.",
            code
        );
    }
    
    /**
     * Send welcome email to new users
     */
    public void sendWelcomeEmail(String toEmail, String username) {
        String subject = "Welcome to SecureVault!";
        String body = String.format(
            "Hello %s,\n\n" +
            "Welcome to SecureVault - your secure password manager!\n\n" +
            "We're excited to have you on board. Your account has been successfully created.\n\n" +
            "Get started by:\n" +
            "1. Adding your first credential\n" +
            "2. Enabling Two-Factor Authentication for extra security\n" +
            "3. Exploring our password generator\n\n" +
            "Thank you for choosing SecureVault!\n\n" +
            "Best regards,\n" +
            "The SecureVault Team",
            username
        );
        
        sendEmail(toEmail, subject, body);
    }
}
