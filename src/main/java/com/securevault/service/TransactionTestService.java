package com.securevault.service;

import com.securevault.dto.CreateCredentialRequest;
import com.securevault.dto.UpdateCredentialRequest;
import com.securevault.entity.AuditLog;
import com.securevault.entity.Credential;
import com.securevault.repository.AuditLogRepository;
import com.securevault.repository.CredentialRepository;
import com.securevault.util.AESUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * TransactionTestService
 *
 * This service is specifically designed to test transaction rollback behavior
 * in the SecureVault application. It provides methods that intentionally fail
 * during different stages of credential operations to verify that transactions
 * are properly rolled back when exceptions occur.
 *
 * Test Scenarios:
 * 1. Credential creation succeeds, audit log creation fails → Both should rollback
 * 2. Credential update succeeds, audit log creation fails → Both should rollback
 * 3. Audit log creation succeeds, credential operation fails → Both should rollback
 *
 * This service should ONLY be used for testing and development purposes.
 */
@Service
public class TransactionTestService {

    private final CredentialRepository credentialRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;

    /**
     * Constructor injection - Spring automatically injects dependencies
     */
    public TransactionTestService(
            CredentialRepository credentialRepository,
            AuditLogRepository auditLogRepository,
            AuditService auditService) {
        this.credentialRepository = credentialRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditService = auditService;
    }

    // ========== Test Scenario 1: Fail After Credential Creation ==========

    /**
     * Test transaction rollback by creating a credential successfully,
     * then forcing an exception during audit log creation.
     *
     * Expected Result:
     * - Credential should NOT be saved to database
     * - Audit log should NOT be saved to database
     * - Exception should be thrown to caller
     *
     * @param request the credential creation request
     * @return never returns normally, always throws exception
     * @throws Exception always throws to simulate audit log failure
     */
    @Transactional
    public String testCreateCredentialWithAuditFailure(CreateCredentialRequest request) throws Exception {
        
        // Step 1: Create and save credential (this should succeed)
        String encryptedPassword = AESUtil.encrypt(request.getPassword());
        Credential credential = new Credential(
            request.getUserId(),
            request.getServiceName(),
            request.getUsername(),
            encryptedPassword
        );
        
        Credential savedCredential = credentialRepository.save(credential);
        System.out.println("TEST: Credential saved with ID: " + savedCredential.getCredentialId());

        // Step 2: Simulate audit log creation failure
        throw new RuntimeException("SIMULATED AUDIT LOG FAILURE - This should cause transaction rollback");
    }

    /**
     * Test transaction rollback by creating a credential and audit log successfully,
     * then forcing an exception at the end of the transaction.
     *
     * Expected Result:
     * - Credential should NOT be saved to database
     * - Audit log should NOT be saved to database
     * - Exception should be thrown to caller
     *
     * @param request the credential creation request
     * @return never returns normally, always throws exception
     * @throws Exception always throws to simulate transaction failure
     */
    @Transactional
    public String testCreateCredentialWithLateFailure(CreateCredentialRequest request) throws Exception {
        
        // Step 1: Create and save credential
        String encryptedPassword = AESUtil.encrypt(request.getPassword());
        Credential credential = new Credential(
            request.getUserId(),
            request.getServiceName(),
            request.getUsername(),
            encryptedPassword
        );
        
        Credential savedCredential = credentialRepository.save(credential);
        System.out.println("TEST: Credential saved with ID: " + savedCredential.getCredentialId());

        // Step 2: Create audit log (this should succeed)
        auditService.logCredentialCreation(savedCredential.getCredentialId(), request.getUserId());
        System.out.println("TEST: Audit log created successfully");

        // Step 3: Simulate failure after both operations
        throw new RuntimeException("SIMULATED LATE FAILURE - Both credential and audit log should rollback");
    }

    // ========== Test Scenario 2: Fail During Update Operation ==========

    /**
     * Test transaction rollback during credential update by forcing
     * an exception after the credential is updated but before audit log creation.
     *
     * Expected Result:
     * - Credential should remain unchanged in database
     * - No audit log should be created
     * - Exception should be thrown to caller
     *
     * @param credentialId the ID of the credential to update
     * @param userId the ID of the user
     * @param request the update request
     * @return never returns normally, always throws exception
     * @throws Exception always throws to simulate update failure
     */
    @Transactional
    public String testUpdateCredentialWithAuditFailure(Long credentialId, Long userId, UpdateCredentialRequest request) throws Exception {
        
        // Step 1: Find and update credential
        Optional<Credential> credentialOptional = credentialRepository.findByCredentialIdAndUserId(credentialId, userId);
        
        if (credentialOptional.isEmpty()) {
            return "NOT_FOUND";
        }

        Credential credential = credentialOptional.get();
        String originalServiceName = credential.getServiceName();
        
        // Update the credential
        if (request.getServiceName() != null && !request.getServiceName().isEmpty()) {
            credential.setServiceName(request.getServiceName());
        }
        
        credentialRepository.save(credential);
        System.out.println("TEST: Credential updated - serviceName changed from '" + originalServiceName + 
                          "' to '" + credential.getServiceName() + "'");

        // Step 2: Simulate audit log failure
        throw new RuntimeException("SIMULATED UPDATE AUDIT FAILURE - Credential update should rollback");
    }

    // ========== Test Scenario 3: Test Successful Operations ==========

    /**
     * Test that normal operations work correctly when no exceptions occur.
     * This serves as a control test to verify the testing infrastructure.
     *
     * Expected Result:
     * - Credential should be saved to database
     * - Audit log should be saved to database
     * - Method should return "SUCCESS"
     *
     * @param request the credential creation request
     * @return "SUCCESS" if all operations complete successfully
     * @throws Exception if encryption fails
     */
    @Transactional
    public String testCreateCredentialSuccessfully(CreateCredentialRequest request) throws Exception {
        
        // Step 1: Create and save credential
        String encryptedPassword = AESUtil.encrypt(request.getPassword());
        Credential credential = new Credential(
            request.getUserId(),
            request.getServiceName(),
            request.getUsername(),
            encryptedPassword
        );
        
        Credential savedCredential = credentialRepository.save(credential);
        System.out.println("TEST: Credential saved successfully with ID: " + savedCredential.getCredentialId());

        // Step 2: Create audit log
        auditService.logCredentialCreation(savedCredential.getCredentialId(), request.getUserId());
        System.out.println("TEST: Audit log created successfully");

        return "SUCCESS";
    }

    // ========== Helper Methods for Testing ==========

    /**
     * Count total number of credentials in database
     * Used to verify rollback behavior
     *
     * @return total count of credentials
     */
    public long countTotalCredentials() {
        return credentialRepository.count();
    }

    /**
     * Count total number of audit logs in database
     * Used to verify rollback behavior
     *
     * @return total count of audit logs
     */
    public long countTotalAuditLogs() {
        return auditLogRepository.count();
    }

    /**
     * Get all credentials for a user (for testing purposes)
     *
     * @param userId the user ID
     * @return list of credentials
     */
    public List<Credential> getAllCredentialsForUser(Long userId) {
        return credentialRepository.findByUserId(userId);
    }

    /**
     * Get all audit logs for a user (for testing purposes)
     *
     * @param userId the user ID
     * @return list of audit logs
     */
    public List<AuditLog> getAllAuditLogsForUser(Long userId) {
        return auditLogRepository.findByPerformedByOrderByTimestampDesc(userId);
    }

    /**
     * Clean up test data - removes all credentials and audit logs for a user
     * WARNING: This method permanently deletes data. Use only for testing.
     *
     * @param userId the user ID to clean up data for
     */
    @Transactional
    public void cleanupTestDataForUser(Long userId) {
        // Delete audit logs first (to avoid foreign key issues if they exist)
        List<AuditLog> auditLogs = auditLogRepository.findByPerformedByOrderByTimestampDesc(userId);
        auditLogRepository.deleteAll(auditLogs);
        
        // Delete credentials
        List<Credential> credentials = credentialRepository.findByUserId(userId);
        credentialRepository.deleteAll(credentials);
        
        System.out.println("TEST CLEANUP: Removed " + auditLogs.size() + " audit logs and " + 
                          credentials.size() + " credentials for user " + userId);
    }
}