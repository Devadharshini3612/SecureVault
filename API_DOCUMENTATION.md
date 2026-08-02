# SecureVault API Documentation

**Version**: 2.0  
**Base URL**: `http://localhost:8080`  
**Authentication**: JWT Bearer Token

---

## Table of Contents

1. [Authentication APIs](#authentication-apis)
2. [Vault/Credential APIs](#vaultcredential-apis)
3. [Password Intelligence APIs](#password-intelligence-apis)
4. [Response Format](#response-format)
5. [Error Codes](#error-codes)
6. [Pagination](#pagination)

---

## Authentication APIs

### 1. Register User

**Endpoint**: `POST /api/auth/register`  
**Authentication**: Not required

**Request Body**:
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

**Validation**:
- `name`: 2-50 characters, required
- `email`: Valid email format, 5-100 characters, required
- `password`: 8-100 characters, required

**Success Response** (201 Created):
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "userId": 1,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "timestamp": "2026-07-17T10:30:00"
}
```

**Error Response** (409 Conflict):
```json
{
  "success": false,
  "message": "Email already registered",
  "timestamp": "2026-07-17T10:30:00"
}
```

---

### 2. Login

**Endpoint**: `POST /api/auth/login`  
**Authentication**: Not required

**Request Body**:
```json
{
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "email": "john@example.com",
    "userId": 1,
    "message": "Login successful"
  },
  "timestamp": "2026-07-17T10:30:00"
}
```

**Use the token in subsequent requests**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## Vault/Credential APIs

All credential endpoints require JWT authentication via `Authorization` header.

### 3. Create Credential

**Endpoint**: `POST /api/credentials/create`  
**Authentication**: Required (JWT)

**Request Body**:
```json
{
  "serviceName": "Gmail",
  "username": "john@gmail.com",
  "password": "MySecretPassword123!",
  "category": "PERSONAL"
}
```

**Categories**: `PERSONAL`, `WORK`, `DEVELOPMENT`, `SOCIAL`, `BANKING`, `ENTERTAINMENT`, `OTHER`

**Success Response** (201 Created):
```json
{
  "success": true,
  "message": "Credential created successfully",
  "data": {
    "credentialId": 1,
    "userId": 1,
    "serviceName": "Gmail",
    "username": "john@gmail.com",
    "password": "MySecretPassword123!",
    "createdAt": "2026-07-17T10:30:00",
    "updatedAt": "2026-07-17T10:30:00"
  },
  "timestamp": "2026-07-17T10:30:00"
}
```

---

### 4. Get Credential by ID

**Endpoint**: `GET /api/credentials/{id}`  
**Authentication**: Required (JWT)

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Credential retrieved successfully",
  "data": {
    "credentialId": 1,
    "userId": 1,
    "serviceName": "Gmail",
    "username": "john@gmail.com",
    "password": "MySecretPassword123!",
    "createdAt": "2026-07-17T10:30:00",
    "updatedAt": "2026-07-17T10:30:00"
  },
  "timestamp": "2026-07-17T10:30:00"
}
```

---

### 5. List All Credentials

**Endpoint**: `GET /api/credentials/list`  
**Authentication**: Required (JWT)

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Credentials retrieved successfully",
  "data": [
    {
      "credentialId": 1,
      "userId": 1,
      "serviceName": "Gmail",
      "username": "john@gmail.com",
      "password": "MySecretPassword123!",
      "createdAt": "2026-07-17T10:30:00",
      "updatedAt": "2026-07-17T10:30:00"
    }
  ],
  "timestamp": "2026-07-17T10:30:00"
}
```

---

### 6. Update Credential

**Endpoint**: `PUT /api/credentials/update/{id}`  
**Authentication**: Required (JWT)

**Request Body** (all fields optional):
```json
{
  "serviceName": "Gmail Personal",
  "username": "john.doe@gmail.com",
  "password": "NewPassword456!",
  "category": "PERSONAL"
}
```

**Note**: Password reuse prevention is enforced - cannot reuse last 5 passwords.

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Credential updated successfully",
  "data": {
    "credentialId": 1,
    "userId": 1,
    "serviceName": "Gmail Personal",
    "username": "john.doe@gmail.com",
    "password": "NewPassword456!",
    "createdAt": "2026-07-17T10:30:00",
    "updatedAt": "2026-07-17T10:35:00"
  },
  "timestamp": "2026-07-17T10:35:00"
}
```

**Error Response** (409 Conflict - Password Reuse):
```json
{
  "success": false,
  "message": "This password was used recently. Please choose a different password. You cannot reuse your last 5 passwords.",
  "timestamp": "2026-07-17T10:35:00"
}
```

---

### 7. Delete Credential (Soft Delete)

**Endpoint**: `DELETE /api/credentials/delete/{id}`  
**Authentication**: Required (JWT)

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Credential deleted successfully",
  "timestamp": "2026-07-17T10:40:00"
}
```

**Note**: This is a soft delete - credential moves to trash and can be restored.

---

### 8. Get Trash (Deleted Credentials)

**Endpoint**: `GET /api/credentials/trash`  
**Authentication**: Required (JWT)

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Trash retrieved successfully",
  "data": [
    {
      "credentialId": 1,
      "userId": 1,
      "serviceName": "Gmail",
      "username": "john@gmail.com",
      "password": "MySecretPassword123!",
      "createdAt": "2026-07-17T10:30:00",
      "updatedAt": "2026-07-17T10:35:00"
    }
  ],
  "timestamp": "2026-07-17T10:40:00"
}
```

---

### 9. Restore Credential

**Endpoint**: `PUT /api/credentials/{id}/restore`  
**Authentication**: Required (JWT)

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Credential restored successfully",
  "timestamp": "2026-07-17T10:45:00"
}
```

---

### 10. Permanently Delete Credential

**Endpoint**: `DELETE /api/credentials/{id}/permanent`  
**Authentication**: Required (JWT)

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Credential permanently deleted",
  "timestamp": "2026-07-17T10:50:00"
}
```

**Warning**: This action cannot be undone. Deletes credential and password history permanently.

---

### 11. Search Credentials

**Endpoint**: `GET /api/credentials/search?q={searchTerm}`  
**Authentication**: Required (JWT)

**Query Parameters**:
- `q`: Search term (searches in service name and username)

**Example**: `GET /api/credentials/search?q=gmail`

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Search completed successfully",
  "data": [
    {
      "credentialId": 1,
      "userId": 1,
      "serviceName": "Gmail",
      "username": "john@gmail.com",
      "password": "MySecretPassword123!",
      "createdAt": "2026-07-17T10:30:00",
      "updatedAt": "2026-07-17T10:30:00"
    }
  ],
  "timestamp": "2026-07-17T11:00:00"
}
```

---

### 12. Get Credentials with Pagination and Filtering

**Endpoint**: `GET /api/credentials/vault`  
**Authentication**: Required (JWT)

**Query Parameters**:
- `page`: Page number (default: 0, zero-indexed)
- `size`: Page size (default: 10, max: 100)
- `sortBy`: Field to sort by (default: `updatedAt`)
  - Options: `serviceName`, `username`, `category`, `createdAt`, `updatedAt`
- `direction`: Sort direction (default: `desc`)
  - Options: `asc`, `desc`
- `category`: Filter by category (optional)
- `serviceName`: Filter by service name partial match (optional)
- `username`: Filter by username partial match (optional)
- `search`: Search term (searches both service name and username, optional)

**Examples**:
```
GET /api/credentials/vault?page=0&size=10
GET /api/credentials/vault?page=0&size=20&sortBy=serviceName&direction=asc
GET /api/credentials/vault?category=BANKING
GET /api/credentials/vault?search=gmail&page=0&size=10
GET /api/credentials/vault?category=WORK&sortBy=serviceName&direction=asc
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Credentials retrieved successfully",
  "data": {
    "content": [
      {
        "credentialId": 1,
        "userId": 1,
        "serviceName": "Gmail",
        "username": "john@gmail.com",
        "password": "MySecretPassword123!",
        "createdAt": "2026-07-17T10:30:00",
        "updatedAt": "2026-07-17T10:30:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 25,
    "totalPages": 3,
    "first": true,
    "last": false,
    "hasNext": true,
    "hasPrevious": false
  },
  "timestamp": "2026-07-17T11:00:00"
}
```

---

## Password Intelligence APIs

### 13. Generate Password

**Endpoint**: `POST /api/password/generate`  
**Authentication**: Not required

**Request Body**:
```json
{
  "length": 16,
  "includeUppercase": true,
  "includeLowercase": true,
  "includeDigits": true,
  "includeSpecial": true
}
```

**Validation**:
- `length`: 8-128 characters

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Password generated successfully",
  "data": {
    "password": "Kx9@mP2#nQ5&vL8!",
    "length": 16,
    "strengthScore": 95,
    "strengthRating": "Very Strong"
  },
  "timestamp": "2026-07-17T11:05:00"
}
```

---

### 14. Analyze Password Strength

**Endpoint**: `POST /api/password/strength`  
**Authentication**: Not required

**Request Body**:
```json
{
  "password": "MyPassword123!"
}
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Password strength analyzed successfully",
  "data": {
    "score": 75,
    "strength": "Strong",
    "feedback": [
      "Consider increasing length to 16+ characters for better security"
    ]
  },
  "timestamp": "2026-07-17T11:10:00"
}
```

**Strength Ratings**:
- `0-19`: Very Weak
- `20-39`: Weak
- `40-59`: Moderate
- `60-79`: Strong
- `80-100`: Very Strong

---

### 15. Generate PIN

**Endpoint**: `POST /api/password/generate/pin`  
**Authentication**: Not required

**Request Body**:
```json
{
  "length": 6
}
```

**Validation**:
- `length`: 4-12 digits

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "PIN generated successfully",
  "data": {
    "pin": "738291",
    "length": 6
  },
  "timestamp": "2026-07-17T11:15:00"
}
```

---

## Response Format

All API responses follow a standardized format:

```json
{
  "success": true/false,
  "message": "Human-readable message",
  "data": { ... },
  "timestamp": "ISO 8601 timestamp",
  "errors": null (only present for validation errors)
}
```

### Success Response
- `success`: `true`
- `message`: Success message
- `data`: Response payload
- `timestamp`: Response timestamp

### Error Response
- `success`: `false`
- `message`: Error message
- `timestamp`: Response timestamp
- `errors`: Validation errors (if applicable)

---

## Error Codes

| HTTP Status | Error Type | Description |
|-------------|------------|-------------|
| 400 | Bad Request | Invalid input or validation failure |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Duplicate resource or password reuse |
| 500 | Internal Server Error | Server-side error |

### Common Error Responses

**401 Unauthorized**:
```json
{
  "success": false,
  "message": "Invalid or expired token",
  "timestamp": "2026-07-17T11:20:00"
}
```

**400 Bad Request (Validation)**:
```json
{
  "success": false,
  "message": "Validation failed for one or more fields",
  "errors": {
    "email": ["Please provide a valid email address"],
    "password": ["Password must be at least 8 characters"]
  },
  "timestamp": "2026-07-17T11:20:00"
}
```

---

## Pagination

Paginated responses include metadata:

```json
{
  "content": [...],      // Array of items for current page
  "page": 0,             // Current page number (0-indexed)
  "size": 10,            // Items per page
  "totalElements": 150,  // Total items across all pages
  "totalPages": 15,      // Total number of pages
  "first": true,         // Is this the first page?
  "last": false,         // Is this the last page?
  "hasNext": true,       // Is there a next page?
  "hasPrevious": false   // Is there a previous page?
}
```

---

## Authentication Flow

1. **Register**: `POST /api/auth/register`
2. **Login**: `POST /api/auth/login` → Get JWT token
3. **Use Token**: Include in `Authorization` header for all protected endpoints
4. **Token Format**: `Authorization: Bearer {token}`

---

## Security Notes

- All passwords are encrypted with **AES-256-GCM** before storage
- User passwords are hashed with **BCrypt** (cost factor 10)
- JWT tokens expire after configured time
- Password reuse prevention (last 5 passwords)
- Soft delete for data recovery
- Complete audit trail for compliance

---

## Rate Limiting

*(Not yet implemented - recommended for production)*

Suggested limits:
- Authentication: 5 requests per minute
- Password generation: 20 requests per minute
- Credential operations: 100 requests per minute

---

*API Documentation Version 2.0*  
*Last Updated: July 17, 2026*


---

## Credential Sharing APIs

### Overview

Credential sharing allows users to securely share credentials with other registered users. The system implements a permission-based access control model with two permission levels: READ and EDIT.

**Permissions:**
- **READ**: View credential only, cannot modify
- **EDIT**: View and modify credential, cannot delete or reshare

**Business Rules:**
- Only the owner can share credentials
- Cannot share with yourself
- Cannot share deleted credentials
- Cannot share the same credential twice to the same user
- Revoked shares immediately lose access

---

### Share Credential

**Endpoint:** `POST /api/share`

**Description:** Share a credential with another user with specific permission level.

**Authorization:** Required (Owner only)

**Request Headers:**
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "credentialId": 1,
  "sharedWithUserId": 2,
  "permission": "READ",
  "expiresAt": "2026-12-31T23:59:59"
}
```

**Request Fields:**
- `credentialId` (required): ID of the credential to share
- `sharedWithUserId` (required): ID of the user to share with
- `permission` (required): Permission level - "READ" or "EDIT"
- `expiresAt` (optional): Expiration date/time for the share

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Credential shared successfully",
  "data": {
    "shareId": 1,
    "credentialId": 1,
    "serviceName": "Gmail",
    "ownerId": 1,
    "ownerName": "Alice Owner",
    "ownerEmail": "alice@example.com",
    "sharedWithUserId": 2,
    "sharedWithUserName": "Bob Reader",
    "sharedWithUserEmail": "bob@example.com",
    "permission": "READ",
    "sharedAt": "2026-07-17T10:00:00",
    "expiresAt": "2026-12-31T23:59:59",
    "active": true,
    "revokedAt": null
  },
  "timestamp": "2026-07-17T10:00:00"
}
```

**Error Responses:**

400 Bad Request - Cannot share with yourself:
```json
{
  "success": false,
  "message": "Cannot share credential with yourself",
  "timestamp": "2026-07-17T10:00:00"
}
```

400 Bad Request - Already shared:
```json
{
  "success": false,
  "message": "This credential is already shared with this user",
  "timestamp": "2026-07-17T10:00:00"
}
```

400 Bad Request - Deleted credential:
```json
{
  "success": false,
  "message": "Cannot share a deleted credential",
  "timestamp": "2026-07-17T10:00:00"
}
```

404 Not Found - Credential or user not found:
```json
{
  "success": false,
  "message": "Credential not found or you don't have permission to share it",
  "timestamp": "2026-07-17T10:00:00"
}
```

---

### Get Shared Credentials

**Endpoint:** `GET /api/share/received`

**Description:** Get all credentials that have been shared with the authenticated user.

**Authorization:** Required

**Request Headers:**
```
Authorization: Bearer {jwt_token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Shared credentials retrieved successfully",
  "data": [
    {
      "shareId": 1,
      "credentialId": 1,
      "serviceName": "Gmail",
      "ownerId": 1,
      "ownerName": "Alice Owner",
      "ownerEmail": "alice@example.com",
      "sharedWithUserId": 2,
      "sharedWithUserName": "Bob Reader",
      "sharedWithUserEmail": "bob@example.com",
      "permission": "READ",
      "sharedAt": "2026-07-17T10:00:00",
      "expiresAt": null,
      "active": true,
      "revokedAt": null
    },
    {
      "shareId": 2,
      "credentialId": 5,
      "serviceName": "GitHub",
      "ownerId": 3,
      "ownerName": "Charlie Editor",
      "ownerEmail": "charlie@example.com",
      "sharedWithUserId": 2,
      "sharedWithUserName": "Bob Reader",
      "sharedWithUserEmail": "bob@example.com",
      "permission": "EDIT",
      "sharedAt": "2026-07-17T11:00:00",
      "expiresAt": "2026-12-31T23:59:59",
      "active": true,
      "revokedAt": null
    }
  ],
  "timestamp": "2026-07-17T12:00:00"
}
```

**Notes:**
- Returns only active, non-expired shares
- Excludes shares for deleted credentials
- Empty array if no shares found

---

### Update Share Permission

**Endpoint:** `PUT /api/share/{shareId}`

**Description:** Update the permission level of an existing share.

**Authorization:** Required (Owner only)

**Request Headers:**
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

**URL Parameters:**
- `shareId`: ID of the share to update

**Request Body:**
```json
{
  "permission": "EDIT"
}
```

**Request Fields:**
- `permission` (required): New permission level - "READ" or "EDIT"

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Share permission updated successfully",
  "data": {
    "shareId": 1,
    "credentialId": 1,
    "serviceName": "Gmail",
    "ownerId": 1,
    "ownerName": "Alice Owner",
    "ownerEmail": "alice@example.com",
    "sharedWithUserId": 2,
    "sharedWithUserName": "Bob Reader",
    "sharedWithUserEmail": "bob@example.com",
    "permission": "EDIT",
    "sharedAt": "2026-07-17T10:00:00",
    "expiresAt": null,
    "active": true,
    "revokedAt": null
  },
  "timestamp": "2026-07-17T12:30:00"
}
```

**Error Responses:**

403 Forbidden - Not owner:
```json
{
  "success": false,
  "message": "Share not found or you don't have permission to modify it",
  "timestamp": "2026-07-17T12:30:00"
}
```

404 Not Found - Share not found:
```json
{
  "success": false,
  "message": "Share not found or you don't have permission to modify it",
  "timestamp": "2026-07-17T12:30:00"
}
```

---

### Revoke Share

**Endpoint:** `DELETE /api/share/{shareId}`

**Description:** Revoke a share, immediately removing access for the shared user.

**Authorization:** Required (Owner only)

**Request Headers:**
```
Authorization: Bearer {jwt_token}
```

**URL Parameters:**
- `shareId`: ID of the share to revoke

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Share revoked successfully",
  "timestamp": "2026-07-17T13:00:00"
}
```

**Error Responses:**

403 Forbidden - Not owner:
```json
{
  "success": false,
  "message": "Share not found or you don't have permission to revoke it",
  "timestamp": "2026-07-17T13:00:00"
}
```

404 Not Found - Share not found:
```json
{
  "success": false,
  "message": "Share not found or you don't have permission to revoke it",
  "timestamp": "2026-07-17T13:00:00"
}
```

**Notes:**
- Revoked shares set `active = false` and `revokedAt = current timestamp`
- User immediately loses access to the credential
- Revoked shares cannot be reactivated (must create new share)

---

## Authorization Flow with Sharing

### Access Check Process

When a user attempts to access a credential:

1. **Check Ownership**
   - If user is the owner → Grant full access
   - If not owner → Continue to step 2

2. **Check Share**
   - If credential is shared with user → Check permission
   - If not shared → Deny access (403 Forbidden)

3. **Check Permission**
   - **READ**: Allow view operations only
   - **EDIT**: Allow view and update operations
   - **No permission**: Deny access (403 Forbidden)

### Permission Matrix

| Operation | Owner | READ | EDIT | No Access |
|-----------|-------|------|------|-----------|
| View Credential | ✅ | ✅ | ✅ | ❌ |
| Update Credential | ✅ | ❌ | ✅ | ❌ |
| Delete Credential | ✅ | ❌ | ❌ | ❌ |
| Share Credential | ✅ | ❌ | ❌ | ❌ |
| Revoke Share | ✅ | ❌ | ❌ | ❌ |
| Update Share Permission | ✅ | ❌ | ❌ | ❌ |

### Example Scenarios

**Scenario 1: Owner Accessing Own Credential**
```
User: Alice (userId=1)
Credential: Gmail (credentialId=1, ownerId=1)
Result: ✅ Full Access (Owner)
```

**Scenario 2: Shared User with READ Permission**
```
User: Bob (userId=2)
Credential: Gmail (credentialId=1, ownerId=1)
Share: Active (permission=READ)
Result: ✅ Can View, ❌ Cannot Update
```

**Scenario 3: Shared User with EDIT Permission**
```
User: Charlie (userId=3)
Credential: Gmail (credentialId=1, ownerId=1)
Share: Active (permission=EDIT)
Result: ✅ Can View, ✅ Can Update, ❌ Cannot Delete
```

**Scenario 4: User with No Access**
```
User: Dave (userId=4)
Credential: Gmail (credentialId=1, ownerId=1)
Share: None
Result: ❌ 403 Forbidden
```

---

## Updated Endpoint Summary

### Total Endpoints: 21

#### Authentication (2)
- POST /api/auth/register
- POST /api/auth/login

#### Credentials (10)
- POST /api/credentials/create
- GET /api/credentials/{id}
- GET /api/credentials/list
- GET /api/credentials/vault (paginated)
- GET /api/credentials/search
- PUT /api/credentials/update/{id}
- DELETE /api/credentials/delete/{id} (soft delete)
- GET /api/credentials/trash
- PUT /api/credentials/{id}/restore
- DELETE /api/credentials/{id}/permanent

#### Credential Sharing (4) ⭐ NEW
- POST /api/share
- GET /api/share/received
- PUT /api/share/{shareId}
- DELETE /api/share/{shareId}

#### Password Intelligence (3)
- POST /api/password/generate
- POST /api/password/strength
- POST /api/password/generate/pin

---

*API Documentation v3.0 - Now with Credential Sharing*
