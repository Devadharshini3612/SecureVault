# SecureVault Testing Guide

**Version**: 2.0  
**Last Updated**: July 17, 2026

---

## Table of Contents

1. [Setup Instructions](#setup-instructions)
2. [Postman Collection](#postman-collection)
3. [Test Scenarios](#test-scenarios)
4. [Expected Results](#expected-results)
5. [Error Testing](#error-testing)
6. [Performance Testing](#performance-testing)

---

## Setup Instructions

### Prerequisites

1. **Java 17+** installed
2. **MySQL 8.x** running on `localhost:3306`
3. **Postman** or any REST client
4. **Maven** for building the project

### Database Setup

```sql
CREATE DATABASE securevault;
USE securevault;
```

Spring Boot will automatically create tables on first run (JPA auto-DDL enabled).

### Application Setup

1. Clone the repository
2. Configure `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/securevault
   spring.datasource.username=root
   spring.datasource.password=your_password
   
   encryption.secret.key=0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF
   jwt.secret=your-256-bit-secret-key-here
   jwt.expiration=86400000
   ```

3. Build and run:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. Verify server is running:
   ```
   Server started on http://localhost:8080
   ```

---

## Postman Collection

### Import Collection

Create a new Postman Collection named **SecureVault API** with the following structure:

```
SecureVault API/
├── Authentication/
│   ├── Register User
│   └── Login User
├── Credentials/
│   ├── Create Credential
│   ├── Get Credential by ID
│   ├── List All Credentials
│   ├── Get Vault (Paginated)
│   ├── Search Credentials
│   ├── Update Credential
│   ├── Delete Credential (Soft)
│   ├── Get Trash
│   ├── Restore Credential
│   └── Permanent Delete
└── Password Intelligence/
    ├── Generate Password
    ├── Analyze Strength
    └── Generate PIN
```

### Environment Variables

Create a Postman Environment with these variables:

```json
{
  "base_url": "http://localhost:8080",
  "jwt_token": "",
  "user_id": "",
  "credential_id": ""
}
```

### Collection Variables Setup

After login, save the JWT token:

```javascript
// In Login request -> Tests tab
var jsonData = pm.response.json();
pm.environment.set("jwt_token", jsonData.data.token);
pm.environment.set("user_id", jsonData.data.userId);
```

---

## Test Scenarios

### Scenario 1: Complete User Journey

**Flow**: Register → Login → Create Credentials → Search → Update → Delete → Restore

#### Step 1: Register User

```http
POST {{base_url}}/api/auth/register
Content-Type: application/json

{
  "name": "Test User",
  "email": "testuser@example.com",
  "password": "SecurePass123!"
}
```

**Expected**: 201 Created, user details in response

#### Step 2: Login

```http
POST {{base_url}}/api/auth/login
Content-Type: application/json

{
  "email": "testuser@example.com",
  "password": "SecurePass123!"
}
```

**Expected**: 200 OK, JWT token in response  
**Action**: Save token for subsequent requests

#### Step 3: Create Credential #1 (Gmail)

```http
POST {{base_url}}/api/credentials/create
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "serviceName": "Gmail",
  "username": "testuser@gmail.com",
  "password": "GmailPass123!",
  "category": "PERSONAL"
}
```

**Expected**: 201 Created, credential with encrypted password

#### Step 4: Create Credential #2 (GitHub)

```http
POST {{base_url}}/api/credentials/create
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "serviceName": "GitHub",
  "username": "testuser",
  "password": "GitHubToken456!",
  "category": "DEVELOPMENT"
}
```

**Expected**: 201 Created

#### Step 5: Create Credential #3 (Bank)

```http
POST {{base_url}}/api/credentials/create
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "serviceName": "Chase Bank",
  "username": "testuser123",
  "password": "BankPass789!",
  "category": "BANKING"
}
```

**Expected**: 201 Created

#### Step 6: List All Credentials

```http
GET {{base_url}}/api/credentials/list
Authorization: Bearer {{jwt_token}}
```

**Expected**: 200 OK, array of 3 credentials

#### Step 7: Get Paginated Vault

```http
GET {{base_url}}/api/credentials/vault?page=0&size=10&sortBy=serviceName&direction=asc
Authorization: Bearer {{jwt_token}}
```

**Expected**: 200 OK, paginated response with metadata

#### Step 8: Filter by Category

```http
GET {{base_url}}/api/credentials/vault?category=BANKING
Authorization: Bearer {{jwt_token}}
```

**Expected**: 200 OK, only Chase Bank credential

#### Step 9: Search Credentials

```http
GET {{base_url}}/api/credentials/search?q=git
Authorization: Bearer {{jwt_token}}
```

**Expected**: 200 OK, GitHub credential found

#### Step 10: Update Credential

```http
PUT {{base_url}}/api/credentials/update/1
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "serviceName": "Gmail Personal",
  "password": "NewGmailPass456!"
}
```

**Expected**: 200 OK, updated credential

#### Step 11: Try Password Reuse (Should Fail)

```http
PUT {{base_url}}/api/credentials/update/1
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "password": "GmailPass123!"
}
```

**Expected**: 409 Conflict, password reuse error

#### Step 12: Delete Credential (Soft)

```http
DELETE {{base_url}}/api/credentials/delete/1
Authorization: Bearer {{jwt_token}}
```

**Expected**: 200 OK, credential soft deleted

#### Step 13: Verify Not in List

```http
GET {{base_url}}/api/credentials/list
Authorization: Bearer {{jwt_token}}
```

**Expected**: 200 OK, only 2 credentials (Gmail not included)

#### Step 14: View Trash

```http
GET {{base_url}}/api/credentials/trash
Authorization: Bearer {{jwt_token}}
```

**Expected**: 200 OK, Gmail credential in trash

#### Step 15: Restore Credential

```http
PUT {{base_url}}/api/credentials/1/restore
Authorization: Bearer {{jwt_token}}
```

**Expected**: 200 OK, credential restored

#### Step 16: Verify Restored

```http
GET {{base_url}}/api/credentials/list
Authorization: Bearer {{jwt_token}}
```

**Expected**: 200 OK, 3 credentials (Gmail back)

---

### Scenario 2: Password Intelligence

#### Test 1: Generate Strong Password

```http
POST {{base_url}}/api/password/generate
Content-Type: application/json

{
  "length": 16,
  "includeUppercase": true,
  "includeLowercase": true,
  "includeDigits": true,
  "includeSpecial": true
}
```

**Expected**: 200 OK, 16-char password with high strength score

#### Test 2: Generate Weak Password

```http
POST {{base_url}}/api/password/generate
Content-Type: application/json

{
  "length": 8,
  "includeUppercase": false,
  "includeLowercase": true,
  "includeDigits": false,
  "includeSpecial": false
}
```

**Expected**: 200 OK, 8-char lowercase-only password with lower score

#### Test 3: Analyze Strong Password

```http
POST {{base_url}}/api/password/strength
Content-Type: application/json

{
  "password": "Xk9@mP2#nQ5&vL8!w3T"
}
```

**Expected**: 200 OK, score 90+, "Very Strong" rating

#### Test 4: Analyze Weak Password

```http
POST {{base_url}}/api/password/strength
Content-Type: application/json

{
  "password": "password123"
}
```

**Expected**: 200 OK, low score, "Weak" rating with feedback


#### Test 5: Generate PIN

```http
POST {{base_url}}/api/password/generate/pin
Content-Type: application/json

{
  "length": 6
}
```

**Expected**: 200 OK, 6-digit numeric PIN

---

### Scenario 3: Pagination Testing

#### Test 1: First Page

```http
GET {{base_url}}/api/credentials/vault?page=0&size=2
Authorization: Bearer {{jwt_token}}
```

**Expected**: 200 OK, 2 items, `hasNext: true`, `hasPrevious: false`

#### Test 2: Second Page

```http
GET {{base_url}}/api/credentials/vault?page=1&size=2
Authorization: Bearer {{jwt_token}}
```

**Expected**: 200 OK, remaining items, `hasPrevious: true`

#### Test 3: Sort Ascending

```http
GET {{base_url}}/api/credentials/vault?sortBy=serviceName&direction=asc
Authorization: Bearer {{jwt_token}}
```

**Expected**: 200 OK, credentials sorted A-Z by service name

#### Test 4: Sort Descending

```http
GET {{base_url}}/api/credentials/vault?sortBy=createdAt&direction=desc
Authorization: Bearer {{jwt_token}}
```

**Expected**: 200 OK, newest credentials first

---

### Scenario 4: Password History

#### Setup: Create Credential

```http
POST {{base_url}}/api/credentials/create
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "serviceName": "History Test",
  "username": "historytest",
  "password": "Password1!",
  "category": "OTHER"
}
```

#### Test 1: Update with New Password (Should Succeed)

```http
PUT {{base_url}}/api/credentials/update/{id}
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "password": "Password2!"
}
```

**Expected**: 200 OK, password updated

#### Test 2: Update with Previous Password (Should Fail)

```http
PUT {{base_url}}/api/credentials/update/{id}
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "password": "Password1!"
}
```

**Expected**: 409 Conflict, "Password was used recently"

#### Test 3: Cycle Through 5 Passwords

Update password 5 times with different passwords:
- Password2! (already done)
- Password3!
- Password4!
- Password5!
- Password6!

#### Test 4: Try Original Password Again (Should Now Succeed)

```http
PUT {{base_url}}/api/credentials/update/{id}
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "password": "Password1!"
}
```

**Expected**: 200 OK (Password1! is now more than 5 changes ago)

---

## Expected Results

### Success Response Format

All successful responses follow this structure:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-07-17T10:30:00"
}
```

### Error Response Format

All error responses follow this structure:

```json
{
  "success": false,
  "message": "Error message",
  "timestamp": "2026-07-17T10:30:00"
}
```

### Validation Error Format

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "fieldName": ["Error message 1", "Error message 2"]
  },
  "timestamp": "2026-07-17T10:30:00"
}
```

---

## Error Testing

### Test 1: Register with Duplicate Email

**Request**:
```http
POST {{base_url}}/api/auth/register
Content-Type: application/json

{
  "name": "Test User 2",
  "email": "testuser@example.com",
  "password": "SecurePass123!"
}
```

**Expected**: 409 Conflict, "Email already registered"

### Test 2: Login with Wrong Password

**Request**:
```http
POST {{base_url}}/api/auth/login
Content-Type: application/json

{
  "email": "testuser@example.com",
  "password": "WrongPassword"
}
```

**Expected**: 401 Unauthorized, "Invalid credentials"

### Test 3: Access Protected Endpoint Without Token

**Request**:
```http
GET {{base_url}}/api/credentials/list
```

**Expected**: 401 Unauthorized, "Missing or invalid token"

### Test 4: Create Credential with Invalid Data

**Request**:
```http
POST {{base_url}}/api/credentials/create
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "serviceName": "",
  "username": "",
  "password": "abc"
}
```

**Expected**: 400 Bad Request, validation errors

### Test 5: Get Non-Existent Credential

**Request**:
```http
GET {{base_url}}/api/credentials/99999
Authorization: Bearer {{jwt_token}}
```

**Expected**: 404 Not Found, "Credential not found"

### Test 6: Update Another User's Credential

**Setup**: Create second user and get their credential ID

**Request**:
```http
PUT {{base_url}}/api/credentials/update/{other_user_credential_id}
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "password": "HackerPassword!"
}
```

**Expected**: 403 Forbidden or 404 Not Found (credential not visible to this user)

### Test 7: Password Too Short

**Request**:
```http
POST {{base_url}}/api/password/generate
Content-Type: application/json

{
  "length": 4,
  "includeUppercase": true,
  "includeLowercase": true,
  "includeDigits": true,
  "includeSpecial": true
}
```

**Expected**: 400 Bad Request, "Length must be between 8 and 128"

### Test 8: Invalid Category

**Request**:
```http
POST {{base_url}}/api/credentials/create
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "serviceName": "Test",
  "username": "test",
  "password": "TestPass123!",
  "category": "INVALID_CATEGORY"
}
```

**Expected**: 400 Bad Request, validation error

### Test 9: Page Size Too Large

**Request**:
```http
GET {{base_url}}/api/credentials/vault?page=0&size=200
Authorization: Bearer {{jwt_token}}
```

**Expected**: 400 Bad Request, "Page size cannot exceed 100"

### Test 10: Restore Already Active Credential

**Request**:
```http
PUT {{base_url}}/api/credentials/1/restore
Authorization: Bearer {{jwt_token}}
```

**Expected**: 400 Bad Request, "Credential is not deleted"

---

## Performance Testing

### Test 1: Bulk Credential Creation

Create 50-100 credentials and measure response times.

**Acceptance Criteria**:
- Average response time < 500ms
- All requests succeed
- No database connection errors

### Test 2: Pagination with Large Dataset

With 100+ credentials, test pagination:

```http
GET {{base_url}}/api/credentials/vault?page=0&size=100
```

**Acceptance Criteria**:
- Response time < 1000ms
- All records returned correctly
- Pagination metadata accurate

### Test 3: Concurrent Requests

Use Postman Runner or JMeter to send 10 concurrent requests:

```http
GET {{base_url}}/api/credentials/list
```

**Acceptance Criteria**:
- All requests succeed
- No race conditions
- Consistent response times

### Test 4: Password Generation Load

Generate 100 passwords in sequence:

```http
POST {{base_url}}/api/password/generate
```

**Acceptance Criteria**:
- Average response time < 100ms
- All passwords unique
- All passwords meet criteria

---

## Regression Testing Checklist

After any code changes, verify:

- [ ] User registration works
- [ ] User login returns valid JWT
- [ ] Token authentication works
- [ ] CRUD operations on credentials work
- [ ] Search and filtering work
- [ ] Pagination works correctly
- [ ] Soft delete and restore work
- [ ] Password history prevents reuse
- [ ] Password generation works
- [ ] Password strength analysis works
- [ ] PIN generation works
- [ ] Validation errors are returned correctly
- [ ] Unauthorized access is blocked
- [ ] Audit logs are created

---

## Manual Testing Tips

1. **Clear Database Between Tests**: Ensure clean state
   ```sql
   TRUNCATE TABLE password_history;
   TRUNCATE TABLE audit_logs;
   TRUNCATE TABLE credentials;
   TRUNCATE TABLE users;
   ```

2. **Check Logs**: Monitor application logs for errors
   ```bash
   tail -f logs/spring-boot-application.log
   ```

3. **Verify Database State**: Check records after operations
   ```sql
   SELECT * FROM credentials WHERE deleted = false;
   SELECT * FROM password_history WHERE credential_id = 1;
   ```

4. **Test Async Operations**: Check logs for async thread activity
   ```
   [SecureVault-Async-1] Activity Log | User: 1 | Action: CREDENTIAL_CREATED
   ```

5. **Token Expiry**: Test with expired tokens (wait for JWT expiration)

---

## Automated Testing (Future)

Consider implementing:

- **Unit Tests**: JUnit + Mockito for service layer
- **Integration Tests**: Spring Boot Test + TestContainers
- **API Tests**: RestAssured for endpoint testing
- **Load Tests**: JMeter or Gatling for performance
- **Security Tests**: OWASP ZAP for vulnerability scanning

---

## Test Data

### Sample Users

```json
{
  "name": "Alice Johnson",
  "email": "alice@example.com",
  "password": "AlicePass123!"
}

{
  "name": "Bob Smith",
  "email": "bob@example.com",
  "password": "BobPass456!"
}
```

### Sample Credentials

```json
// Banking
{
  "serviceName": "Chase Bank",
  "username": "alice123",
  "password": "BankPass789!",
  "category": "BANKING"
}

// Work
{
  "serviceName": "Company VPN",
  "username": "alice.johnson",
  "password": "VpnPass456!",
  "category": "WORK"
}

// Development
{
  "serviceName": "AWS Console",
  "username": "alice@company.com",
  "password": "AwsPass123!",
  "category": "DEVELOPMENT"
}
```

---

## Troubleshooting

### Issue: "Invalid or expired token"
**Solution**: Re-login and get a new JWT token

### Issue: "Credential not found"
**Solution**: Verify credential ID and user ownership

### Issue: "Database connection failed"
**Solution**: Check MySQL is running, verify credentials in application.properties

### Issue: "Validation failed"
**Solution**: Check request body against DTO validation rules

### Issue: "Password reuse detected"
**Solution**: Use a different password not in the last 5

---

*Testing Guide v2.0 - Comprehensive Test Coverage for SecureVault*
