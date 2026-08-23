package com.securevault.controller;

import com.securevault.dto.CreateCredentialRequest;
import com.securevault.dto.UpdateCredentialRequest;
import com.securevault.entity.AuditLog;
import com.securevault.entity.Credential;
import com.securevault.service.TransactionTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TransactionTestController
 *
 * REST controller for testing transaction rollback behavior in the SecureVault application.
 * This controller provides endpoints to verify that credential operations and audit logs
 * are properly rolled back together when exceptions occur.
 *
 * WARNING: This controller should ONLY be used in development/testing environments.
 * DO NOT deploy this to production as it contains methods that intentionally cause failures.
 *
 * Test Flow:
 * 1. Check initial counts of credentials and audit logs
 * 2. Attempt operations that should fail and rollback
 * 3. Verify that counts remain unchanged after rollback
 * 4. Test successful operations to ensure normal flow works
 */
@RestController
@RequestMapping("/api/test/transactions")
public class TransactionTestController {

    @Autowired
    private TransactionTestService transactionTestService;

    // ========== Transaction Rollback Test Endpoints ==========

    /**
     * Test credential creation that fails during audit log creation
     * 
     * Expected behavior:
     * - Credential creation should be rolled back
     * - No audit log should be created
     * - Database counts should remain unchanged
     *
     * POST /api/test/transactions/create-with-audit-failure
     * Body: CreateCredentialRequest
     */
    @PostMapping("/create-with-audit-failure")
    public ResponseEntity<Map<String, Object>> testCreateWithAuditFailure(@RequestBody CreateCredentialRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Get counts before the operation
        long credentialsBefore = transactionTestService.countTotalCredentials();
        long auditLogsBefore = transactionTestService.countTotalAuditLogs();
        
        response.put("credentialsBefore", credentialsBefore);
        response.put("auditLogsBefore", auditLogsBefore);
        
        try {
            // This should fail and rollback
            String result = transactionTestService.testCreateCredentialWithAuditFailure(request);
            response.put("result", result);
            response.put("exception", "No exception occurred - this is unexpected!");
            
        } catch (Exception e) {
            response.put("exception", e.getMessage());
        }
        
        // Get counts after the operation
        long credentialsAfter = transactionTestService.countTotalCredentials();
        long auditLogsAfter = transactionTestService.countTotalAuditLogs();
        
        response.put("credentialsAfter", credentialsAfter);
        response.put("auditLogsAfter", auditLogsAfter);
        
        // Verify rollback occurred
        boolean rollbackSuccessful = (credentialsBefore == credentialsAfter) && (auditLogsBefore == auditLogsAfter);
        response.put("rollbackSuccessful", rollbackSuccessful);
        
        if (rollbackSuccessful) {
            response.put("testResult", "PASS - Transaction rolled back successfully");
        } else {
            response.put("testResult", "FAIL - Transaction was not rolled back properly");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test credential creation that fails after both credential and audit log creation
     * 
     * Expected behavior:
     * - Both credential and audit log should be rolled back
     * - Database counts should remain unchanged
     *
     * POST /api/test/transactions/create-with-late-failure
     * Body: CreateCredentialRequest
     */
    @PostMapping("/create-with-late-failure")
    public ResponseEntity<Map<String, Object>> testCreateWithLateFailure(@RequestBody CreateCredentialRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        long credentialsBefore = transactionTestService.countTotalCredentials();
        long auditLogsBefore = transactionTestService.countTotalAuditLogs();
        
        response.put("credentialsBefore", credentialsBefore);
        response.put("auditLogsBefore", auditLogsBefore);
        
        try {
            String result = transactionTestService.testCreateCredentialWithLateFailure(request);
            response.put("result", result);
            response.put("exception", "No exception occurred - this is unexpected!");
            
        } catch (Exception e) {
            response.put("exception", e.getMessage());
        }
        
        long credentialsAfter = transactionTestService.countTotalCredentials();
        long auditLogsAfter = transactionTestService.countTotalAuditLogs();
        
        response.put("credentialsAfter", credentialsAfter);
        response.put("auditLogsAfter", auditLogsAfter);
        
        boolean rollbackSuccessful = (credentialsBefore == credentialsAfter) && (auditLogsBefore == auditLogsAfter);
        response.put("rollbackSuccessful", rollbackSuccessful);
        
        response.put("testResult", rollbackSuccessful ? 
            "PASS - Both credential and audit log rolled back successfully" : 
            "FAIL - Transaction was not rolled back properly");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test credential update that fails during audit log creation
     * 
     * Expected behavior:
     * - Credential update should be rolled back
     * - No audit log should be created
     * - Credential should remain in original state
     *
     * PUT /api/test/transactions/update-with-audit-failure/{credentialId}/{userId}
     * Body: UpdateCredentialRequest
     */
    @PutMapping("/update-with-audit-failure/{credentialId}/{userId}")
    public ResponseEntity<Map<String, Object>> testUpdateWithAuditFailure(
            @PathVariable Long credentialId, 
            @PathVariable Long userId, 
            @RequestBody UpdateCredentialRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Get credential state before update
        List<Credential> credentialsBefore = transactionTestService.getAllCredentialsForUser(userId);
        long auditLogsBefore = transactionTestService.countTotalAuditLogs();
        
        response.put("credentialsCountBefore", credentialsBefore.size());
        response.put("auditLogsCountBefore", auditLogsBefore);
        
        try {
            String result = transactionTestService.testUpdateCredentialWithAuditFailure(credentialId, userId, request);
            response.put("result", result);
            response.put("exception", "No exception occurred - this is unexpected!");
            
        } catch (Exception e) {
            response.put("exception", e.getMessage());
        }
        
        // Get credential state after update attempt
        List<Credential> credentialsAfter = transactionTestService.getAllCredentialsForUser(userId);
        long auditLogsAfter = transactionTestService.countTotalAuditLogs();
        
        response.put("credentialsCountAfter", credentialsAfter.size());
        response.put("auditLogsCountAfter", auditLogsAfter);
        
        boolean rollbackSuccessful = (credentialsBefore.size() == credentialsAfter.size()) && 
                                   (auditLogsBefore == auditLogsAfter);
        response.put("rollbackSuccessful", rollbackSuccessful);
        
        response.put("testResult", rollbackSuccessful ? 
            "PASS - Update transaction rolled back successfully" : 
            "FAIL - Update transaction was not rolled back properly");
        
        return ResponseEntity.ok(response);
    }

    // ========== Successful Operation Test (Control Test) ==========

    /**
     * Test credential creation that should succeed
     * This serves as a control test to verify normal operations work
     *
     * POST /api/test/transactions/create-successfully
     * Body: CreateCredentialRequest
     */
    @PostMapping("/create-successfully")
    public ResponseEntity<Map<String, Object>> testCreateSuccessfully(@RequestBody CreateCredentialRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        long credentialsBefore = transactionTestService.countTotalCredentials();
        long auditLogsBefore = transactionTestService.countTotalAuditLogs();
        
        response.put("credentialsBefore", credentialsBefore);
        response.put("auditLogsBefore", auditLogsBefore);
        
        try {
            String result = transactionTestService.testCreateCredentialSuccessfully(request);
            response.put("result", result);
            
            long credentialsAfter = transactionTestService.countTotalCredentials();
            long auditLogsAfter = transactionTestService.countTotalAuditLogs();
            
            response.put("credentialsAfter", credentialsAfter);
            response.put("auditLogsAfter", auditLogsAfter);
            
            boolean operationSuccessful = (credentialsAfter == credentialsBefore + 1) && 
                                        (auditLogsAfter == auditLogsBefore + 1);
            response.put("operationSuccessful", operationSuccessful);
            
            response.put("testResult", operationSuccessful ? 
                "PASS - Credential and audit log created successfully" : 
                "FAIL - Expected counts do not match");
                
        } catch (Exception e) {
            response.put("exception", e.getMessage());
            response.put("testResult", "FAIL - Unexpected exception occurred");
        }
        
        return ResponseEntity.ok(response);
    }

    // ========== Utility Endpoints ==========

    /**
     * Get current database counts for monitoring
     * 
     * GET /api/test/transactions/counts
     */
    @GetMapping("/counts")
    public ResponseEntity<Map<String, Object>> getCounts() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("totalCredentials", transactionTestService.countTotalCredentials());
        response.put("totalAuditLogs", transactionTestService.countTotalAuditLogs());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all data for a specific user (for verification)
     * 
     * GET /api/test/transactions/user-data/{userId}
     */
    @GetMapping("/user-data/{userId}")
    public ResponseEntity<Map<String, Object>> getUserData(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        List<Credential> credentials = transactionTestService.getAllCredentialsForUser(userId);
        List<AuditLog> auditLogs = transactionTestService.getAllAuditLogsForUser(userId);
        
        response.put("credentials", credentials);
        response.put("auditLogs", auditLogs);
        response.put("credentialCount", credentials.size());
        response.put("auditLogCount", auditLogs.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Clean up test data for a user
     * WARNING: This permanently deletes data. Use only for testing.
     * 
     * DELETE /api/test/transactions/cleanup/{userId}
     */
    @DeleteMapping("/cleanup/{userId}")
    public ResponseEntity<Map<String, String>> cleanupTestData(@PathVariable Long userId) {
        Map<String, String> response = new HashMap<>();
        
        try {
            transactionTestService.cleanupTestDataForUser(userId);
            response.put("result", "SUCCESS");
            response.put("message", "Test data cleaned up for user " + userId);
        } catch (Exception e) {
            response.put("result", "FAILURE");
            response.put("message", "Failed to clean up test data: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}