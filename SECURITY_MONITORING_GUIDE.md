# Security Monitoring & Audit Analytics Guide

## Overview

This guide documents the complete Security Monitoring Module and Audit Logging & Analytics system for SecureVault. The system provides comprehensive security event tracking, real-time monitoring, risk classification, analytics reporting, and an interactive dashboard.

## Table of Contents

1. [Features](#features)
2. [Architecture](#architecture)
3. [Risk Levels & Classification](#risk-levels--classification)
4. [Security Event Types](#security-event-types)
5. [API Endpoints](#api-endpoints)
6. [Dashboard Metrics](#dashboard-metrics)
7. [Reports](#reports)
8. [Testing](#testing)
9. [Configuration](#configuration)
10. [Example API Calls](#example-api-calls)

---

## Features

### Security Monitoring
- **Login Monitoring**: Track every authentication attempt with detailed context
- **Failed Login Tracking**: Automatic detection and counting of failed login attempts
- **New Device Detection**: Identify logins from previously unseen devices
- **Brute Force Detection**: Real-time detection of brute force attack patterns
- **Suspicious Activity Tracking**: Monitor and flag abnormal user behavior
- **Risk Classification**: Automatic risk level assignment (LOW, MEDIUM, HIGH, CRITICAL)
- **Security Alerts**: Real-time alert generation for security events

### Audit Logging
- **Comprehensive Event Logging**: Track all user actions (LOGIN, LOGOUT, CREATE, UPDATE, DELETE, SHARE)
- **Entity-Level Tracking**: Audit trail for specific credentials and users
- **Time-Based Queries**: Retrieve audit logs for specific time periods
- **Action-Based Filtering**: Filter logs by action type

### Analytics & Reporting
- **Password Health Report**: Analyze password strength across all credentials
- **Login Activity Report**: Detailed login patterns and security events
- **Security Summary**: Comprehensive security posture overview
- **Analytics Dashboard**: Real-time metrics and trends

---

## Architecture

### Core Components

#### Entities
- **SecurityEvent**: Tracks security-related events (login attempts, suspicious activities)
- **AuditLog**: Records all user actions and system events

#### Services
- **SecurityMonitoringService**: Core security event tracking and risk classification
- **SecurityAlertService**: Alert generation and notification dispatch
- **AuditAnalyticsService**: Report generation (password health, login activity, security summary)
- **AnalyticsDashboardService**: Dashboard metrics and trend analysis

#### Controllers
- **SecurityMonitoringController**: Security event retrieval endpoints
- **AuditController**: Audit log history and reports
- **AnalyticsDashboardController**: Dashboard metrics
- **SecurityMonitoringTestController**: Testing and simulation endpoints

#### Repositories
- **SecurityEventRepository**: Security event data access
- **AuditLogRepository**: Audit log data access

---

## Risk Levels & Classification

### Risk Level Enum

```java
public enum RiskLevel {
    LOW,      // Normal activity
    MEDIUM,   // 3+ failed logins in 30 minutes
    HIGH,     // 5+ failed logins in 30 minutes
    CRITICAL  // 10+ failed logins in 30 minutes
}
```

### Risk Classification Rules

| Risk Level | Trigger Condition | Time Window | Alert Generated |
|------------|------------------|-------------|-----------------|
| LOW | Successful login or 0-2 failed logins | 30 minutes | No |
| MEDIUM | 3-4 failed login attempts | 30 minutes | Yes |
| HIGH | 5-9 failed login attempts | 30 minutes | Yes |
| CRITICAL | 10+ failed login attempts | 30 minutes | Yes (Brute Force) |

### Brute Force Detection
- **Threshold**: 5 or more failed login attempts within 10 minutes
- **Action**: Automatic CRITICAL risk classification and alert generation
- **Alert Type**: BRUTE_FORCE_ATTEMPT

---

## Security Event Types

### Available Event Types

```java
public enum SecurityEventType {
    // Authentication Events
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGOUT,
    
    // Security Events
    REPEATED_LOGIN_FAILURES,
    BRUTE_FORCE_ATTEMPT,
    NEW_DEVICE_LOGIN,
    ACCOUNT_LOCKED,
    
    // Suspicious Activities
    SUSPICIOUS_PATTERN,
    UNUSUAL_LOCATION,
    UNUSUAL_TIME,
    RAPID_OPERATIONS,
    MULTIPLE_DEVICE_LOGINS,
    
    // Access Events
    UNAUTHORIZED_ACCESS_ATTEMPT,
    PERMISSION_ESCALATION_ATTEMPT,
    
    // Data Events
    BULK_DATA_ACCESS,
    SENSITIVE_DATA_EXPORT
}
```

---

## API Endpoints

### Security Monitoring Endpoints

#### 1. Get User Security Events
```http
GET /api/security/events/{userId}
```
Retrieve all security events for a specific user.

**Parameters:**
- `userId` (path): User ID

**Response:**
```json
{
  "success": true,
  "message": "Security events retrieved successfully",
  "data": [
    {
      "eventId": 1,
      "userId": 10,
      "email": "user@example.com",
      "eventType": "LOGIN_SUCCESS",
      "riskLevel": "LOW",
      "ipAddress": "192.168.1.1",
      "userAgent": "Mozilla/5.0...",
      "deviceFingerprint": "abc123...",
      "location": "New York, US",
      "success": true,
      "details": "Successful login",
      "isNewDevice": false,
      "alertGenerated": false,
      "timestamp": "2026-08-08T10:30:00"
    }
  ]
}
```

#### 2. Get Failed Login Attempts
```http
GET /api/security/failed-logins?email={email}&days={days}
```
Get failed login attempts for a user.

**Parameters:**
- `email` (query): User email
- `days` (query, optional): Number of days to look back (default: 7)

**Response:**
```json
{
  "success": true,
  "message": "Failed login attempts retrieved",
  "data": [...]
}
```

#### 3. Get High-Risk Events
```http
GET /api/security/high-risk-events?days={days}
```
Retrieve all high-risk security events.

**Parameters:**
- `days` (query, optional): Number of days to look back (default: 7)

**Response:**
```json
{
  "success": true,
  "message": "High-risk events retrieved",
  "data": [...]
}
```

#### 4. Get Security Alerts
```http
GET /api/security/alerts?days={days}
```
Get all security alerts generated.

**Parameters:**
- `days` (query, optional): Number of days to look back (default: 7)

**Response:**
```json
{
  "success": true,
  "message": "Security alerts retrieved",
  "data": [...]
}
```

#### 5. Get New Device Logins
```http
GET /api/security/new-device-logins/{userId}?days={days}
```
Get new device login events for a user.

**Parameters:**
- `userId` (path): User ID
- `days` (query, optional): Number of days (default: 30)

**Response:**
```json
{
  "success": true,
  "message": "New device logins retrieved",
  "data": [...]
}
```

### Audit Endpoints

#### 1. Get User Audit Logs
```http
GET /api/audit/logs/{userId}
```
Retrieve all audit logs for a specific user.

**Response:**
```json
{
  "success": true,
  "message": "Audit logs retrieved successfully",
  "data": [
    {
      "logId": 1,
      "performedBy": 10,
      "action": "CREATE",
      "entityType": "CREDENTIAL",
      "entityId": 15,
      "details": "Created credential for example.com",
      "timestamp": "2026-08-08T10:30:00"
    }
  ]
}
```

#### 2. Get Entity Audit Logs
```http
GET /api/audit/logs/entity/{entityType}/{entityId}
```
Get audit logs for a specific entity (e.g., a credential).

**Parameters:**
- `entityType` (path): Entity type (CREDENTIAL, USER, etc.)
- `entityId` (path): Entity ID

#### 3. Get Audit Logs by Action
```http
GET /api/audit/logs/action/{action}
```
Filter audit logs by action type.

**Parameters:**
- `action` (path): Action type (CREATE, UPDATE, DELETE, LOGIN, LOGOUT, SHARE)

#### 4. Get Recent Audit Logs
```http
GET /api/audit/logs/recent?limit={limit}
```
Get the most recent audit logs (admin view).

**Parameters:**
- `limit` (query, optional): Maximum number of logs (default: 50)

#### 5. Get Password Health Report
```http
GET /api/audit/reports/password-health/{userId}
```
Generate password health report for a user.

**Response:**
```json
{
  "success": true,
  "message": "Password health report generated",
  "data": {
    "userId": 10,
    "totalCredentials": 25,
    "weakPasswords": 3,
    "mediumStrengthPasswords": 10,
    "strongPasswords": 12,
    "averagePasswordScore": 75.5,
    "reusedPasswords": 2,
    "oldPasswords": 5,
    "passwordHealthScore": 82.0,
    "weakPasswordsList": [
      {
        "credentialId": 15,
        "title": "Old Account",
        "strengthScore": 35,
        "issues": ["Too short", "No special characters"]
      }
    ],
    "recommendations": [
      "Update 3 weak passwords to strong passwords",
      "Change 5 passwords older than 90 days"
    ],
    "generatedAt": "2026-08-08T10:30:00"
  }
}
```

#### 6. Get Login Activity Report
```http
GET /api/audit/reports/login-activity/{userId}?days={days}
```
Generate login activity report.

**Parameters:**
- `userId` (path): User ID
- `days` (query, optional): Number of days to analyze (default: 30)

**Response:**
```json
{
  "success": true,
  "message": "Login activity report generated",
  "data": {
    "userId": 10,
    "email": "user@example.com",
    "reportPeriodDays": 30,
    "totalLogins": 45,
    "successfulLogins": 42,
    "failedLogins": 3,
    "uniqueDevices": 2,
    "uniqueLocations": 3,
    "newDeviceLogins": 1,
    "suspiciousActivities": 0,
    "loginsByDay": [...],
    "loginsByHour": [...],
    "recentLogins": [...],
    "deviceBreakdown": [...],
    "locationBreakdown": [...],
    "generatedAt": "2026-08-08T10:30:00"
  }
}
```

#### 7. Get Security Summary
```http
GET /api/audit/reports/security-summary/{userId}?days={days}
```
Generate comprehensive security summary.

**Parameters:**
- `userId` (path): User ID
- `days` (query, optional): Number of days to analyze (default: 30)

**Response:**
```json
{
  "success": true,
  "message": "Security summary generated",
  "data": {
    "userId": 10,
    "email": "user@example.com",
    "reportPeriodDays": 30,
    "overallSecurityScore": 85.5,
    "passwordHealthScore": 82.0,
    "loginSecurityScore": 90.0,
    "accountSecurityScore": 84.0,
    "totalCredentials": 25,
    "weakPasswords": 3,
    "sharedCredentials": 8,
    "totalLogins": 45,
    "failedLogins": 3,
    "securityAlerts": 1,
    "suspiciousActivities": 0,
    "newDeviceLogins": 1,
    "recommendations": [...],
    "recentSecurityEvents": [...],
    "passwordHealth": {...},
    "loginActivity": {...},
    "generatedAt": "2026-08-08T10:30:00"
  }
}
```

### Dashboard Endpoints

#### 1. Get User Dashboard Metrics
```http
GET /api/dashboard/metrics/{userId}
```
Get comprehensive dashboard metrics for a user.

**Response:**
```json
{
  "success": true,
  "message": "Dashboard metrics retrieved successfully",
  "data": {
    "userId": 10,
    "totalCredentials": 25,
    "sharedCredentials": 8,
    "weakPasswords": 3,
    "failedLoginCount": 2,
    "securityAlertCount": 1,
    "suspiciousActivityCount": 0,
    "recentActivity": [...],
    "passwordHealthScore": 82.0,
    "securityScore": 85.5,
    "trends": {
      "credentialsTrend": [...],
      "loginsTrend": [...]
    },
    "quickStats": {
      "todayLogins": 3,
      "todayCredentialsAdded": 1,
      "todayShares": 0,
      "todaySecurityEvents": 0
    }
  }
}
```

#### 2. Get System Dashboard Metrics
```http
GET /api/dashboard/metrics/system
```
Get system-wide dashboard metrics (admin view).

**Response:**
```json
{
  "success": true,
  "message": "System dashboard metrics retrieved successfully",
  "data": {
    "totalCredentials": 5000,
    "sharedCredentials": 1200,
    "weakPasswords": 350,
    "failedLoginCount": 45,
    "securityAlertCount": 12,
    "totalUsers": 250,
    "activeUsers": 180,
    "trends": {...},
    "quickStats": {...}
  }
}
```

### Testing Endpoints

**WARNING: These endpoints should be disabled in production!**

#### 1. Simulate Failed Login
```http
POST /api/security/test/simulate-failed-login?email={email}&count={count}
```
Simulate failed login attempts for testing.

#### 2. Simulate Brute Force Attack
```http
POST /api/security/test/simulate-brute-force?email={email}
```
Simulate a brute force attack (10 rapid failed logins).

#### 3. Simulate Suspicious Activity
```http
POST /api/security/test/simulate-suspicious-activity?userId={userId}&email={email}&activityType={type}
```
Simulate suspicious activity events.

#### 4. Simulate New Device Login
```http
POST /api/security/test/simulate-new-device?email={email}
```
Simulate a login from a new device.

#### 5. Get Test Information
```http
GET /api/security/test/info
```
Get information about test endpoints and thresholds.

---

## Dashboard Metrics

### User Dashboard

The user dashboard provides:

1. **Credential Overview**
   - Total credentials count
   - Shared credentials count
   - Weak passwords count

2. **Security Statistics**
   - Failed login count (last 30 days)
   - Security alert count
   - Suspicious activity count

3. **Health Scores**
   - Password health score (0-100)
   - Overall security score (0-100)

4. **Trends**
   - Credentials trend (last 7 days)
   - Logins trend (last 7 days)

5. **Quick Stats** (Today)
   - Logins today
   - Credentials added today
   - Shares created today
   - Security events today

6. **Recent Activity**
   - Last 10 user actions

### System Dashboard (Admin)

The system dashboard aggregates metrics across all users:

- Total system credentials
- Total shares
- Total weak passwords
- System-wide failed logins
- Security alerts across all users
- Total users and active users
- System-wide trends and quick stats

---

## Reports

### Password Health Report

**Purpose**: Analyze the security posture of all user passwords.

**Includes**:
- Total credentials count
- Weak/medium/strong password breakdown
- Average password strength score
- Reused passwords count
- Old passwords (>90 days) count
- Overall password health score
- List of weak passwords with issues
- Actionable recommendations

**Password Strength Scoring** (0-100):
- Length contribution: Up to 40 points (2 points per character, max 20 chars)
- Character variety: 30 points (uppercase, lowercase, numbers, special chars)
- Variety bonus: 30 points (multiple character types)

**Strength Categories**:
- Weak: Score < 50
- Medium: Score 50-79
- Strong: Score >= 80

### Login Activity Report

**Purpose**: Analyze user login patterns and detect anomalies.

**Includes**:
- Total/successful/failed login counts
- Unique devices and locations
- New device detections
- Suspicious activities count
- Logins by day of week
- Logins by hour of day
- Recent login history
- Device breakdown with counts
- Location breakdown with counts

**Use Cases**:
- Identify unusual login times
- Detect login patterns from new locations
- Monitor account sharing (multiple devices)
- Track failed login attempts

### Security Summary

**Purpose**: Comprehensive security posture overview combining password health and login security.

**Includes**:
- Overall security score (0-100)
- Password health score
- Login security score
- Account security score
- Complete credential statistics
- Login statistics
- Security alerts and suspicious activities
- Detailed recommendations
- Recent security events
- Embedded password health report
- Embedded login activity report

**Score Calculation**:
- Overall Security Score = (Password Health × 0.4) + (Login Security × 0.3) + (Account Security × 0.3)
- Login Security Score considers failed logins, alerts, and suspicious activities
- Account Security Score considers MFA status, password age, and security settings

---

## Testing

### Using Test Endpoints

1. **Test Failed Login Detection**
```bash
curl -X POST "http://localhost:8080/api/security/test/simulate-failed-login?email=test@example.com&count=3"
```

2. **Test Brute Force Detection**
```bash
curl -X POST "http://localhost:8080/api/security/test/simulate-brute-force?email=test@example.com"
```

3. **Test New Device Detection**
```bash
curl -X POST "http://localhost:8080/api/security/test/simulate-new-device?email=test@example.com" \
  -H "User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X)"
```

4. **Verify Security Events**
```bash
curl "http://localhost:8080/api/security/events/10"
```

### Manual Testing Scenarios

#### Scenario 1: Password Health Monitoring
1. Create credentials with varying password strengths
2. Call password health report endpoint
3. Verify weak passwords are identified
4. Check recommendations are appropriate

#### Scenario 2: Failed Login Tracking
1. Attempt login with wrong password
2. Check security events for failed login entry
3. Verify risk level increases with repeated failures
4. Confirm alert generation at thresholds

#### Scenario 3: New Device Detection
1. Login from a new browser/device
2. Check for NEW_DEVICE_LOGIN event
3. Verify alert is generated
4. Confirm device fingerprint is stored

#### Scenario 4: Dashboard Metrics
1. Perform various actions (create credentials, login, etc.)
2. Call dashboard metrics endpoint
3. Verify all counters are accurate
4. Check trends data is populated

---

## Configuration

### Risk Thresholds

Configure in `application.properties` or environment variables:

```properties
# Risk classification thresholds
security.risk.medium.threshold=3
security.risk.high.threshold=5
security.risk.critical.threshold=10

# Time windows (minutes)
security.risk.window=30
security.bruteforce.window=10
security.bruteforce.threshold=5

# Password age threshold (days)
security.password.age.threshold=90

# Alert settings
security.alerts.enabled=true
security.alerts.async=true
```

### Logging Configuration

All security services use SLF4J logging:

```properties
# Security monitoring logs
logging.level.com.securevault.service.SecurityMonitoringService=DEBUG
logging.level.com.securevault.service.SecurityAlertService=INFO
logging.level.com.securevault.service.AuditAnalyticsService=INFO
logging.level.com.securevault.service.AnalyticsDashboardService=INFO
```

### Database Indexes

For optimal performance, ensure these indexes exist:

```sql
-- SecurityEvent indexes
CREATE INDEX idx_security_event_user ON security_event(user_id);
CREATE INDEX idx_security_event_timestamp ON security_event(timestamp);
CREATE INDEX idx_security_event_risk ON security_event(risk_level);
CREATE INDEX idx_security_event_type ON security_event(event_type);

-- AuditLog indexes
CREATE INDEX idx_audit_log_user ON audit_log(performed_by);
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp);
CREATE INDEX idx_audit_log_action ON audit_log(action);
CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
```

---

## Example API Calls

### Complete Workflow Example

```bash
# 1. User registers and logs in
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"SecurePass123!"}'

curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"SecurePass123!"}'

# 2. Create some credentials
curl -X POST http://localhost:8080/api/credentials \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"title":"Gmail","username":"user@gmail.com","password":"StrongPass123!"}'

# 3. Check dashboard metrics
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/dashboard/metrics/10

# 4. Generate password health report
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/audit/reports/password-health/10

# 5. View login activity
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/audit/reports/login-activity/10?days=30

# 6. Get security summary
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/audit/reports/security-summary/10?days=30

# 7. View security events
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/security/events/10

# 8. Check audit logs
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/audit/logs/10
```

### Postman Collection

Import this JSON into Postman for quick testing:

```json
{
  "info": {
    "name": "SecureVault Security Monitoring",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Dashboard Metrics",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/api/dashboard/metrics/{{userId}}"
      }
    },
    {
      "name": "Password Health Report",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/api/audit/reports/password-health/{{userId}}"
      }
    },
    {
      "name": "Login Activity Report",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/api/audit/reports/login-activity/{{userId}}?days=30"
      }
    },
    {
      "name": "Security Summary",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/api/audit/reports/security-summary/{{userId}}?days=30"
      }
    },
    {
      "name": "Security Events",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/api/security/events/{{userId}}"
      }
    }
  ],
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080"
    },
    {
      "key": "userId",
      "value": "10"
    }
  ]
}
```

---

## Best Practices

### For Developers

1. **Always track security events** in authentication flows
2. **Use appropriate risk levels** based on actual threat assessment
3. **Generate alerts asynchronously** to avoid blocking user operations
4. **Index database tables** for performance with large datasets
5. **Log all security operations** for audit trail
6. **Sanitize user input** in all API endpoints
7. **Use transactions** for security event tracking to ensure atomicity

### For Administrators

1. **Monitor high-risk events** daily
2. **Review security alerts** promptly
3. **Analyze password health reports** weekly
4. **Track failed login patterns** to identify attack attempts
5. **Disable test endpoints** in production
6. **Configure appropriate thresholds** for your security policy
7. **Enable comprehensive logging** for security services

### For End Users

1. **Review login activity** regularly
2. **Update weak passwords** promptly
3. **Monitor new device alerts** for unauthorized access
4. **Use strong, unique passwords** for each credential
5. **Change passwords periodically** (every 90 days recommended)
6. **Report suspicious activities** immediately

---

## Troubleshooting

### Common Issues

#### 1. Security Events Not Being Tracked
- **Check**: SecurityMonitoringService is injected in UserController
- **Check**: Database connection is working
- **Check**: SecurityEvent table exists
- **Solution**: Verify @Transactional annotations are present

#### 2. Alerts Not Being Generated
- **Check**: SecurityAlertService is configured
- **Check**: AsyncNotificationService is available
- **Check**: Risk thresholds are configured correctly
- **Solution**: Check logs for alert generation errors

#### 3. Dashboard Metrics Show Zero
- **Check**: User has created credentials
- **Check**: Database queries are executing successfully
- **Check**: Repository methods are returning data
- **Solution**: Verify userId is correct and data exists

#### 4. Reports Not Generating
- **Check**: AuditAnalyticsService dependencies are injected
- **Check**: Required repositories are accessible
- **Check**: User has sufficient data for reporting period
- **Solution**: Check logs for calculation errors

---

## Security Considerations

### Data Protection
- Security events contain sensitive information (IP addresses, device fingerprints)
- Implement proper access controls to restrict who can view security data
- Consider data retention policies for security events and audit logs
- Encrypt sensitive fields in the database

### Performance
- Security event tracking adds overhead to login operations
- Use database indexes to optimize query performance
- Consider archiving old security events
- Use caching for frequently accessed metrics

### Compliance
- Security monitoring supports compliance requirements (SOC 2, ISO 27001)
- Audit logs provide evidence for security audits
- Reports can be exported for compliance documentation
- Ensure retention periods meet regulatory requirements

---

## Future Enhancements

### Planned Features
- [ ] GeoIP integration for accurate location tracking
- [ ] Machine learning-based anomaly detection
- [ ] Real-time security dashboard with WebSocket updates
- [ ] Email/SMS notifications for critical security alerts
- [ ] Export reports to PDF/CSV
- [ ] Integration with SIEM systems
- [ ] Two-factor authentication enforcement based on risk level
- [ ] Account lockout after repeated failed attempts
- [ ] Security event correlation and pattern analysis

---

## Support

For questions or issues:
- Check the logs: `logs/securevault.log`
- Review API documentation: `/swagger-ui.html`
- Contact: security@securevault.com

---

**Document Version**: 1.0  
**Last Updated**: August 8, 2026  
**Author**: SecureVault Development Team
