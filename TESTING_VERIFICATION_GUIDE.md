# Comprehensive Testing and Verification Guide - SecureVault

## Overview

This guide provides complete instructions for testing and verifying all implemented features of SecureVault, including Redis caching, environment variable configuration, and Docker deployment.

---

## Table of Contents

1. [Environment Setup Verification](#1-environment-setup-verification)
2. [Redis Caching Verification](#2-redis-caching-verification)
3. [Cache Invalidation Testing](#3-cache-invalidation-testing)
4. [Environment Variable Configuration Testing](#4-environment-variable-configuration-testing)
5. [Application Functionality Testing](#5-application-functionality-testing)
6. [Docker Deployment Testing](#6-docker-deployment-testing)
7. [Performance Testing](#7-performance-testing)
8. [Security Testing](#8-security-testing)

---

## 1. Environment Setup Verification

### 1.1 Prerequisites Check

**Verify Java Installation:**
```bash
java -version
```
**Expected**: Java 17 or later

**Verify Maven Installation:**
```bash
mvn -version
```
**Expected**: Maven 3.6 or later

**Verify PostgreSQL:**
```bash
# Check if PostgreSQL is running
# Windows:
sc query postgresql-x64-16

# Mac/Linux:
pg_isready
```

**Verify Redis:**
```bash
# Check if Redis is running
redis-cli ping
```
**Expected**: PONG

---

### 1.2 Database Setup

**Create Database:**
```sql
-- Connect to PostgreSQL
psql -U postgres

-- Create database
CREATE DATABASE securevault;

-- Verify
\l securevault
```

**Expected**: Database created successfully

---

### 1.3 Environment Variables Setup

**Create .env file:**
```bash
# Copy the template
copy .env.example .env
```

**Configure Environment Variables:**
```properties
# Edit .env file with your values
JWT_SECRET_KEY=your_base64_encoded_secret
AES_ENCRYPTION_KEY=your_base64_encoded_key
SPRING_DATASOURCE_PASSWORD=your_postgres_password
```

---

## 2. Redis Caching Verification

### 2.1 Start the Application

```bash
# Method 1: Using Maven
mvn spring-boot:run

# Method 2: Using JAR
mvn clean package
java -jar target/securevault-0.0.1-SNAPSHOT.jar
```

**Expected Log Output:**
```
Tomcat started on port(s): 8080 (http)
Started SecureVaultApplication in X.XXX seconds
```

---

### 2.2 Test User Profile Caching

#### Step 1: Register a User

**Request:**
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Doe",
    "email": "john@example.com",
    "password": "SecurePass123!"
  }'
```

**Expected Response:**
```json
{
  "userId": 1,
  "fullName": "John Doe",
  "email": "john@example.com",
  "createdAt": "2024-08-07T10:00:00"
}
```

#### Step 2: Login to Get JWT Token

**Request:**
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123!"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "userId": 1,
    "fullName": "John Doe",
    "email": "john@example.com"
  }
}
```

**Save the token:**
```bash
set TOKEN=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Step 3: Verify Cache Miss (First Request)

**Check Application Logs:**
```
[DEBUG] Getting user profile for userId: 1 (cache miss)
[INFO] User profile loaded from database and cached: 1
Hibernate: select user0_.user_id ... from users user0_ where user0_.user_id=?
```

**Expected**: SQL query is executed (database hit)

#### Step 4: Verify Cache Hit (Second Request)

**Request same user again (within 2-hour TTL):**
```bash
# Make the same request multiple times
curl -X GET http://localhost:8080/api/credentials \
  -H "Authorization: Bearer %TOKEN%"
```

**Check Application Logs:**
```
[DEBUG] User found in cache
```

**Expected**: 
- ✅ No SQL query in logs
- ✅ Response time is faster
- ✅ Cache hit logged

---

### 2.3 Test Credential Caching

#### Step 1: Create a Credential

**Request:**
```bash
curl -X POST http://localhost:8080/api/credentials \
  -H "Authorization: Bearer %TOKEN%" \
  -H "Content-Type: application/json" \
  -d '{
    "serviceName": "Gmail",
    "username": "john@gmail.com",
    "password": "MyGmailPass123!",
    "category": "EMAIL"
  }'
```

**Expected Response:**
```json
{
  "credentialId": 1,
  "serviceName": "Gmail",
  "username": "john@gmail.com",
  "password": "MyGmailPass123!",
  "category": "EMAIL",
  "createdAt": "2024-08-07T10:05:00"
}
```

#### Step 2: List Credentials (Cache Miss)

**Request:**
```bash
curl -X GET http://localhost:8080/api/credentials \
  -H "Authorization: Bearer %TOKEN%"
```

**Check Logs:**
```
[DEBUG] Credential metadata cache miss for user: 1
Hibernate: select credential0_.credential_id ... from credentials credential0_
[INFO] Caching credential metadata for user: 1
```

**Expected**: Database query executed

#### Step 3: List Credentials Again (Cache Hit)

**Request:**
```bash
curl -X GET http://localhost:8080/api/credentials \
  -H "Authorization: Bearer %TOKEN%"
```

**Check Logs:**
```
[DEBUG] Credential metadata found in cache for user: 1 (count: 1)
```

**Expected**: No database query

---

### 2.4 Test Category Caching

**Request:**
```bash
curl -X GET http://localhost:8080/api/credentials/category/EMAIL \
  -H "Authorization: Bearer %TOKEN%"
```

**First Request Log:**
```
Hibernate: select credential0_.credential_id ... where credential0_.category=?
```

**Second Request Log:**
```
[DEBUG] Category data served from cache
```

**Expected**: Second request uses cache

---

### 2.5 Verify Cache in Redis

**Connect to Redis CLI:**
```bash
redis-cli
```

**Check Cached Keys:**
```redis
# List all keys
KEYS *

# Expected output (example):
1) "securevault:user:1"
2) "securevault:cred:1"
3) "securevault:category:1:EMAIL"
```

**View Cached Data:**
```redis
# Get user profile cache
GET "securevault:user:1"

# Check TTL (Time To Live)
TTL "securevault:user:1"
# Expected: ~7200 seconds (2 hours) or less
```

**Expected**: Keys exist with appropriate TTL values

---

## 3. Cache Invalidation Testing

### 3.1 Test Update Invalidation

#### Step 1: Update Credential

**Request:**
```bash
curl -X PUT http://localhost:8080/api/credentials/1 \
  -H "Authorization: Bearer %TOKEN%" \
  -H "Content-Type: application/json" \
  -d '{
    "serviceName": "Gmail Updated",
    "password": "NewPassword456!"
  }'
```

**Check Logs:**
```
[INFO] Credential updated and caches invalidated: 1 (user: 1)
[DEBUG] Evicting credential metadata from cache: 1
[DEBUG] Clearing all user caches: 1
```

**Expected**: Cache eviction logged

#### Step 2: Verify Cache Was Cleared

**Request:**
```bash
curl -X GET http://localhost:8080/api/credentials \
  -H "Authorization: Bearer %TOKEN%"
```

**Check Logs:**
```
[DEBUG] Credential metadata cache miss for user: 1
Hibernate: select credential0_.credential_id ...
```

**Expected**: 
- ✅ Cache miss (cache was cleared)
- ✅ Database query executed
- ✅ Fresh data returned

---

### 3.2 Test Delete Invalidation

#### Step 1: Delete Credential

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/credentials/1 \
  -H "Authorization: Bearer %TOKEN%"
```

**Check Logs:**
```
[INFO] Credential deleted and caches invalidated: 1 (user: 1)
[DEBUG] Evicting credential metadata from cache: 1
```

#### Step 2: Verify Cache Cleared

**In Redis CLI:**
```redis
# Check if credential cache exists
EXISTS "securevault:cred:1"
# Expected: (integer) 0 (cache cleared)
```

---

### 3.3 Test Manual Cache Management

**Clear All Caches:**
```bash
curl -X DELETE http://localhost:8080/api/cache/clear-all \
  -H "Authorization: Bearer %TOKEN%"
```

**Check Redis:**
```redis
KEYS *
# Expected: (empty array) or minimal keys
```

---

## 4. Environment Variable Configuration Testing

### 4.1 Test Default Values

**Start application without environment variables:**
```bash
mvn spring-boot:run
```

**Check Logs:**
```
spring.datasource.url=jdbc:postgresql://localhost:5432/securevault
spring.datasource.username=postgres
server.port=8080
```

**Expected**: Default values from application.properties are used

---

### 4.2 Test Environment Variable Override

**Set environment variables:**
```bash
# Windows (Command Prompt)
set SERVER_PORT=9090
set SPRING_DATASOURCE_USERNAME=securevault_user

# Windows (PowerShell)
$env:SERVER_PORT=9090
$env:SPRING_DATASOURCE_USERNAME="securevault_user"

# Mac/Linux
export SERVER_PORT=9090
export SPRING_DATASOURCE_USERNAME=securevault_user
```

**Start application:**
```bash
mvn spring-boot:run
```

**Check Logs:**
```
Tomcat started on port(s): 9090 (http)
spring.datasource.username=securevault_user
```

**Expected**: Environment variables override defaults

---

### 4.3 Test Command-Line Override

**Start with command-line arguments:**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=7070 --spring.datasource.username=cmduser"
```

**Check Logs:**
```
Tomcat started on port(s): 7070 (http)
spring.datasource.username=cmduser
```

**Expected**: Command-line args have highest precedence

---

### 4.4 Test Configuration Precedence

**Setup:**
1. Set in application.properties: `server.port=8080`
2. Set environment variable: `SERVER_PORT=9090`
3. Set command-line arg: `--server.port=7070`

**Result:**
```
Tomcat started on port(s): 7070 (http)
```

**Precedence Order (Highest to Lowest):**
1. ✅ Command-line arguments (7070) - USED
2. ❌ Environment variables (9090) - Ignored
3. ❌ application.properties (8080) - Ignored

---

## 5. Application Functionality Testing

### 5.1 User Registration & Login Flow

**Test Case 1: Successful Registration**
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test User","email":"test@example.com","password":"Test123!"}'
```
**Expected**: HTTP 200, user created

**Test Case 2: Duplicate Email**
```bash
# Register same email again
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test User","email":"test@example.com","password":"Test123!"}'
```
**Expected**: HTTP 409 Conflict

**Test Case 3: Invalid Email Format**
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test","email":"invalid-email","password":"Test123!"}'
```
**Expected**: HTTP 400 Bad Request

**Test Case 4: Successful Login**
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!"}'
```
**Expected**: HTTP 200, JWT token returned

**Test Case 5: Wrong Password**
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"WrongPass"}'
```
**Expected**: HTTP 401 Unauthorized

---

### 5.2 Credential CRUD Operations

**Create:**
```bash
curl -X POST http://localhost:8080/api/credentials \
  -H "Authorization: Bearer %TOKEN%" \
  -H "Content-Type: application/json" \
  -d '{"serviceName":"GitHub","username":"testuser","password":"ghp_token123","category":"DEVELOPER"}'
```
**Expected**: HTTP 201 Created

**Read (List):**
```bash
curl -X GET http://localhost:8080/api/credentials \
  -H "Authorization: Bearer %TOKEN%"
```
**Expected**: HTTP 200, array of credentials

**Read (Single):**
```bash
curl -X GET http://localhost:8080/api/credentials/1 \
  -H "Authorization: Bearer %TOKEN%"
```
**Expected**: HTTP 200, single credential with decrypted password

**Update:**
```bash
curl -X PUT http://localhost:8080/api/credentials/1 \
  -H "Authorization: Bearer %TOKEN%" \
  -H "Content-Type: application/json" \
  -d '{"serviceName":"GitHub Updated","password":"new_token456"}'
```
**Expected**: HTTP 200, updated credential

**Delete:**
```bash
curl -X DELETE http://localhost:8080/api/credentials/1 \
  -H "Authorization: Bearer %TOKEN%"
```
**Expected**: HTTP 200, credential deleted

---

### 5.3 Search and Filter Testing

**Search by Term:**
```bash
curl -X GET "http://localhost:8080/api/credentials/search?term=github" \
  -H "Authorization: Bearer %TOKEN%"
```
**Expected**: Matching credentials

**Filter by Category:**
```bash
curl -X GET http://localhost:8080/api/credentials/category/EMAIL \
  -H "Authorization: Bearer %TOKEN%"
```
**Expected**: Credentials in EMAIL category

---

## 6. Docker Deployment Testing

### 6.1 Build Docker Image

**Build the image:**
```bash
cd "c:\Users\devad\Desktop\secure vault\SecureVault"
docker build -t securevault:latest .
```

**Expected Output:**
```
[+] Building 120.5s (12/12) FINISHED
=> => naming to docker.io/library/securevault:latest
```

**Verify image:**
```bash
docker images | findstr securevault
```
**Expected**: securevault image listed

---

### 6.2 Test with Docker Compose

**Step 1: Configure Environment**
```bash
# Create .env file
copy .env.docker .env

# Edit with your values
notepad .env
```

**Step 2: Start Services**
```bash
docker compose up -d
```

**Expected Output:**
```
[+] Running 3/3
✔ Container securevault-postgres  Started
✔ Container securevault-redis     Started
✔ Container securevault-app       Started
```

**Step 3: Check Container Status**
```bash
docker compose ps
```

**Expected:**
```
NAME                  STATUS    PORTS
securevault-app       Up        0.0.0.0:8080->8080/tcp
securevault-postgres  Up        0.0.0.0:5432->5432/tcp
securevault-redis     Up        0.0.0.0:6379->6379/tcp
```

**Step 4: View Logs**
```bash
docker compose logs securevault
```

**Expected**: Application startup logs with no errors

**Step 5: Test Application**
```bash
curl http://localhost:8080/actuator/health
```

**Expected:**
```json
{"status":"UP"}
```

**Step 6: Test Full Workflow**
```bash
# Register user
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Docker Test","email":"docker@test.com","password":"Test123!"}'
```

**Expected**: HTTP 200, user created successfully

---

### 6.3 Test Inter-Container Communication

**Connect to app container:**
```bash
docker exec -it securevault-app sh
```

**Test PostgreSQL connection:**
```bash
nc -zv postgres 5432
```
**Expected**: Connection succeeded

**Test Redis connection:**
```bash
nc -zv redis 6379
```
**Expected**: Connection succeeded

---

### 6.4 Test Data Persistence

**Step 1: Create Data**
```bash
# Create a credential via API
curl -X POST http://localhost:8080/api/credentials ...
```

**Step 2: Stop Containers**
```bash
docker compose down
```

**Step 3: Restart Containers**
```bash
docker compose up -d
```

**Step 4: Verify Data Persists**
```bash
# List credentials
curl -X GET http://localhost:8080/api/credentials \
  -H "Authorization: Bearer %TOKEN%"
```

**Expected**: Previously created data still exists

---

## 7. Performance Testing

### 7.1 Cache Performance Comparison

**Without Cache (First Request):**
```bash
# Measure time
curl -w "@curl-format.txt" -o /dev/null -s http://localhost:8080/api/credentials \
  -H "Authorization: Bearer %TOKEN%"
```

**Create curl-format.txt:**
```
time_total: %{time_total}s
```

**Expected**: ~200-500ms (includes database query)

**With Cache (Subsequent Requests):**
```bash
# Same request
curl -w "@curl-format.txt" -o /dev/null -s http://localhost:8080/api/credentials \
  -H "Authorization: Bearer %TOKEN%"
```

**Expected**: ~50-100ms (Redis cache hit)

**Performance Improvement**: 2-5x faster with cache

---

### 7.2 Load Testing (Optional)

**Using Apache Bench:**
```bash
# Install Apache Bench (ab)
# Test 100 requests, 10 concurrent
ab -n 100 -c 10 -H "Authorization: Bearer %TOKEN%" http://localhost:8080/api/credentials
```

**Expected Metrics:**
- Requests per second: >100
- Mean response time: <100ms (with cache)
- Failed requests: 0

---

## 8. Security Testing

### 8.1 Test JWT Authentication

**Access Protected Endpoint Without Token:**
```bash
curl -X GET http://localhost:8080/api/credentials
```
**Expected**: HTTP 401 Unauthorized

**Access with Invalid Token:**
```bash
curl -X GET http://localhost:8080/api/credentials \
  -H "Authorization: Bearer invalid.token.here"
```
**Expected**: HTTP 401 Unauthorized

**Access with Valid Token:**
```bash
curl -X GET http://localhost:8080/api/credentials \
  -H "Authorization: Bearer %TOKEN%"
```
**Expected**: HTTP 200, data returned

---

### 8.2 Test Password Encryption

**Verify in Database:**
```sql
-- Connect to database
psql -U postgres -d securevault

-- View encrypted password
SELECT credential_id, service_name, encrypted_password 
FROM credentials 
WHERE credential_id = 1;
```

**Expected**: Encrypted password is Base64 string, NOT plain text

---

### 8.3 Test Input Validation

**Invalid Email:**
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"not-an-email"}'
```
**Expected**: HTTP 400, validation error

**Empty Password:**
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":""}'
```
**Expected**: HTTP 400, validation error

---

## Testing Checklist

### Environment Setup
- [ ] Java 17+ installed
- [ ] Maven installed
- [ ] PostgreSQL running
- [ ] Redis running
- [ ] Database created
- [ ] Environment variables configured

### Redis Caching
- [ ] User profile caching works
- [ ] Credential caching works
- [ ] Category caching works
- [ ] Cache hit reduces database queries
- [ ] Cache TTL configured correctly

### Cache Invalidation
- [ ] Update triggers cache eviction
- [ ] Delete triggers cache eviction
- [ ] Manual cache clear works
- [ ] Stale data not returned

### Environment Variables
- [ ] Default values work
- [ ] Environment variables override defaults
- [ ] Command-line args override env vars
- [ ] All secrets configurable via env vars

### Application Features
- [ ] User registration works
- [ ] User login returns JWT token
- [ ] Credential CRUD operations work
- [ ] Search and filter work
- [ ] Authentication required for protected endpoints

### Docker Deployment
- [ ] Docker image builds successfully
- [ ] Docker Compose starts all services
- [ ] Containers communicate properly
- [ ] Data persists across restarts
- [ ] Application accessible on port 8080

### Performance
- [ ] Cache improves response time
- [ ] No performance degradation under load
- [ ] Database queries optimized

### Security
- [ ] JWT authentication enforced
- [ ] Passwords encrypted in database
- [ ] Input validation working
- [ ] No secrets in logs or responses

---

## Summary

### All Tests Passing Means:

1. ✅ **Caching Works**: Redis caching reduces database load
2. ✅ **Cache Invalidation Works**: Stale data automatically cleared
3. ✅ **Environment Variables Work**: All secrets externalized
4. ✅ **Application Functions**: All CRUD operations work
5. ✅ **Docker Deployment Works**: Complete stack runs in containers
6. ✅ **Security**: Authentication, encryption, validation all working
7. ✅ **Performance**: Caching provides measurable improvement

### Production Readiness:

- ✅ Externalized configuration
- ✅ Distributed caching
- ✅ Container deployment
- ✅ Data persistence
- ✅ Security measures in place
- ✅ Performance optimized

**SecureVault is ready for deployment!**
