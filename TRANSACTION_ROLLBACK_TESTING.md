# Transaction Rollback Testing Guide

This document provides step-by-step instructions for testing the transaction management and audit logging implementation in the SecureVault application.

## Overview

The SecureVault application now implements transaction management with audit logging for all credential operations (CREATE, UPDATE, DELETE). This testing guide verifies that:

1. **Successful operations**: Both credential and audit log are saved together
2. **Failed operations**: Both credential and audit log are rolled back together
3. **Transaction integrity**: No partial states exist in the database

## Test Scenarios

### Scenario 1: Credential Creation with Audit Log Failure

**Purpose**: Verify that when credential creation succeeds but audit log creation fails, both operations are rolled back.

**Expected Behavior**:
- Credential should NOT be saved to database
- Audit log should NOT be created
- Database counts should remain unchanged
- Exception should be thrown

**Test Steps**:

1. **Get initial counts**:
   ```
   GET /api/test/transactions/counts
   ```
   Record the `totalCredentials` and `totalAuditLogs` values.

2. **Execute test**:
   ```
   POST /api/test/transactions/create-with-audit-failure
   Content-Type: application/json
   
   {
     "userId": 1,
     "serviceName": "TestService",
     "username": "testuser",
     "password": "testpassword123"
   }
   ```

3. **Verify response**:
   - `rollbackSuccessful` should be `true`
   - `testResult` should be "PASS - Transaction rolled back successfully"
   - `exception` should contain "SIMULATED AUDIT LOG FAILURE"
   - `credentialsBefore` should equal `credentialsAfter`
   - `auditLogsBefore` should equal `auditLogsAfter`

### Scenario 2: Late Failure After Both Operations

**Purpose**: Verify that when both credential and audit log creation succeed but a later operation fails, both are rolled back.

**Expected Behavior**:
- Both credential and audit log should be rolled back
- Database counts should remain unchanged
- Exception should be thrown

**Test Steps**:

1. **Execute test**:
   ```
   POST /api/test/transactions/create-with-late-failure
   Content-Type: application/json
   
   {
     "userId": 1,
     "serviceName": "TestService2",
     "username": "testuser2",
     "password": "testpassword456"
   }
   ```

2. **Verify response**:
   - `rollbackSuccessful` should be `true`
   - `testResult` should be "PASS - Both credential and audit log rolled back successfully"
   - `exception` should contain "SIMULATED LATE FAILURE"

### Scenario 3: Credential Update with Audit Log Failure

**Purpose**: Verify that when credential update succeeds but audit log creation fails, the update is rolled back.

**Test Steps**:

1. **First, create a test credential successfully**:
   ```
   POST /api/test/transactions/create-successfully
   Content-Type: application/json
   
   {
     "userId": 1,
     "serviceName": "TestServiceForUpdate",
     "username": "updatetest",
     "password": "originalpassword"
   }
   ```

2. **Get the credential ID from user data**:
   ```
   GET /api/test/transactions/user-data/1
   ```
   Note the `credentialId` of the credential you just created.

3. **Execute update test**:
   ```
   PUT /api/test/transactions/update-with-audit-failure/{credentialId}/1
   Content-Type: application/json
   
   {
     "serviceName": "UpdatedServiceName",
     "username": "updatedusername"
   }
   ```

4. **Verify response**:
   - `rollbackSuccessful` should be `true`
   - `testResult` should be "PASS - Update transaction rolled back successfully"
   - `exception` should contain "SIMULATED UPDATE AUDIT FAILURE"

5. **Verify credential wasn't updated**:
   ```
   GET /api/test/transactions/user-data/1
   ```
   The credential should still have its original values.

### Scenario 4: Successful Operation (Control Test)

**Purpose**: Verify that normal operations work correctly when no failures occur.

**Expected Behavior**:
- Credential should be saved to database
- Audit log should be created
- Both counts should increase by 1

**Test Steps**:

1. **Execute successful creation**:
   ```
   POST /api/test/transactions/create-successfully
   Content-Type: application/json
   
   {
     "userId": 1,
     "serviceName": "SuccessfulTest",
     "username": "successuser",
     "password": "successpassword"
   }
   ```

2. **Verify response**:
   - `operationSuccessful` should be `true`
   - `testResult` should be "PASS - Credential and audit log created successfully"
   - `credentialsAfter` should be `credentialsBefore + 1`
   - `auditLogsAfter` should be `auditLogsBefore + 1`

## Verification Steps

After running the tests, verify the transaction behavior:

### 1. Database State Verification

Check that the database is in a consistent state:

```sql
-- Count credentials for test user
SELECT COUNT(*) FROM credentials WHERE user_id = 1;

-- Count audit logs for test user  
SELECT COUNT(*) FROM audit_logs WHERE performed_by = 1;

-- View all audit logs for the user
SELECT * FROM audit_logs WHERE performed_by = 1 ORDER BY timestamp DESC;
```

### 2. Application Log Verification

Check the application logs for transaction behavior:

- Look for "TEST: Credential saved with ID: X" messages
- Look for "TEST: Audit log created successfully" messages
- Verify that after rollback scenarios, no persistent changes remain

### 3. API Response Verification

Use the utility endpoints to verify state:

```bash
# Get current counts
GET /api/test/transactions/counts

# Get all data for test user
GET /api/test/transactions/user-data/1
```

## Expected Results Summary

| Test Scenario | Credential Saved | Audit Log Created | Exception Thrown | Database Consistent |
|---------------|------------------|-------------------|------------------|-------------------|
| Audit Failure | ❌ No | ❌ No | ✅ Yes | ✅ Yes |
| Late Failure  | ❌ No | ❌ No | ✅ Yes | ✅ Yes |
| Update Failure| ❌ No Change | ❌ No | ✅ Yes | ✅ Yes |
| Success Case  | ✅ Yes | ✅ Yes | ❌ No | ✅ Yes |

## Cleanup

After testing, clean up the test data:

```
DELETE /api/test/transactions/cleanup/1
```

This will remove all test credentials and audit logs for user ID 1.

## Troubleshooting

### Common Issues

1. **Rollback not occurring**:
   - Verify `@Transactional` annotations are present
   - Check that exceptions are runtime exceptions (not checked exceptions)
   - Ensure proper transaction propagation settings

2. **Audit logs not created**:
   - Verify `AuditService` is properly injected
   - Check database table creation
   - Verify transaction propagation in `AuditService`

3. **Test endpoints not accessible**:
   - Ensure Spring Security allows access to `/api/test/**` endpoints
   - Verify controller is properly annotated and component scanned

### Database Schema Verification

Ensure the audit_logs table exists:

```sql
CREATE TABLE IF NOT EXISTS audit_logs (
    audit_id BIGSERIAL PRIMARY KEY,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    performed_by BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    details VARCHAR(500)
);
```

## Security Note

⚠️ **WARNING**: The test endpoints in this guide should ONLY be used in development and testing environments. They intentionally cause failures and should NEVER be deployed to production.

Remove or disable the `TransactionTestController` before production deployment.