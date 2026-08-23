package com.securevault.service;

import com.securevault.dto.ShareResponse;
import com.securevault.entity.Credential;
import com.securevault.entity.CredentialShare;
import com.securevault.entity.User;
import com.securevault.enums.Permission;
import com.securevault.exception.InvalidShareException;
import com.securevault.exception.ResourceNotFoundException;
import com.securevault.exception.UnauthorizedAccessException;
import com.securevault.repository.CredentialRepository;
import com.securevault.repository.CredentialShareRepository;
import com.securevault.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CredentialShareService
 *
 * Business logic for credential sharing between users.
 *
 * Business Rules:
 * - Only the owner can share credentials
 * - Cannot share with yourself
 * - Cannot share the same credential twice with the same user
 * - Soft-deleted credentials cannot be shared
 * - Revoked shares immediately lose access
 */
@Service
public class CredentialShareService {

    private static final Logger logger = LoggerFactory.getLogger(CredentialShareService.class);

    private final CredentialShareRepository shareRepository;
    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final AsyncNotificationService asyncNotificationService;

    /**
     * Constructor injection - Spring automatically injects dependencies
     */
    public CredentialShareService(
            CredentialShareRepository shareRepository,
            CredentialRepository credentialRepository,
            UserRepository userRepository,
            AuditService auditService,
            AsyncNotificationService asyncNotificationService) {
        this.shareRepository = shareRepository;
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.asyncNotificationService = asyncNotificationService;
    }

    /**
     * Share a credential with another user
     *
     * @param credentialId the credential to share
     * @param ownerId the owner of the credential
     * @param sharedWithUserId the user to share with
     * @param permission the permission level (READ or EDIT)
     * @param expiresAt optional expiration date
     * @return ShareResponse with share details
     */
    @Transactional
    public ShareResponse shareCredential(Long credentialId, Long ownerId, Long sharedWithUserId, 
                                        Permission permission, LocalDateTime expiresAt) {
        
        logger.info("Sharing credential {} from owner {} to user {} with permission {}", 
                   credentialId, ownerId, sharedWithUserId, permission);

        // Rule 1: Cannot share with yourself
        if (ownerId.equals(sharedWithUserId)) {
            throw new InvalidShareException("Cannot share credential with yourself");
        }

        // Rule 2: Verify credential exists and belongs to owner
        Optional<Credential> credentialOpt = credentialRepository
            .findByCredentialIdAndUserIdAndDeletedFalse(credentialId, ownerId);
        
        if (credentialOpt.isEmpty()) {
            throw new ResourceNotFoundException("Credential not found or you don't have permission to share it");
        }

        Credential credential = credentialOpt.get();

        // Rule 3: Cannot share deleted credentials
        if (credential.isDeleted()) {
            throw new InvalidShareException("Cannot share a deleted credential");
        }

        // Rule 4: Verify recipient user exists
        Optional<User> recipientOpt = userRepository.findById(sharedWithUserId);
        if (recipientOpt.isEmpty()) {
            throw new ResourceNotFoundException("User with ID " + sharedWithUserId + " not found");
        }

        // Rule 5: Check if already shared with this user
        if (shareRepository.existsActiveShareForCredentialAndUser(credentialId, sharedWithUserId)) {
            throw new InvalidShareException("This credential is already shared with this user");
        }

        // Create share
        CredentialShare share = new CredentialShare(credentialId, ownerId, sharedWithUserId, permission);
        share.setExpiresAt(expiresAt);
        share = shareRepository.save(share);

        // Audit log
        auditService.createAuditLog("SHARE", "CREDENTIAL", credentialId, ownerId,
            String.format("Shared credential %d with user %d (permission: %s)", 
                         credentialId, sharedWithUserId, permission));

        // Async notification
        User recipient = recipientOpt.get();
        asyncNotificationService.logActivity(sharedWithUserId, "CREDENTIAL_RECEIVED", 
            String.format("User %d shared '%s' with you", ownerId, credential.getServiceName()));

        // Build response
        return buildShareResponse(share, credential, userRepository.findById(ownerId).get(), recipient);
    }

    /**
     * Get all credentials shared WITH the logged-in user
     *
     * @param userId the user ID
     * @return list of shared credentials
     */
    @Transactional(readOnly = true)
    public List<ShareResponse> getSharedWithMe(Long userId) {
        logger.info("Getting credentials shared with user {}", userId);

        List<CredentialShare> shares = shareRepository.findBySharedWithUserIdAndActiveTrue(userId);
        List<ShareResponse> responses = new ArrayList<>();

        for (CredentialShare share : shares) {
            // Skip expired shares
            if (share.isExpired()) {
                continue;
            }

            Optional<Credential> credentialOpt = credentialRepository.findById(share.getCredentialId());
            if (credentialOpt.isEmpty() || credentialOpt.get().isDeleted()) {
                continue; // Skip if credential no longer exists or is deleted
            }

            Optional<User> ownerOpt = userRepository.findById(share.getOwnerId());
            Optional<User> recipientOpt = userRepository.findById(share.getSharedWithUserId());

            if (ownerOpt.isPresent() && recipientOpt.isPresent()) {
                responses.add(buildShareResponse(share, credentialOpt.get(), ownerOpt.get(), recipientOpt.get()));
            }
        }

        return responses;
    }

    /**
     * Get all credentials that the logged-in user HAS SHARED (as owner)
     *
     * @param userId the owner user ID
     * @return list of shares where user is the owner
     */
    @Transactional(readOnly = true)
    public List<ShareResponse> getMyShares(Long userId) {
        logger.info("Getting credentials shared BY user {} (as owner)", userId);

        List<CredentialShare> shares = shareRepository.findByOwnerIdAndActiveTrue(userId);
        List<ShareResponse> responses = new ArrayList<>();

        for (CredentialShare share : shares) {
            // Skip expired shares
            if (share.isExpired()) {
                continue;
            }

            Optional<Credential> credentialOpt = credentialRepository.findById(share.getCredentialId());
            if (credentialOpt.isEmpty() || credentialOpt.get().isDeleted()) {
                continue;
            }

            Optional<User> ownerOpt = userRepository.findById(share.getOwnerId());
            Optional<User> recipientOpt = userRepository.findById(share.getSharedWithUserId());

            if (ownerOpt.isPresent() && recipientOpt.isPresent()) {
                responses.add(buildShareResponse(share, credentialOpt.get(), ownerOpt.get(), recipientOpt.get()));
            }
        }

        return responses;
    }

    /**
     * Update share permission
     *
     * @param shareId the share ID
     * @param ownerId the owner ID (for authorization)
     * @param newPermission the new permission
     * @return updated ShareResponse
     */
    @Transactional
    public ShareResponse updateSharePermission(Long shareId, Long ownerId, Permission newPermission) {
        logger.info("Updating share {} permission to {}", shareId, newPermission);

        // Verify ownership
        Optional<CredentialShare> shareOpt = shareRepository.findByShareIdAndOwnerIdAndActiveTrue(shareId, ownerId);
        if (shareOpt.isEmpty()) {
            throw new UnauthorizedAccessException("Share not found or you don't have permission to modify it");
        }

        CredentialShare share = shareOpt.get();
        share.setPermission(newPermission);
        share = shareRepository.save(share);

        // Audit log
        auditService.createAuditLog("UPDATE", "CREDENTIAL_SHARE", shareId, ownerId,
            String.format("Updated share %d permission to %s", shareId, newPermission));

        // Build response
        Credential credential = credentialRepository.findById(share.getCredentialId()).get();
        User owner = userRepository.findById(share.getOwnerId()).get();
        User recipient = userRepository.findById(share.getSharedWithUserId()).get();

        return buildShareResponse(share, credential, owner, recipient);
    }

    /**
     * Revoke a share
     *
     * @param shareId the share ID
     * @param ownerId the owner ID (for authorization)
     */
    @Transactional
    public void revokeShare(Long shareId, Long ownerId) {
        logger.info("Revoking share {} by owner {}", shareId, ownerId);

        // Verify ownership
        Optional<CredentialShare> shareOpt = shareRepository.findByShareIdAndOwnerIdAndActiveTrue(shareId, ownerId);
        if (shareOpt.isEmpty()) {
            throw new UnauthorizedAccessException("Share not found or you don't have permission to revoke it");
        }

        CredentialShare share = shareOpt.get();
        share.setActive(false);
        shareRepository.save(share);

        // Audit log
        auditService.createAuditLog("DELETE", "CREDENTIAL_SHARE", shareId, ownerId,
            String.format("Revoked share %d with user %d", shareId, share.getSharedWithUserId()));

        // Async notification
        asyncNotificationService.logActivity(share.getSharedWithUserId(), "SHARE_REVOKED", 
            String.format("Share for credential %d was revoked", share.getCredentialId()));
    }

    /**
     * Check if user has access to a credential (as owner or via share)
     *
     * @param credentialId the credential ID
     * @param userId the user ID
     * @return true if user has access
     */
    public boolean hasAccessToCredential(Long credentialId, Long userId) {
        // Check if user is owner
        Optional<Credential> credentialOpt = credentialRepository
            .findByCredentialIdAndUserIdAndDeletedFalse(credentialId, userId);
        
        if (credentialOpt.isPresent()) {
            return true; // User is owner
        }

        // Check if credential is shared with user
        return shareRepository.hasAccessToCredential(credentialId, userId);
    }

    /**
     * Get permission for a user on a credential
     *
     * @param credentialId the credential ID
     * @param userId the user ID
     * @return Permission (null if no access, or the permission level)
     */
    public Permission getPermissionForCredential(Long credentialId, Long userId) {
        // Check if user is owner (owners have full access)
        Optional<Credential> credentialOpt = credentialRepository
            .findByCredentialIdAndUserIdAndDeletedFalse(credentialId, userId);
        
        if (credentialOpt.isPresent()) {
            return Permission.EDIT; // Owners have EDIT permission (actually they have all permissions)
        }

        // Check shared permission
        Optional<Permission> permissionOpt = shareRepository
            .findPermissionForUserAndCredential(credentialId, userId);
        
        return permissionOpt.orElse(null);
    }

    /**
     * Check if user is the owner of a credential
     *
     * @param credentialId the credential ID
     * @param userId the user ID
     * @return true if user is owner
     */
    public boolean isOwner(Long credentialId, Long userId) {
        return credentialRepository
            .findByCredentialIdAndUserIdAndDeletedFalse(credentialId, userId)
            .isPresent();
    }

    /**
     * Revoke all shares for a credential (when credential is deleted)
     *
     * @param credentialId the credential ID
     */
    @Transactional
    public void revokeAllSharesForCredential(Long credentialId) {
        logger.info("Revoking all shares for credential {}", credentialId);
        shareRepository.revokeAllSharesForCredential(credentialId);
    }

    /**
     * Build ShareResponse DTO from entities
     */
    private ShareResponse buildShareResponse(CredentialShare share, Credential credential, 
                                             User owner, User recipient) {
        ShareResponse response = new ShareResponse();
        response.setShareId(share.getShareId());
        response.setCredentialId(share.getCredentialId());
        response.setServiceName(credential.getServiceName());
        response.setOwnerId(share.getOwnerId());
        response.setOwnerName(owner.getName());
        response.setOwnerEmail(owner.getEmail());
        response.setSharedWithUserId(share.getSharedWithUserId());
        response.setSharedWithUserName(recipient.getName());
        response.setSharedWithUserEmail(recipient.getEmail());
        response.setPermission(share.getPermission());
        response.setSharedAt(share.getSharedAt());
        response.setExpiresAt(share.getExpiresAt());
        response.setActive(share.isActive());
        response.setRevokedAt(share.getRevokedAt());
        return response;
    }

    /**
     * Get user ID by email address
     * 
     * @param email The user's email address
     * @return userId if found, null otherwise
     */
    public Long getUserIdByEmail(String email) {
        logger.info("Looking up user by email: {}", email);
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            Long userId = userOpt.get().getUserId();
            logger.info("Found user with ID: {} for email: {}", userId, email);
            return userId;
        }
        
        logger.warn("No user found for email: {}", email);
        return null;
    }
}
