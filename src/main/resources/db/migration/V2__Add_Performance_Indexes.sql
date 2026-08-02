-- Database Performance Optimization Migration
-- Adds indexes to improve query performance for credential operations

-- ========================================
-- CREDENTIAL TABLE INDEXES
-- ========================================

-- Index for user credential lookup (most common query)
-- Supports: findByUserIdAndIsDeletedFalse, listCredentials
CREATE INDEX IF NOT EXISTS idx_credentials_user_active 
ON credentials(user_id, is_deleted, updated_at DESC);

-- Index for credential search by service name (case-insensitive)
-- Supports: searchCredentials with service name filtering
CREATE INDEX IF NOT EXISTS idx_credentials_service_name_lower 
ON credentials(user_id, is_deleted, LOWER(service_name));

-- Index for credential search by username (case-insensitive)  
-- Supports: searchCredentials with username filtering
CREATE INDEX IF NOT EXISTS idx_credentials_username_lower 
ON credentials(user_id, is_deleted, LOWER(username));

-- Index for category filtering
-- Supports: findByUserIdAndCategory, getCredentialsByCategory
CREATE INDEX IF NOT EXISTS idx_credentials_category 
ON credentials(user_id, category, is_deleted);

-- Index for recent activity queries
-- Supports: findRecentlyModifiedCredentials, activity dashboards
CREATE INDEX IF NOT EXISTS idx_credentials_updated_at 
ON credentials(user_id, updated_at DESC) WHERE is_deleted = false;

-- Index for audit and creation date queries
-- Supports: analytics and reporting features
CREATE INDEX IF NOT EXISTS idx_credentials_created_at 
ON credentials(user_id, created_at DESC) WHERE is_deleted = false;

-- Composite index for complex searches with multiple criteria
-- Supports: paginated search with filters
CREATE INDEX IF NOT EXISTS idx_credentials_multi_filter 
ON credentials(user_id, is_deleted, category, updated_at DESC);

-- ========================================
-- USER TABLE INDEXES (if not already optimized)
-- ========================================

-- Index for email lookup (login operations)
-- Supports: findByEmail, authentication
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_unique 
ON users(LOWER(email));

-- ========================================
-- CREDENTIAL SHARES TABLE INDEXES (if table exists)
-- ========================================

-- Index for finding credentials shared with a user
-- Supports: findSharedCredentialsForUser
CREATE INDEX IF NOT EXISTS idx_credential_shares_user_active 
ON credential_shares(shared_with_user_id, is_active, created_at DESC);

-- Index for checking credential sharing permissions
-- Supports: hasUserAccessToCredential
CREATE INDEX IF NOT EXISTS idx_credential_shares_credential_active 
ON credential_shares(credential_id, is_active);

-- Composite index for shared credential lookup optimization
CREATE INDEX IF NOT EXISTS idx_credential_shares_composite 
ON credential_shares(credential_id, shared_with_user_id, is_active);

-- ========================================
-- PERFORMANCE ANALYSIS VIEWS
-- ========================================

-- Create a view for credential statistics (optional)
CREATE OR REPLACE VIEW credential_stats AS
SELECT 
    u.user_id,
    u.email,
    COUNT(c.credential_id) as total_credentials,
    COUNT(CASE WHEN c.category = 'PERSONAL' THEN 1 END) as personal_count,
    COUNT(CASE WHEN c.category = 'WORK' THEN 1 END) as work_count,
    COUNT(CASE WHEN c.category = 'BANKING' THEN 1 END) as banking_count,
    MAX(c.updated_at) as last_updated,
    MIN(c.created_at) as first_created
FROM users u
LEFT JOIN credentials c ON u.user_id = c.user_id AND c.is_deleted = false
GROUP BY u.user_id, u.email;

-- ========================================
-- PERFORMANCE MONITORING QUERIES
-- ========================================

-- Query to check index usage (PostgreSQL specific)
-- Run this occasionally to verify indexes are being used:
-- 
-- SELECT schemaname, tablename, attname, n_distinct, correlation 
-- FROM pg_stats 
-- WHERE tablename IN ('credentials', 'users', 'credential_shares')
-- ORDER BY tablename, attname;
--
-- SELECT schemaname, tablename, indexname, idx_tup_read, idx_tup_fetch
-- FROM pg_stat_user_indexes 
-- WHERE schemaname = 'public' 
-- ORDER BY idx_tup_read DESC;

-- ========================================
-- CLEANUP OLD UNUSED INDEXES (if any exist)
-- ========================================

-- Remove any potentially conflicting or redundant indexes
-- (only if they exist and are not being used)
-- DROP INDEX IF EXISTS old_credential_index_name;

-- ========================================
-- MIGRATION VERIFICATION
-- ========================================

-- Verify all indexes were created successfully
DO $$
BEGIN
    -- Check if critical indexes exist
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE tablename = 'credentials' 
        AND indexname = 'idx_credentials_user_active'
    ) THEN
        RAISE EXCEPTION 'Critical index idx_credentials_user_active was not created';
    END IF;
    
    RAISE NOTICE 'Database performance optimization migration completed successfully';
END $$;