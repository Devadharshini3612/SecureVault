package com.securevault.service;

import com.securevault.dto.CreateCredentialRequest;
import com.securevault.dto.UpdateCredentialRequest;
import com.securevault.dto.CredentialResponse;
import com.securevault.entity.AuditLog;
import com.securevault.entity.Credential;
import com.securevault.enums.Category;
import com.securevault.exception.PasswordReuseException;
import com.securevault.repository.CredentialRepository;
import com.securevault.specification.CredentialSpecification;
import com.securevault.util.AESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CredentialService
 *
 * @Service - marks this class as a Spring service bean.
 * This layer contains the business logic for credential management.
 *
 * Key responsibilities:
 * 1. Encrypt passwords before saving to database using AES-256
 * 2. Decrypt passwords when retrieving from database
 * 3. Perform CRUD operations on credentials
 * 4. Ensure users can only access their own credentials
 *
 * Security Flow:
 * - CREATE: plaintext password → AES encrypt → save encrypted password
 * - READ:   retrieve encrypted password → AES decrypt → return plaintext password
 * - UPDATE: new plaintext password → AES encrypt → update encrypted password
 * - DELETE: remove credential from database
 */
@Service
public class CredentialService {

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private PasswordHistoryService passwordHistoryService;

    @Autowired
    private AsyncNotificationService asyncNotificationService;

    @Autowired
    private CredentialShareService credentialShareService;

    /**
     * Creates a new credential with AES-256 encrypted password
     * Returns the created credential response
     *
     * Process:
     * 1. Receive plaintext password from request
     * 2. Encrypt password using AES-256-GCM
     * 3. Create Credential entity with encrypted password
     * 4. Save to database
     * 5. Create audit log
     * 6. Return response with decrypted password
     *
     * @param request CreateCredentialRequest containing userId, serviceName, username, plaintext password
     * @return CredentialResponse with created credential data
     * @throws Exception if encryption or database operation fails
     */
    @Transactional
    public CredentialResponse createCredentialWithResponse(CreateCredentialRequest request) throws Exception {
        
        // Step 1: Encrypt the plaintext password using AES-256
        String encryptedPassword = AESUtil.encrypt(request.getPassword());

        // Step 2: Create Credential entity with encrypted password
        Credential credential = new Credential(
            request.getUserId(),
            request.getServiceName(),
            request.getUsername(),
            encryptedPassword
        );

        // Set category if provided
        if (request.getCategory() != null) {
            credential.setCategory(request.getCategory());
        }

        // Step 3: Save to database
        Credential savedCredential = credentialRepository.save(credential);

        // Step 4: Create audit log entry within the same transaction
        auditService.logCredentialCreation(savedCredential.getCredentialId(), request.getUserId());

        // Step 5: Async notification (non-blocking)
        asyncNotificationService.logActivity(request.getUserId(), "CREDENTIAL_CREATED", 
            "Created credential for: " + request.getServiceName());

        // Step 6: Build and return response with decrypted password
        String decryptedPassword = AESUtil.decrypt(savedCredential.getEncryptedPassword());
        return new CredentialResponse(
            savedCredential.getCredentialId(),
            savedCredential.getUserId(),
            savedCredential.getServiceName(),
            savedCredential.getUsername(),
            decryptedPassword,
            savedCredential.getCreatedAt(),
            savedCredential.getUpdatedAt()
        );
    }

    /**
     * Creates a new credential with AES-256 encrypted password (legacy method)
     * 
     * @deprecated Use createCredentialWithResponse instead
     */
    @Transactional
    public String createCredential(CreateCredentialRequest request) throws Exception {
        createCredentialWithResponse(request);
        return "SUCCESS";
    }

    /**
     * Retrieves a credential by ID and decrypts the password
     *
     * Process:
     * 1. Find credential by credentialId and userId (security check)
     * 2. If not found → return "NOT_FOUND"
     * 3. If found → decrypt the password using AES-256
     * 4. Build and return CredentialResponse with decrypted password
     *
     * Security: Users can only retrieve their own credentials
     *
     * @param credentialId the ID of the credential to retrieve
     * @param userId the ID of the user (for authorization check)
     * @return CredentialResponse with decrypted password, or null if not found
     * @throws Exception if decryption fails
     */
    @Transactional(readOnly = true)
    public CredentialResponse getCredential(Long credentialId, Long userId) throws Exception {
        
        // Step 1: Check if user is owner
        Optional<Credential> credentialOptional = credentialRepository.findByCredentialIdAndUserId(credentialId, userId);

        if (credentialOptional.isPresent()) {
            // User is owner - full access
            Credential credential = credentialOptional.get();
            String decryptedPassword = AESUtil.decrypt(credential.getEncryptedPassword());

            return new CredentialResponse(
                credential.getCredentialId(),
                credential.getUserId(),
                credential.getServiceName(),
                credential.getUsername(),
                decryptedPassword,
                credential.getCreatedAt(),
                credential.getUpdatedAt()
            );
        }

        // Step 2: Check if credential is shared with user
        if (credentialShareService.hasAccessToCredential(credentialId, userId)) {
            // User has shared access
            Optional<Credential> sharedCredOpt = credentialRepository.findById(credentialId);
            if (sharedCredOpt.isPresent()) {
                Credential credential = sharedCredOpt.get();
                
                // Don't show password if deleted
                if (credential.isDeleted()) {
                    return null;
                }
                
                String decryptedPassword = AESUtil.decrypt(credential.getEncryptedPassword());

                return new CredentialResponse(
                    credential.getCredentialId(),
                    credential.getUserId(),
                    credential.getServiceName(),
                    credential.getUsername(),
                    decryptedPassword,
                    credential.getCreatedAt(),
                    credential.getUpdatedAt()
                );
            }
        }

        // Step 3: No access
        return null;
    }

    /**
     * Lists all credentials for a specific user with decrypted passwords
     *
     * Process:
     * 1. Find all credentials belonging to the user
     * 2. For each credential:
     *    - Decrypt the password
     *    - Build CredentialResponse
     * 3. Return list of responses
     *
     * @param userId the ID of the user
     * @return List of CredentialResponse objects with decrypted passwords
     * @throws Exception if decryption fails for any credential
     */
    @Transactional(readOnly = true)
    public List<CredentialResponse> listCredentials(Long userId) throws Exception {
        
        // Step 1: Find all credentials for the user
        List<Credential> credentials = credentialRepository.findByUserId(userId);

        // Step 2: Decrypt passwords and build response list
        List<CredentialResponse> responseList = new ArrayList<>();
        
        for (Credential credential : credentials) {
            // Decrypt password
            String decryptedPassword = AESUtil.decrypt(credential.getEncryptedPassword());

            // Build response
            CredentialResponse response = new CredentialResponse(
                credential.getCredentialId(),
                credential.getUserId(),
                credential.getServiceName(),
                credential.getUsername(),
                decryptedPassword, // Decrypted password
                credential.getCreatedAt(),
                credential.getUpdatedAt()
            );

            responseList.add(response);
        }

        return responseList;
    }

    /**
     * Updates an existing credential and returns the updated credential response
     *
     * Process:
     * 1. Find credential by credentialId and userId (security check)
     * 2. If not found → return null
     * 3. Update fields that are provided in the request
     * 4. Save updated credential
     * 5. Create audit log
     * 6. Return updated credential response
     *
     * @param credentialId the ID of the credential to update
     * @param userId the ID of the user (for authorization check)
     * @param request UpdateCredentialRequest with fields to update
     * @return CredentialResponse with updated data, or null if not found
     * @throws Exception if encryption fails
     */
    @Transactional
    public CredentialResponse updateCredentialWithResponse(Long credentialId, Long userId, UpdateCredentialRequest request) throws Exception {
        
        // Step 1: Check if user is owner
        Optional<Credential> credentialOptional = credentialRepository.findByCredentialIdAndUserId(credentialId, userId);
        boolean isOwner = credentialOptional.isPresent();

        Credential credential;
        
        if (isOwner) {
            // User is owner - full access
            credential = credentialOptional.get();
        } else {
            // Step 2: Check if user has EDIT permission via sharing
            com.securevault.enums.Permission permission = credentialShareService.getPermissionForCredential(credentialId, userId);
            
            if (permission == null) {
                throw new com.securevault.exception.UnauthorizedAccessException(
                    "You don't have permission to access this credential");
            }
            
            if (permission != com.securevault.enums.Permission.EDIT) {
                throw new com.securevault.exception.UnauthorizedAccessException(
                    "You only have READ permission. Cannot modify this credential.");
            }
            
            // User has EDIT permission - allow update
            Optional<Credential> sharedCredOpt = credentialRepository.findById(credentialId);
            if (sharedCredOpt.isEmpty() || sharedCredOpt.get().isDeleted()) {
                return null;
            }
            credential = sharedCredOpt.get();
        }

        // Step 3: Update the credential
        StringBuilder updateDetails = new StringBuilder("Updated fields: ");
        boolean hasUpdates = false;

        // Update serviceName if provided
        if (request.getServiceName() != null && !request.getServiceName().isEmpty()) {
            credential.setServiceName(request.getServiceName());
            updateDetails.append("serviceName, ");
            hasUpdates = true;
        }

        // Update username if provided
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            credential.setUsername(request.getUsername());
            updateDetails.append("username, ");
            hasUpdates = true;
        }

        // Update password if provided (encrypt the new password)
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            // Check for password reuse before updating
            passwordHistoryService.validatePasswordNotReused(credentialId, request.getPassword());
            
            // Save current password to history before updating
            passwordHistoryService.savePasswordHistory(credentialId, credential.getEncryptedPassword(), "Password updated by user");
            
            // Encrypt and set new password
            String newEncryptedPassword = AESUtil.encrypt(request.getPassword());
            credential.setEncryptedPassword(newEncryptedPassword);
            updateDetails.append("password, ");
            hasUpdates = true;
        }

        // Update category if provided
        if (request.getCategory() != null) {
            credential.setCategory(request.getCategory());
            updateDetails.append("category, ");
            hasUpdates = true;
        }

        // Step 4: Save updated credential if there were any updates
        if (hasUpdates) {
            credentialRepository.save(credential);
            
            // Step 5: Create audit log entry within the same transaction
            String finalDetails = updateDetails.toString().replaceAll(", $", ""); // Remove trailing comma
            auditService.logCredentialUpdate(credentialId, userId, finalDetails);
            
            // Step 6: Async activity logging (non-blocking)
            asyncNotificationService.logActivity(userId, "CREDENTIAL_UPDATED", 
                "Updated credential: " + credential.getServiceName() + (isOwner ? "" : " (shared)"));
        }

        // Step 6: Build and return response with decrypted password
        String decryptedPassword = AESUtil.decrypt(credential.getEncryptedPassword());
        return new CredentialResponse(
            credential.getCredentialId(),
            credential.getUserId(),
            credential.getServiceName(),
            credential.getUsername(),
            decryptedPassword,
            credential.getCreatedAt(),
            credential.getUpdatedAt()
        );
    }

    /**
     * Updates an existing credential (legacy method)
     * 
     * @deprecated Use updateCredentialWithResponse instead
     */
    @Transactional
    public String updateCredential(Long credentialId, Long userId, UpdateCredentialRequest request) throws Exception {
        CredentialResponse response = updateCredentialWithResponse(credentialId, userId, request);
        return response != null ? "SUCCESS" : "NOT_FOUND";
    }

    /**
     * Deletes a credential (soft delete)
     *
     * Process:
     * 1. Find credential by credentialId and userId (security check)
     * 2. If not found → return "NOT_FOUND"
     * 3. If found → mark as deleted (soft delete)
     * 4. Create audit log
     * 5. Return "SUCCESS"
     *
     * Security: Users can only delete their own credentials
     *
     * @param credentialId the ID of the credential to delete
     * @param userId the ID of the user (for authorization check)
     * @return "SUCCESS" if deleted, "NOT_FOUND" if credential doesn't exist or user doesn't have access
     */
    @Transactional
    public String deleteCredential(Long credentialId, Long userId) {
        
        // Step 1: Find credential with security check
        Optional<Credential> credentialOptional = credentialRepository.findByCredentialIdAndUserId(credentialId, userId);

        // Step 2: Check if credential exists and belongs to the user
        if (credentialOptional.isEmpty()) {
            return "NOT_FOUND";
        }

        // Step 3: Soft delete the credential
        Credential credential = credentialOptional.get();
        credential.setDeleted(true);
        credential.setDeletedAt(LocalDateTime.now());
        credentialRepository.save(credential);

        // Step 4: Create audit log entry within the same transaction
        auditService.logCredentialDeletion(credentialId, userId);

        return "SUCCESS";
    }

    /**
     * Restore a soft-deleted credential
     *
     * @param credentialId the ID of the credential to restore
     * @param userId the ID of the user
     * @return "SUCCESS" if restored, "NOT_FOUND" if credential doesn't exist
     */
    @Transactional
    public String restoreCredential(Long credentialId, Long userId) {
        
        // Find credential including deleted ones
        Optional<Credential> credentialOptional = credentialRepository
                .findByCredentialIdAndUserIdIncludingDeleted(credentialId, userId);

        if (credentialOptional.isEmpty()) {
            return "NOT_FOUND";
        }

        Credential credential = credentialOptional.get();
        
        // Check if it was actually deleted
        if (!credential.isDeleted()) {
            return "NOT_DELETED"; // Credential is not in trash
        }

        // Restore the credential
        credential.setDeleted(false);
        credential.setDeletedAt(null);
        credentialRepository.save(credential);

        // Create audit log
        auditService.createAuditLog("RESTORE", "CREDENTIAL", credentialId, userId, "Credential restored from trash");

        return "SUCCESS";
    }

    /**
     * Get all soft-deleted credentials (trash)
     *
     * @param userId the ID of the user
     * @return List of deleted credentials
     */
    @Transactional(readOnly = true)
    public List<CredentialResponse> getDeletedCredentials(Long userId) throws Exception {
        List<Credential> deletedCredentials = credentialRepository.findDeletedByUserId(userId);
        List<CredentialResponse> responseList = new ArrayList<>();

        for (Credential credential : deletedCredentials) {
            // Decrypt the password for the response
            String decryptedPassword = AESUtil.decrypt(credential.getEncryptedPassword());

            CredentialResponse response = new CredentialResponse(
                credential.getCredentialId(),
                credential.getUserId(),
                credential.getServiceName(),
                credential.getUsername(),
                decryptedPassword,
                credential.getCreatedAt(),
                credential.getUpdatedAt()
            );

            responseList.add(response);
        }

        return responseList;
    }

    /**
     * Permanently delete a credential and all associated data
     *
     * @param credentialId the ID of the credential to permanently delete
     * @param userId the ID of the user
     * @return "SUCCESS" if deleted, "NOT_FOUND" if credential doesn't exist
     */
    @Transactional
    public String permanentlyDeleteCredential(Long credentialId, Long userId) {
        
        // Find credential including deleted ones
        Optional<Credential> credentialOptional = credentialRepository
                .findByCredentialIdAndUserIdIncludingDeleted(credentialId, userId);

        if (credentialOptional.isEmpty()) {
            return "NOT_FOUND";
        }

        Credential credential = credentialOptional.get();

        // Delete password history
        passwordHistoryService.deletePasswordHistory(credentialId);

        // Note: Audit logs are preserved for compliance
        // Create final audit log before deletion
        auditService.createAuditLog("PERMANENT_DELETE", "CREDENTIAL", credentialId, userId, 
            "Credential permanently deleted");

        // Permanently delete the credential
        credentialRepository.delete(credential);

        return "SUCCESS";
    }

    /**
     * Search credentials by term (searches in serviceName and username)
     * 
     * @param userId the ID of the user
     * @param searchTerm the term to search for
     * @return List of matching CredentialResponse objects
     */
    @Transactional(readOnly = true)
    public List<CredentialResponse> searchCredentials(Long userId, String searchTerm) throws Exception {
        List<Credential> credentials = credentialRepository.searchCredentials(userId, searchTerm);
        List<CredentialResponse> responseList = new ArrayList<>();

        for (Credential credential : credentials) {
            // Decrypt the password for the response
            String decryptedPassword = AESUtil.decrypt(credential.getEncryptedPassword());

            CredentialResponse response = new CredentialResponse(
                credential.getCredentialId(),
                credential.getUserId(),
                credential.getServiceName(),
                credential.getUsername(),
                decryptedPassword,
                credential.getCreatedAt(),
                credential.getUpdatedAt()
            );

            responseList.add(response);
        }

        return responseList;
    }

    /**
     * Get credentials by category for a specific user
     * 
     * @param userId the ID of the user
     * @param category the category to filter by
     * @return List of CredentialResponse objects in the specified category
     */
    @Transactional(readOnly = true)
    public List<CredentialResponse> getCredentialsByCategory(Long userId, Category category) throws Exception {
        List<Credential> credentials = credentialRepository.findByUserIdAndCategory(userId, category);
        List<CredentialResponse> responseList = new ArrayList<>();

        for (Credential credential : credentials) {
            // Decrypt the password for the response
            String decryptedPassword = AESUtil.decrypt(credential.getEncryptedPassword());

            CredentialResponse response = new CredentialResponse(
                credential.getCredentialId(),
                credential.getUserId(),
                credential.getServiceName(),
                credential.getUsername(),
                decryptedPassword,
                credential.getCreatedAt(),
                credential.getUpdatedAt()
            );

            responseList.add(response);
        }

        return responseList;
    }

    /**
     * Get paginated and filtered credentials with dynamic sorting
     *
     * @param userId the ID of the user
     * @param category optional category filter
     * @param serviceName optional service name filter (partial match)
     * @param username optional username filter (partial match)
     * @param searchTerm optional search term (searches both service name and username)
     * @param pageable pagination and sorting parameters
     * @return Page of CredentialResponse objects
     */
    @Transactional(readOnly = true)
    public Page<CredentialResponse> getCredentialsPaginated(
            Long userId,
            Category category,
            String serviceName,
            String username,
            String searchTerm,
            Pageable pageable) throws Exception {
        
        // Build dynamic query specification
        Specification<Credential> spec = CredentialSpecification.buildSpecification(
            userId, category, serviceName, username, searchTerm
        );

        // Execute paginated query
        Page<Credential> credentialPage = credentialRepository.findAll(spec, pageable);

        // Convert to CredentialResponse DTOs with decrypted passwords
        return credentialPage.map(credential -> {
            try {
                String decryptedPassword = AESUtil.decrypt(credential.getEncryptedPassword());
                return new CredentialResponse(
                    credential.getCredentialId(),
                    credential.getUserId(),
                    credential.getServiceName(),
                    credential.getUsername(),
                    decryptedPassword,
                    credential.getCreatedAt(),
                    credential.getUpdatedAt()
                );
            } catch (Exception e) {
                throw new RuntimeException("Error decrypting password", e);
            }
        });
    }

    /**
     * Get audit trail for a specific credential
     * Shows the complete history of operations performed on a credential
     * 
     * @param credentialId the ID of the credential
     * @param userId the ID of the user (for authorization check)
     * @return List of AuditLog entries for the credential, or empty list if not authorized
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getCredentialAuditTrail(Long credentialId, Long userId) {
        // First verify that the user has access to this credential
        Optional<Credential> credentialOptional = credentialRepository.findByCredentialIdAndUserId(credentialId, userId);
        
        if (credentialOptional.isEmpty()) {
            // User doesn't have access to this credential, return empty list
            return new ArrayList<>();
        }
        
        // User has access, return the audit trail
        return auditService.getCredentialAuditTrail(credentialId);
    }
}
