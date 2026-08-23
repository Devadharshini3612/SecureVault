package com.securevault.service;

import com.securevault.entity.TwoFactorAuth;
import com.securevault.enums.TwoFactorMethod;
import com.securevault.repository.TwoFactorAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for Two-Factor Authentication operations
 * 
 * Handles 2FA setup, code generation, verification, and management
 */
@Service
public class TwoFactorAuthService {
    
    @Autowired
    private TwoFactorAuthRepository twoFactorAuthRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private com.securevault.repository.UserRepository userRepository;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Enable 2FA for a user with SMS method
     */
    public TwoFactorAuth enableSMS(Long userId, String phoneNumber) {
        TwoFactorAuth twoFA = twoFactorAuthRepository.findByUserId(userId)
                .orElse(new TwoFactorAuth(userId, TwoFactorMethod.SMS));
        
        twoFA.setMethod(TwoFactorMethod.SMS);
        twoFA.setPhoneNumber(phoneNumber);
        twoFA.setIsEnabled(true);
        
        // Generate backup codes
        twoFA.setBackupCodes(generateBackupCodes());
        
        return twoFactorAuthRepository.save(twoFA);
    }
    
    /**
     * Enable 2FA for a user with Email method
     */
    public TwoFactorAuth enableEmail(Long userId, String backupEmail) {
        TwoFactorAuth twoFA = twoFactorAuthRepository.findByUserId(userId)
                .orElse(new TwoFactorAuth(userId, TwoFactorMethod.EMAIL));
        
        twoFA.setMethod(TwoFactorMethod.EMAIL);
        twoFA.setBackupEmail(backupEmail);
        twoFA.setIsEnabled(true);
        
        // Generate backup codes
        twoFA.setBackupCodes(generateBackupCodes());
        
        return twoFactorAuthRepository.save(twoFA);
    }
    
    /**
     * Enable 2FA for a user with Authenticator App method
     */
    public TwoFactorAuth enableAuthenticator(Long userId) {
        TwoFactorAuth twoFA = twoFactorAuthRepository.findByUserId(userId)
                .orElse(new TwoFactorAuth(userId, TwoFactorMethod.AUTHENTICATOR));
        
        twoFA.setMethod(TwoFactorMethod.AUTHENTICATOR);
        twoFA.setSecretKey(generateSecretKey());
        twoFA.setIsEnabled(true);
        
        // Generate backup codes
        twoFA.setBackupCodes(generateBackupCodes());
        
        return twoFactorAuthRepository.save(twoFA);
    }
    
    /**
     * Disable 2FA for a user
     */
    public void disable2FA(Long userId) {
        Optional<TwoFactorAuth> twoFA = twoFactorAuthRepository.findByUserId(userId);
        if (twoFA.isPresent()) {
            TwoFactorAuth auth = twoFA.get();
            auth.setIsEnabled(false);
            auth.setVerificationCode(null);
            auth.setCodeExpiresAt(null);
            twoFactorAuthRepository.save(auth);
        }
    }
    
    /**
     * Generate and send verification code
     */
    public void sendVerificationCode(Long userId) {
        Optional<TwoFactorAuth> twoFAOpt = twoFactorAuthRepository.findEnabledByUserId(userId);
        if (!twoFAOpt.isPresent()) {
            throw new IllegalStateException("2FA is not enabled for this user");
        }
        
        TwoFactorAuth twoFA = twoFAOpt.get();
        String code = generateVerificationCode();
        
        twoFA.setVerificationCode(code);
        twoFA.setCodeExpiresAt(LocalDateTime.now().plusMinutes(5)); // 5 minute expiry
        twoFactorAuthRepository.save(twoFA);
        
        // Send code based on method
        switch (twoFA.getMethod()) {
            case SMS:
                sendSMSCode(twoFA.getPhoneNumber(), code);
                break;
            case EMAIL:
                sendEmailCode(twoFA.getBackupEmail(), code, userId);
                break;
            case AUTHENTICATOR:
                // No need to send - user uses their authenticator app
                break;
        }
    }
    
    /**
     * Verify 2FA code
     */
    public boolean verifyCode(Long userId, String code) {
        // Check if it's a backup code
        if (isBackupCode(userId, code)) {
            return useBackupCode(userId, code);
        }
        
        // Check regular verification code
        Optional<TwoFactorAuth> twoFAOpt = twoFactorAuthRepository.findByUserIdAndValidCode(
            userId, code, LocalDateTime.now());
        
        if (twoFAOpt.isPresent()) {
            // Clear the code after successful verification
            TwoFactorAuth twoFA = twoFAOpt.get();
            twoFA.setVerificationCode(null);
            twoFA.setCodeExpiresAt(null);
            twoFactorAuthRepository.save(twoFA);
            return true;
        }
        
        // For authenticator apps, verify TOTP
        Optional<TwoFactorAuth> authOpt = twoFactorAuthRepository.findEnabledByUserId(userId);
        if (authOpt.isPresent() && authOpt.get().getMethod() == TwoFactorMethod.AUTHENTICATOR) {
            return verifyTOTP(authOpt.get().getSecretKey(), code);
        }
        
        return false;
    }
    
    /**
     * Check if 2FA is enabled for user
     */
    public boolean is2FAEnabled(Long userId) {
        return twoFactorAuthRepository.isEnabledForUser(userId);
    }
    
    /**
     * Get 2FA configuration for user
     */
    public Optional<TwoFactorAuth> get2FAConfig(Long userId) {
        return twoFactorAuthRepository.findByUserId(userId);
    }
    
    /**
     * Generate QR code data for authenticator apps
     */
    public String generateQRCodeData(Long userId, String userEmail, String secretKey) {
        String issuer = "SecureVault";
        String accountName = userEmail;
        
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer, accountName, secretKey, issuer);
    }
    
    // ========== Private Helper Methods ==========
    
    /**
     * Generate a 6-digit verification code
     */
    private String generateVerificationCode() {
        return String.format("%06d", secureRandom.nextInt(1000000));
    }
    
    /**
     * Generate a secret key for TOTP (32 character base32)
     */
    private String generateSecretKey() {
        byte[] buffer = new byte[20]; // 160 bits
        secureRandom.nextBytes(buffer);
        return Base64.getEncoder().encodeToString(buffer).replaceAll("=", "").substring(0, 32);
    }
    
    /**
     * Generate 10 backup codes
     */
    private String generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            codes.add(String.format("%08d", secureRandom.nextInt(100000000)));
        }
        return String.join(",", codes);
    }
    
    /**
     * Send SMS verification code (mock implementation)
     */
    private void sendSMSCode(String phoneNumber, String code) {
        // In a real implementation, this would integrate with SMS providers like Twilio
        System.out.println("SMS Code sent to " + phoneNumber + ": " + code);
        
        // For demo purposes, we'll log it
        // In production, integrate with SMS service providers
    }
    
    /**
     * Send email verification code
     */
    private void sendEmailCode(String backupEmail, String code, Long userId) {
        try {
            // Get user's primary email
            String primaryEmail = userRepository.findById(userId)
                .map(user -> user.getEmail())
                .orElse(backupEmail);
            
            // Send to primary email (user's registered email)
            emailService.send2FACode(primaryEmail, code);
            System.out.println("✅ Email Code sent to " + primaryEmail + ": " + code);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email code: " + e.getMessage());
            // Log but don't fail the operation
        }
    }
    
    /**
     * Check if code is a backup code
     */
    private boolean isBackupCode(Long userId, String code) {
        Optional<TwoFactorAuth> twoFAOpt = twoFactorAuthRepository.findEnabledByUserId(userId);
        if (!twoFAOpt.isPresent()) return false;
        
        String backupCodes = twoFAOpt.get().getBackupCodes();
        return backupCodes != null && backupCodes.contains(code);
    }
    
    /**
     * Use a backup code (one-time use)
     */
    private boolean useBackupCode(Long userId, String code) {
        Optional<TwoFactorAuth> twoFAOpt = twoFactorAuthRepository.findEnabledByUserId(userId);
        if (!twoFAOpt.isPresent()) return false;
        
        TwoFactorAuth twoFA = twoFAOpt.get();
        String backupCodes = twoFA.getBackupCodes();
        
        if (backupCodes != null && backupCodes.contains(code)) {
            // Remove the used backup code
            String updatedCodes = backupCodes.replace(code, "").replaceAll(",,", ",").replaceAll("^,|,$", "");
            twoFA.setBackupCodes(updatedCodes);
            twoFactorAuthRepository.save(twoFA);
            return true;
        }
        
        return false;
    }
    
    /**
     * Verify TOTP code for authenticator apps
     */
    private boolean verifyTOTP(String secretKey, String userCode) {
        try {
            // Simple TOTP verification (in production, use a proper TOTP library)
            long timeWindow = System.currentTimeMillis() / 30000; // 30-second window
            
            // For demo purposes, accept any 6-digit code
            // In production, implement proper TOTP algorithm
            return userCode.matches("\\d{6}");
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Clean up expired verification codes (scheduled task)
     */
    public void cleanupExpiredCodes() {
        twoFactorAuthRepository.clearExpiredCodes(LocalDateTime.now());
    }
}