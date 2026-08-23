-- Migration: Create Two-Factor Authentication table
-- Version: V5
-- Description: Add 2FA support with SMS, Email, and Authenticator methods

CREATE TABLE two_factor_auth (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    method VARCHAR(20) NOT NULL CHECK (method IN ('SMS', 'EMAIL', 'AUTHENTICATOR')),
    secret_key VARCHAR(32),
    phone_number VARCHAR(20),
    backup_email VARCHAR(100),
    verification_code VARCHAR(10),
    code_expires_at TIMESTAMP,
    is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    backup_codes TEXT, -- JSON array of backup codes
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_two_factor_auth_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_2fa UNIQUE (user_id),
    
    -- Check constraints for method-specific fields
    CONSTRAINT check_sms_phone CHECK (
        method != 'SMS' OR (phone_number IS NOT NULL AND LENGTH(phone_number) > 0)
    ),
    CONSTRAINT check_email_backup CHECK (
        method != 'EMAIL' OR (backup_email IS NOT NULL AND LENGTH(backup_email) > 0)
    ),
    CONSTRAINT check_authenticator_secret CHECK (
        method != 'AUTHENTICATOR' OR (secret_key IS NOT NULL AND LENGTH(secret_key) > 0)
    )
);

-- Indexes for performance
CREATE INDEX idx_two_factor_auth_user_id ON two_factor_auth(user_id);
CREATE INDEX idx_two_factor_auth_enabled ON two_factor_auth(user_id, is_enabled) WHERE is_enabled = TRUE;
CREATE INDEX idx_two_factor_auth_code_expires ON two_factor_auth(code_expires_at) WHERE code_expires_at IS NOT NULL;

-- Comments for documentation
COMMENT ON TABLE two_factor_auth IS 'Two-factor authentication configuration and verification codes for users';
COMMENT ON COLUMN two_factor_auth.method IS 'Authentication method: SMS, EMAIL, or AUTHENTICATOR';
COMMENT ON COLUMN two_factor_auth.secret_key IS 'TOTP secret key for authenticator apps (Base32 encoded)';
COMMENT ON COLUMN two_factor_auth.phone_number IS 'Phone number for SMS codes (international format recommended)';
COMMENT ON COLUMN two_factor_auth.backup_email IS 'Email address for backup codes (different from login email)';
COMMENT ON COLUMN two_factor_auth.verification_code IS 'Current verification code (temporary)';
COMMENT ON COLUMN two_factor_auth.code_expires_at IS 'Expiration time for current verification code';
COMMENT ON COLUMN two_factor_auth.backup_codes IS 'JSON array of one-time backup codes';
COMMENT ON COLUMN two_factor_auth.is_enabled IS 'Whether 2FA is currently active for the user';