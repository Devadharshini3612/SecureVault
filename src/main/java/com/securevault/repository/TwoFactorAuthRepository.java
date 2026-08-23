package com.securevault.repository;

import com.securevault.entity.TwoFactorAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for TwoFactorAuth entity
 */
@Repository
public interface TwoFactorAuthRepository extends JpaRepository<TwoFactorAuth, Long> {
    
    /**
     * Find 2FA configuration by user ID
     */
    Optional<TwoFactorAuth> findByUserId(Long userId);
    
    /**
     * Find enabled 2FA configuration by user ID
     */
    @Query("SELECT tfa FROM TwoFactorAuth tfa WHERE tfa.userId = :userId AND tfa.isEnabled = true")
    Optional<TwoFactorAuth> findEnabledByUserId(@Param("userId") Long userId);
    
    /**
     * Find by user ID and verification code
     */
    @Query("SELECT tfa FROM TwoFactorAuth tfa WHERE tfa.userId = :userId AND tfa.verificationCode = :code AND tfa.codeExpiresAt > :now")
    Optional<TwoFactorAuth> findByUserIdAndValidCode(@Param("userId") Long userId, 
                                                    @Param("code") String code, 
                                                    @Param("now") LocalDateTime now);
    
    /**
     * Check if user has 2FA enabled
     */
    @Query("SELECT COUNT(tfa) > 0 FROM TwoFactorAuth tfa WHERE tfa.userId = :userId AND tfa.isEnabled = true")
    boolean isEnabledForUser(@Param("userId") Long userId);
    
    /**
     * Delete expired verification codes
     */
    @Query("UPDATE TwoFactorAuth tfa SET tfa.verificationCode = NULL, tfa.codeExpiresAt = NULL WHERE tfa.codeExpiresAt < :now")
    void clearExpiredCodes(@Param("now") LocalDateTime now);
}