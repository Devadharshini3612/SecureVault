package com.securevault.mapper;

import com.securevault.dto.CreateCredentialRequest;
import com.securevault.dto.CredentialResponse;
import com.securevault.dto.UpdateCredentialRequest;
import com.securevault.entity.Credential;

/**
 * CredentialMapper
 *
 * Manual mapper class for converting between Credential entity and Credential DTOs.
 * Handles the conversion logic between presentation and persistence layers.
 *
 * Note: Password encryption/decryption is handled in the service layer,
 * not in the mapper, as it requires the AESUtil utility.
 */
public class CredentialMapper {

    /**
     * Convert CreateCredentialRequest DTO to Credential entity
     * Note: Password must be encrypted before calling this method
     *
     * @param request the create request DTO
     * @param encryptedPassword the already-encrypted password
     * @return Credential entity ready for persistence
     */
    public static Credential toEntity(CreateCredentialRequest request, String encryptedPassword) {
        if (request == null) {
            return null;
        }

        Credential credential = new Credential(
            request.getUserId(),
            request.getServiceName(),
            request.getUsername(),
            encryptedPassword
        );

        if (request.getCategory() != null) {
            credential.setCategory(request.getCategory());
        }

        return credential;
    }

    /**
     * Convert Credential entity to CredentialResponse DTO
     * Note: Password must be decrypted before calling this method
     *
     * @param credential the credential entity
     * @param decryptedPassword the already-decrypted password
     * @return CredentialResponse DTO safe for API responses
     */
    public static CredentialResponse toResponse(Credential credential, String decryptedPassword) {
        if (credential == null) {
            return null;
        }

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
     * Update existing Credential entity with data from UpdateCredentialRequest
     * Note: Password encryption is handled in the service layer
     *
     * @param credential the existing credential entity
     * @param request the update request
     * @param newEncryptedPassword the newly encrypted password (if password is being updated)
     * @return true if any fields were updated, false otherwise
     */
    public static boolean updateEntityFromRequest(Credential credential, UpdateCredentialRequest request, 
                                                  String newEncryptedPassword) {
        if (credential == null || request == null) {
            return false;
        }

        boolean hasUpdates = false;

        if (request.getServiceName() != null && !request.getServiceName().isEmpty()) {
            credential.setServiceName(request.getServiceName());
            hasUpdates = true;
        }

        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            credential.setUsername(request.getUsername());
            hasUpdates = true;
        }

        if (newEncryptedPassword != null && !newEncryptedPassword.isEmpty()) {
            credential.setEncryptedPassword(newEncryptedPassword);
            hasUpdates = true;
        }

        if (request.getCategory() != null) {
            credential.setCategory(request.getCategory());
            hasUpdates = true;
        }

        return hasUpdates;
    }

    /**
     * Build update details string for audit logging
     *
     * @param request the update request
     * @return formatted string describing what fields were updated
     */
    public static String buildUpdateDetails(UpdateCredentialRequest request) {
        StringBuilder details = new StringBuilder("Updated fields: ");
        boolean hasFields = false;

        if (request.getServiceName() != null && !request.getServiceName().isEmpty()) {
            details.append("serviceName, ");
            hasFields = true;
        }

        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            details.append("username, ");
            hasFields = true;
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            details.append("password, ");
            hasFields = true;
        }

        if (request.getCategory() != null) {
            details.append("category, ");
            hasFields = true;
        }

        if (!hasFields) {
            return "No fields updated";
        }

        // Remove trailing comma and space
        return details.toString().replaceAll(", $", "");
    }
}
