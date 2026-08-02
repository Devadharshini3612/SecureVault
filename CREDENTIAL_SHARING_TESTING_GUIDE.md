# Credential Sharing - Comprehensive Testing Guide

**Version**: 1.0  
**Last Updated**: July 17, 2026  
**Feature**: Credential Sharing with Permission Management

---

## Table of Contents

1. [Overview](#overview)
2. [Permission Model](#permission-model)
3. [Setup Instructions](#setup-instructions)
4. [Test Scenarios](#test-scenarios)
5. [Authorization Testing](#authorization-testing)
6. [Error Testing](#error-testing)
7. [Security Testing](#security-testing)

---

## Overview

The Credential Sharing feature allows users to securely share credentials with other registered users while maintaining proper access control through a permission-based system.

### Key Features

- **Two Permission Levels**: READ and EDIT
- **Owner Control**: Only credential owners can share
- **Access Management**: Share, revoke, and update permissions
- **Security**: Authorization checks on all operations
- **Audit Trail**: All sharing activities logged

---

## Permission Model

### READ Permission

**Can:**
- ✅ View shared credential
- ✅ See all credential details including password

**Cannot:**
- ❌ Modify credential
- ❌ Delete credential
- ❌ Reshare with others
- ❌ Change ownership

### EDIT Permission

**Can:**
- ✅ View credential
- ✅ Update service name, username, password, category
- ✅ All operations that READ can perform

**Cannot:**
- ❌ Delete credential ownership
- ❌ Transfer ownership
- ❌ Permanently delete credential
- ❌ Reshare with others

### Owner Permissions

**Can:**
- ✅ All EDIT permissions
- ✅ Share with other users
- ✅ Revoke shares
- ✅ Update share permissions
- ✅ Delete credential (soft and permanent)
- ✅ Restore deleted credential

---

## Setup Instructions

### Prerequisites

1. **Two or more test users registered**
2. **At least one credential created by User A**
3. **Postman or REST client ready**

### Create Test Users

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

# User A (Owner)
{
  "name": "Alice Owner",
  "email": "alice@example.com",
  "password": "AlicePass123!"
}

# User B (Shared With - READ)
{
  "name": "Bob Reader",
  "email": "bob@example.com",
  "password": "BobPass123!"
}

# User C (Shared With - EDIT)
{
  "name": "Charlie Editor",
  "email": "charlie@example.com",
  "password": "CharliePass123!"
}

# User D (No Access)
{
  "name": "Dave NoAccess",
  "email": "dave@example.com",
  "password": "DavePass123!"
}
```

### Login and Get Tokens

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "alice@example.com",
  "password": "AlicePass123!"
}
```

**Save tokens in Postman environment:**
- `alice_token` - Alice's JWT token
- `bob_token` - Bob's JWT token
- `charlie_token` - Charlie's JWT token
- `dave_token` - Dave's JWT token

### Create Test Credential

```http
POST http://localhost:8080/api/credentials/create
Authorization: Bearer {{alice_token}}
Content-Type: application/json

{
  "serviceName": "Shared Gmail Account",
  "username": "team@company.com",
  "password": "TeamPassword123!",
  "category": "WORK"
}
```

**Save the credential ID:** `shared_credential_id`

---

## Test Scenarios

### Scenario 1: Complete Sharing Workflow

**Flow**: Share → View → Update Permission → Revoke

#### Step 1: Alice Shares with Bob (READ Permission)

```http
POST http://localhost:8080/api/share
Authorization: Bearer {{alice_token}}
Content-Type: application/json

{
  "credentialId": 1,
  "sharedWithUserId": 2,
  "permission": "READ"
}
```

**Expected Response**: 201 Created
```json
{
  "success": true,
  "message": "Credential shared successfully",
  "data": {
    "shareId": 1,
    "credentialId": 1,
    "serviceName": "Shared Gmail Account",
    "ownerId": 1,
    "ownerName": "Alice Owner",
    "ownerEmail": "alice@example.com",
    "sharedWithUserId": 2,
    "sharedWithUserName": "Bob Reader",
    "sharedWithUserEmail": "bob@example.com",
    "permission": "READ",
    "sharedAt": "2026-07-17T10:00:00",
    "active": true
  },
  "timestamp": "2026-07-17T10:00:00"
}
```

#### Step 2: Bob Views Shared Credentials

```http
GET http://localhost:8080/api/share/received
Authorization: Bearer {{bob_token}}
```

**Expected Response**: 200 OK
```json
{
  "success": true,
  "message": "Shared credentials retrieved successfully",
  "data": [
    {
      "shareId": 1,
      "credentialId": 1,
      "serviceName": "Shared Gmail Account",
      "ownerId": 1,
      "ownerName": "Alice Owner",
      "ownerEmail": "alice@example.com",
      "sharedWithUserId": 2,
      "sharedWithUserName": "Bob Reader",
      "sharedWithUserEmail": "bob@example.com",
      "permission": "READ",
      "sharedAt": "2026-07-17T10:00:00",
      "active": true
    }
  ],
  "timestamp": "2026-07-17T10:01:00"
}
```

#### Step 3: Bob Accesses Shared Credential

```http
GET http://localhost:8080/api/credentials/1
Authorization: Bearer {{bob_token}}
```

**Expected Response**: 200 OK
```json
{
  "success": true,
  "message": "Credential retrieved successfully",
  "data": {
    "credentialId": 1,
    "userId": 1,
    "serviceName": "Shared Gmail Account",
    "username": "team@company.com",
    "password": "TeamPassword123!",
    "createdAt": "2026-07-17T09:00:00",
    "updatedAt": "2026-07-17T09:00:00"
  },
  "timestamp": "2026-07-17T10:02:00"
}
```

#### Step 4: Bob Tries to Update (Should Fail - READ Only)

```http
PUT http://localhost:8080/api/credentials/update/1
Authorization: Bearer {{bob_token}}
Content-Type: application/json

{
  "password": "NewPassword456!"
}
```

**Expected Response**: 403 Forbidden
```json
{
  "success": false,
  "message": "You only have READ permission. Cannot modify this credential.",
  "timestamp": "2026-07-17T10:03:00"
}
```

#### Step 5: Alice Updates Bob's Permission to EDIT

```http
PUT http://localhost:8080/api/share/1
Authorization: Bearer {{alice_token}}
Content-Type: application/json

{
  "permission": "EDIT"
}
```

**Expected Response**: 200 OK
```json
{
  "success": true,
  "message": "Share permission updated successfully",
  "data": {
    "shareId": 1,
    "credentialId": 1,
    "permission": "EDIT",
    ...
  },
  "timestamp": "2026-07-17T10:04:00"
}
```

#### Step 6: Bob Updates Credential (Should Succeed Now)

```http
PUT http://localhost:8080/api/credentials/update/1
Authorization: Bearer {{bob_token}}
Content-Type: application/json

{
  "password": "NewPassword456!"
}
```

**Expected Response**: 200 OK
```json
{
  "success": true,
  "message": "Credential updated successfully",
  "data": {
    "credentialId": 1,
    "password": "NewPassword456!",
    ...
  },
  "timestamp": "2026-07-17T10:05:00"
}
```

#### Step 7: Alice Revokes Bob's Access

```http
DELETE http://localhost:8080/api/share/1
Authorization: Bearer {{alice_token}}
```

**Expected Response**: 200 OK
```json
{
  "success": true,
  "message": "Share revoked successfully",
  "timestamp": "2026-07-17T10:06:00"
}
```

#### Step 8: Bob Tries to Access After Revocation (Should Fail)

```http
GET http://localhost:8080/api/credentials/1
Authorization: Bearer {{bob_token}}
```

**Expected Response**: 404 Not Found
```json
{
  "success": false,
  "message": "Credential not found",
  "timestamp": "2026-07-17T10:07:00"
}
```

---

### Scenario 2: Multiple Users with Different Permissions

#### Step 1: Alice Shares with Bob (READ) and Charlie (EDIT)

```http
# Share with Bob - READ
POST http://localhost:8080/api/share
Authorization: Bearer {{alice_token}}
Content-Type: application/json

{
  "credentialId": 1,
  "sharedWithUserId": 2,
  "permission": "READ"
}

# Share with Charlie - EDIT
POST http://localhost:8080/api/share
Authorization: Bearer {{alice_token}}
Content-Type: application/json

{
  "credentialId": 1,
  "sharedWithUserId": 3,
  "permission": "EDIT"
}
```

#### Step 2: Bob Views (READ Permission)

```http
GET http://localhost:8080/api/credentials/1
Authorization: Bearer {{bob_token}}
```

**Expected**: ✅ 200 OK - Can view

#### Step 3: Bob Tries to Update (Should Fail)

```http
PUT http://localhost:8080/api/credentials/update/1
Authorization: Bearer {{bob_token}}
Content-Type: application/json

{
  "serviceName": "Updated Name"
}
```

**Expected**: ❌ 403 Forbidden - READ permission insufficient

#### Step 4: Charlie Updates (EDIT Permission)

```http
PUT http://localhost:8080/api/credentials/update/1
Authorization: Bearer {{charlie_token}}
Content-Type: application/json

{
  "serviceName": "Updated by Charlie"
}
```

**Expected**: ✅ 200 OK - Can update with EDIT permission

#### Step 5: Dave (No Access) Tries to View

```http
GET http://localhost:8080/api/credentials/1
Authorization: Bearer {{dave_token}}
```

**Expected**: ❌ 404 Not Found - No access

---

### Scenario 3: Sharing Edge Cases

#### Test 1: Share with Yourself (Should Fail)

```http
POST http://localhost:8080/api/share
Authorization: Bearer {{alice_token}}
Content-Type: application/json

{
  "credentialId": 1,
  "sharedWithUserId": 1,
  "permission": "READ"
}
```

**Expected Response**: 400 Bad Request
```json
{
  "success": false,
  "message": "Cannot share credential with yourself",
  "timestamp": "2026-07-17T10:10:00"
}
```

#### Test 2: Share Non-Existent Credential

```http
POST http://localhost:8080/api/share
Authorization: Bearer {{alice_token}}
Content-Type: application/json

{
  "credentialId": 99999,
  "sharedWithUserId": 2,
  "permission": "READ"
}
```

**Expected Response**: 404 Not Found
```json
{
  "success": false,
  "message": "Credential not found or you don't have permission to share it",
  "timestamp": "2026-07-17T10:11:00"
}
```

#### Test 3: Share with Non-Existent User

```http
POST http://localhost:8080/api/share
Authorization: Bearer {{alice_token}}
Content-Type: application/json

{
  "credentialId": 1,
  "sharedWithUserId": 99999,
  "permission": "READ"
}
```

**Expected Response**: 404 Not Found
```json
{
  "success": false,
  "message": "User with ID 99999 not found",
  "timestamp": "2026-07-17T10:12:00"
}
```

#### Test 4: Duplicate Share (Should Fail)

```http
# First share
POST http://localhost:8080/api/share
Authorization: Bearer {{alice_token}}
Content-Type: application/json

{
  "credentialId": 1,
  "sharedWithUserId": 2,
  "permission": "READ"
}

# Try to share again
POST http://localhost:8080/api/share
Authorization: Bearer {{alice_token}}
Content-Type: application/json

{
  "credentialId": 1,
  "sharedWithUserId": 2,
  "permission": "EDIT"
}
```

**Expected Response**: 400 Bad Request
```json
{
  "success": false,
  "message": "This credential is already shared with this user",
  "timestamp": "2026-07-17T10:13:00"
}
```

#### Test 5: Non-Owner Tries to Share

```http
POST http://localhost:8080/api/share
Authorization: Bearer {{bob_token}}
Content-Type: application/json

{
  "credentialId": 1,
  "sharedWithUserId": 3,
  "permission": "READ"
}
```

**Expected Response**: 404 Not Found
```json
{
  "success": false,
  "message": "Credential not found or you don't have permission to share it",
  "timestamp": "2026-07-17T10:14:00"
}
```

---

## Authorization Testing

### Test Matrix

| User | Credential Owner | Permission | View | Update | Delete | Share | Revoke |
|------|-----------------|-----------|------|--------|--------|-------|--------|
| Alice | ✅ Yes | OWNER | ✅ | ✅ | ✅ | ✅ | ✅ |
| Bob | ❌ No | READ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Charlie | ❌ No | EDIT | ✅ | ✅ | ❌ | ❌ | ❌ |
| Dave | ❌ No | NONE | ❌ | ❌ | ❌ | ❌ | ❌ |

### Authorization Flow

```
Request to Access Credential
         ↓
Is User Owner?
    ↓           ↓
   Yes          No
    ↓           ↓
  ALLOW    Is Shared?
              ↓     ↓
             Yes    No
              ↓     ↓
         Check   DENY (403)
         Permission
              ↓
         READ or EDIT?
           ↓        ↓
         READ      EDIT
           ↓        ↓
      View Only  View + Update
```

### Test Cases

#### TC1: Owner Accessing Own Credential
- **User**: Alice (Owner)
- **Action**: GET /api/credentials/1
- **Expected**: ✅ 200 OK

#### TC2: Shared User with READ Permission
- **User**: Bob (READ)
- **Action**: GET /api/credentials/1
- **Expected**: ✅ 200 OK

#### TC3: Shared User with READ Trying to Update
- **User**: Bob (READ)
- **Action**: PUT /api/credentials/update/1
- **Expected**: ❌ 403 Forbidden

#### TC4: Shared User with EDIT Permission
- **User**: Charlie (EDIT)
- **Action**: PUT /api/credentials/update/1
- **Expected**: ✅ 200 OK

#### TC5: User with No Access
- **User**: Dave (NONE)
- **Action**: GET /api/credentials/1
- **Expected**: ❌ 404 Not Found

#### TC6: Shared User Trying to Delete
- **User**: Charlie (EDIT)
- **Action**: DELETE /api/credentials/delete/1
- **Expected**: ❌ 404 Not Found (only owner can delete)

#### TC7: Shared User Trying to Share
- **User**: Bob (READ)
- **Action**: POST /api/share
- **Expected**: ❌ 404 Not Found (only owner can share)

---

## Error Testing

### Validation Errors

#### Missing Required Fields

```http
POST http://localhost:8080/api/share
Authorization: Bearer {{alice_token}}
Content-Type: application/json

{
  "credentialId": 1
}
```

**Expected**: 400 Bad Request
```json
{
  "success": false,
  "message": "Validation failed for one or more fields",
  "errors": {
    "sharedWithUserId": ["Shared with user ID is required"],
    "permission": ["Permission is required"]
  },
  "timestamp": "2026-07-17T10:15:00"
}
```

#### Invalid Permission Value

```http
POST http://localhost:8080/api/share
Authorization: Bearer {{alice_token}}
Content-Type: application/json

{
  "credentialId": 1,
  "sharedWithUserId": 2,
  "permission": "INVALID"
}
```

**Expected**: 400 Bad Request - Enum validation error

### Authorization Errors

#### Missing JWT Token

```http
GET http://localhost:8080/api/share/received
```

**Expected**: 401 Unauthorized

#### Invalid/Expired JWT Token

```http
GET http://localhost:8080/api/share/received
Authorization: Bearer invalid_token_here
```

**Expected**: 401 Unauthorized

#### Access Another User's Share

```http
# Alice creates share with Bob
# Charlie tries to modify Alice's share

PUT http://localhost:8080/api/share/1
Authorization: Bearer {{charlie_token}}
Content-Type: application/json

{
  "permission": "EDIT"
}
```

**Expected**: 403 Forbidden
```json
{
  "success": false,
  "message": "Share not found or you don't have permission to modify it",
  "timestamp": "2026-07-17T10:16:00"
}
```

---

## Security Testing

### Test 1: Soft-Deleted Credentials Not Shareable

```http
# Alice deletes credential (soft delete)
DELETE http://localhost:8080/api/credentials/delete/1
Authorization: Bearer {{alice_token}}

# Alice tries to share deleted credential
POST http://localhost:8080/api/share
Authorization: Bearer {{alice_token}}
Content-Type: application/json

{
  "credentialId": 1,
  "sharedWithUserId": 2,
  "permission": "READ"
}
```

**Expected**: 400 Bad Request
```json
{
  "success": false,
  "message": "Cannot share a deleted credential",
  "timestamp": "2026-07-17T10:17:00"
}
```

### Test 2: Shared Credentials Don't Appear After Owner Deletes

```http
# Alice shares with Bob
POST http://localhost:8080/api/share
Authorization: Bearer {{alice_token}}
Content-Type: application/json

{
  "credentialId": 1,
  "sharedWithUserId": 2,
  "permission": "READ"
}

# Bob can access
GET http://localhost:8080/api/credentials/1
Authorization: Bearer {{bob_token}}
# Expected: 200 OK

# Alice deletes credential
DELETE http://localhost:8080/api/credentials/delete/1
Authorization: Bearer {{alice_token}}

# Bob tries to access (should fail)
GET http://localhost:8080/api/credentials/1
Authorization: Bearer {{bob_token}}
# Expected: 404 Not Found
```

### Test 3: Revoked Shares Immediately Lose Access

```http
# Alice shares with Bob
# Bob can access
# Alice revokes
DELETE http://localhost:8080/api/share/1
Authorization: Bearer {{alice_token}}

# Bob tries immediately
GET http://localhost:8080/api/credentials/1
Authorization: Bearer {{bob_token}}
# Expected: 404 Not Found (immediate effect)
```

### Test 4: Password History Shared Users

```http
# Alice updates password 5 times
# Alice shares with Charlie (EDIT)
# Charlie tries to use old password (should fail)

PUT http://localhost:8080/api/credentials/update/1
Authorization: Bearer {{charlie_token}}
Content-Type: application/json

{
  "password": "OldPasswordAlreadyUsed"
}
```

**Expected**: 409 Conflict
```json
{
  "success": false,
  "message": "Password was recently used. Please choose a different password.",
  "timestamp": "2026-07-17T10:18:00"
}
```

---

## Performance Testing

### Test 1: Share with Multiple Users

Create 10-20 shares for the same credential and measure:
- Share creation time (should be < 500ms each)
- Shared list retrieval time (should be < 1000ms)

### Test 2: Concurrent Access

- 5 shared users access the same credential simultaneously
- All should succeed without errors
- Response times should be consistent

---

## Regression Testing Checklist

After implementing credential sharing, verify:

- [ ] Existing credential CRUD operations still work
- [ ] Owners can access own credentials
- [ ] Non-owners cannot access credentials without share
- [ ] Password history still enforced for shared users
- [ ] Soft delete still works for shared credentials
- [ ] Pagination includes only accessible credentials
- [ ] Search excludes inaccessible credentials
- [ ] Audit logs record sharing activities
- [ ] JWT authentication still required
- [ ] Validation on all endpoints

---

## Summary of HTTP Status Codes

| Code | Meaning | When |
|------|---------|------|
| 200 | OK | Successful operation |
| 201 | Created | Share created successfully |
| 400 | Bad Request | Validation error, business rule violation |
| 401 | Unauthorized | Missing/invalid JWT token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found or no access |
| 409 | Conflict | Duplicate share attempt |
| 500 | Internal Server Error | Unexpected error |

---

## Postman Collection Structure

```
SecureVault API/
├── Credential Sharing/
│   ├── Share Credential (READ)
│   ├── Share Credential (EDIT)
│   ├── Get Shared With Me
│   ├── Update Share Permission
│   ├── Revoke Share
│   ├── Error Cases/
│   │   ├── Share with Self
│   │   ├── Duplicate Share
│   │   ├── Share Deleted Credential
│   │   └── Non-Owner Shares
│   └── Authorization Tests/
│       ├── READ User Views
│       ├── READ User Updates (Fail)
│       ├── EDIT User Updates
│       └── No Access User (Fail)
```

---

*Credential Sharing Testing Guide v1.0 - Complete Test Coverage*
