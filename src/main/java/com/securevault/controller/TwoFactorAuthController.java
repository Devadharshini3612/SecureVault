package com.securevault.controller;

import com.securevault.dto.ApiResponse;
import com.securevault.entity.TwoFactorAuth;
import com.securevault.security.JwtService;
import com.securevault.service.TwoFactorAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Two-Factor Authentication operations
 */
@RestController
@RequestMapping("/api/2fa")
@CrossOrigin(origins = {
    "http://localhost:3000", 
    "http://127.0.0.1:3000",
    "https://securevault-frontend.onrender.com"
})
public class TwoFactorAuthController {
    
    @Autowired
    private TwoFactorAuthService twoFactorAuthService;
    
    @Autowired
    private JwtService jwtService;
    
    /**
     * GET /api/2fa/status
     * Get 2FA status for the current user
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> get2FAStatus(
            @RequestHeader("Authorization") String authHeader) {
        
        Long userId = jwtService.extractUserId(authHeader.substring(7));
        
        Optional<TwoFactorAuth> config = twoFactorAuthService.get2FAConfig(userId);
        boolean isEnabled = twoFactorAuthService.is2FAEnabled(userId);
        
        Map<String, Object> status = Map.of(
            "enabled", isEnabled,
            "method", config.map(c -> c.getMethod().name()).orElse("NONE"),
            "phoneNumber", config.map(TwoFactorAuth::getPhoneNumber).orElse(""),
            "backupEmail", config.map(TwoFactorAuth::getBackupEmail).orElse(""),
            "hasBackupCodes", config.map(c -> c.getBackupCodes() != null && !c.getBackupCodes().isEmpty()).orElse(false)
        );
        
        return ResponseEntity.ok(ApiResponse.success("2FA status retrieved", status));
    }
    
    /**
     * POST /api/2fa/enable/sms
     * Enable 2FA with SMS method
     */
    @PostMapping("/enable/sms")
    public ResponseEntity<ApiResponse<Map<String, Object>>> enableSMS(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        
        Long userId = jwtService.extractUserId(authHeader.substring(7));
        String phoneNumber = request.get("phoneNumber");
        
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Phone number is required"));
        }
        
        TwoFactorAuth twoFA = twoFactorAuthService.enableSMS(userId, phoneNumber);
        
        Map<String, Object> response = Map.of(
            "enabled", true,
            "method", "SMS",
            "phoneNumber", phoneNumber,
            "backupCodes", twoFA.getBackupCodes().split(",")
        );
        
        return ResponseEntity.ok(ApiResponse.success("2FA enabled with SMS", response));
    }
    
    /**
     * POST /api/2fa/enable/email
     * Enable 2FA with Email method
     */
    @PostMapping("/enable/email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> enableEmail(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        
        Long userId = jwtService.extractUserId(authHeader.substring(7));
        String backupEmail = request.get("backupEmail");
        
        if (backupEmail == null || backupEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Backup email is required"));
        }
        
        TwoFactorAuth twoFA = twoFactorAuthService.enableEmail(userId, backupEmail);
        
        Map<String, Object> response = Map.of(
            "enabled", true,
            "method", "EMAIL",
            "backupEmail", backupEmail,
            "backupCodes", twoFA.getBackupCodes().split(",")
        );
        
        return ResponseEntity.ok(ApiResponse.success("2FA enabled with Email", response));
    }
    
    /**
     * POST /api/2fa/enable/authenticator
     * Enable 2FA with Authenticator App method
     */
    @PostMapping("/enable/authenticator")
    public ResponseEntity<ApiResponse<Map<String, Object>>> enableAuthenticator(
            @RequestHeader("Authorization") String authHeader) {
        
        Long userId = jwtService.extractUserId(authHeader.substring(7));
        String userEmail = jwtService.extractUsername(authHeader.substring(7));
        
        TwoFactorAuth twoFA = twoFactorAuthService.enableAuthenticator(userId);
        String qrCodeData = twoFactorAuthService.generateQRCodeData(userId, userEmail, twoFA.getSecretKey());
        
        Map<String, Object> response = Map.of(
            "enabled", true,
            "method", "AUTHENTICATOR",
            "secretKey", twoFA.getSecretKey(),
            "qrCodeData", qrCodeData,
            "backupCodes", twoFA.getBackupCodes().split(",")
        );
        
        return ResponseEntity.ok(ApiResponse.success("2FA enabled with Authenticator", response));
    }
    
    /**
     * POST /api/2fa/disable
     * Disable 2FA for the current user
     */
    @PostMapping("/disable")
    public ResponseEntity<ApiResponse<String>> disable2FA(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        
        Long userId = jwtService.extractUserId(authHeader.substring(7));
        String verificationCode = request.get("verificationCode");
        
        // Verify current 2FA code before disabling
        if (verificationCode == null || !twoFactorAuthService.verifyCode(userId, verificationCode)) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid verification code"));
        }
        
        twoFactorAuthService.disable2FA(userId);
        
        return ResponseEntity.ok(ApiResponse.success("2FA has been disabled", "disabled"));
    }
    
    /**
     * POST /api/2fa/send-code
     * Send verification code to user
     */
    @PostMapping("/send-code")
    public ResponseEntity<ApiResponse<String>> sendVerificationCode(
            @RequestHeader("Authorization") String authHeader) {
        
        Long userId = jwtService.extractUserId(authHeader.substring(7));
        
        try {
            twoFactorAuthService.sendVerificationCode(userId);
            return ResponseEntity.ok(ApiResponse.success("Verification code sent", "sent"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to send verification code"));
        }
    }
    
    /**
     * POST /api/2fa/verify
     * Verify 2FA code
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyCode(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        
        Long userId = jwtService.extractUserId(authHeader.substring(7));
        String code = request.get("code");
        
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Verification code is required"));
        }
        
        boolean isValid = twoFactorAuthService.verifyCode(userId, code);
        
        if (isValid) {
            return ResponseEntity.ok(ApiResponse.success("Code verified successfully", "verified"));
        } else {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid or expired verification code"));
        }
    }
    
    /**
     * GET /api/2fa/backup-codes
     * Get new backup codes (requires verification)
     */
    @PostMapping("/regenerate-backup-codes")
    public ResponseEntity<ApiResponse<String[]>> regenerateBackupCodes(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        
        Long userId = jwtService.extractUserId(authHeader.substring(7));
        String verificationCode = request.get("verificationCode");
        
        // Verify current 2FA code before regenerating
        if (verificationCode == null || !twoFactorAuthService.verifyCode(userId, verificationCode)) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid verification code"));
        }
        
        // Regenerate backup codes by re-enabling with current settings
        Optional<TwoFactorAuth> config = twoFactorAuthService.get2FAConfig(userId);
        if (!config.isPresent()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("2FA is not enabled"));
        }
        
        TwoFactorAuth twoFA = config.get();
        TwoFactorAuth updated;
        
        switch (twoFA.getMethod()) {
            case SMS:
                updated = twoFactorAuthService.enableSMS(userId, twoFA.getPhoneNumber());
                break;
            case EMAIL:
                updated = twoFactorAuthService.enableEmail(userId, twoFA.getBackupEmail());
                break;
            case AUTHENTICATOR:
                updated = twoFactorAuthService.enableAuthenticator(userId);
                break;
            default:
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Unknown 2FA method"));
        }
        
        String[] backupCodes = updated.getBackupCodes().split(",");
        
        return ResponseEntity.ok(ApiResponse.success("Backup codes regenerated", backupCodes));
    }
}