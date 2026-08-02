# SecureVault Logging Configuration Guide

This guide explains the comprehensive logging setup for the SecureVault application, including file rotation, performance optimization, and monitoring.

## 📁 Log Files Structure

```
logs/
├── securevault-application.log    # Main application log
├── securevault-security.log       # Security events and authentication
├── securevault-audit.log         # Audit trail (compliance/regulatory)
├── securevault-performance.log    # Performance metrics and monitoring
├── securevault-error.log         # Error-only log for quick issue identification
└── archive/                       # Compressed historical logs
    ├── securevault-application.2024-01-01.1.log.gz
    ├── securevault-security.2024-01-01.1.log.gz
    └── ...
```

## 🎯 Log Categories

### 1. Application Log (`securevault-application.log`)
- **Purpose**: General application flow and business logic
- **Rotation**: Daily + 50MB size limit
- **Retention**: 30 days, max 5GB total
- **Content**: Controller actions, service calls, database operations

### 2. Security Log (`securevault-security.log`)
- **Purpose**: Authentication, authorization, and security events
- **Rotation**: Daily + 10MB size limit
- **Retention**: 90 days, max 1GB total
- **Content**: Login attempts, failed authentications, suspicious activities

### 3. Audit Log (`securevault-audit.log`)
- **Purpose**: Regulatory compliance and data access tracking
- **Rotation**: Daily + 20MB size limit
- **Retention**: 365 days (1 year), max 2GB total
- **Content**: Credential CRUD operations, user registrations, data modifications

### 4. Performance Log (`securevault-performance.log`)
- **Purpose**: API response times and system performance
- **Rotation**: Daily + 30MB size limit
- **Retention**: 30 days, max 1GB total
- **Content**: Request durations, slow queries, resource usage

### 5. Error Log (`securevault-error.log`)
- **Purpose**: Critical errors and exceptions only
- **Rotation**: Daily + 10MB size limit
- **Retention**: 90 days, max 500MB total
- **Content**: ERROR and FATAL level messages only

## ⚙️ Configuration Files

### Primary Configuration: `logback-spring.xml`
- Complete Logback configuration with appenders and policies
- Profile-specific settings (dev/test/prod)
- Async logging for performance
- Custom patterns for different log types

### Secondary Configuration: `logging.properties`
- Fine-grained log level control
- Easy modification without XML editing
- Profile overrides for different environments

### Application Integration: `LoggingConfig.java`
- Automatic log directory creation
- Configuration validation
- System information logging
- Integration with `ProductionLoggingService`

## 🚀 Usage Examples

### Using ProductionLoggingService

```java
@Service
public class UserService {
    
    @Autowired
    private ProductionLoggingService loggingService;
    
    public UserResponse authenticateUser(LoginRequest request) {
        // Log authentication attempt
        loggingService.logUserAuthentication(
            request.getEmail(),
            true, // success
            getClientIp(),
            getUserAgent()
        );
        
        // Log credential operation
        loggingService.logCredentialOperation(
            user.getId(),
            "LOGIN",
            null,
            true,
            "User authenticated successfully"
        );
    }
}
```

### Direct SLF4J Logging

```java
@RestController
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody RegisterRequest request) {
        logger.info("User registration attempt for email: {}", request.getEmail());
        
        try {
            UserResponse response = userService.register(request);
            logger.info("User registered successfully with ID: {}", response.getUserId());
            return ResponseEntity.ok(ApiResponse.success("User registered", response));
        } catch (Exception e) {
            logger.error("Registration failed for email: {}: {}", request.getEmail(), e.getMessage(), e);
            throw e;
        }
    }
}
```

## 🔧 Environment Configuration

### Development Environment
```properties
# application-dev.properties
logging.level.com.securevault=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### Production Environment
```properties
# application-prod.properties
LOG_DIR=/var/log/securevault
logging.level.com.securevault=INFO
logging.level.root=INFO
logging.level.org.hibernate.SQL=WARN
```

### Docker Environment
```yaml
# docker-compose.yml
services:
  securevault:
    environment:
      - LOG_DIR=/app/logs
    volumes:
      - ./logs:/app/logs
```

## 📊 Log Monitoring

### Key Metrics to Monitor
1. **Error Rate**: Errors per minute from error.log
2. **Performance**: Average response time from performance.log
3. **Security Events**: Failed login attempts from security.log
4. **Disk Usage**: Log directory space consumption
5. **Authentication Success Rate**: From security.log

### Log Analysis Tools
- **ELK Stack**: Elasticsearch, Logstash, Kibana for log aggregation
- **Splunk**: Commercial log analysis platform
- **Graylog**: Open-source log management
- **Simple Grep**: For basic log searching

```bash
# Find all authentication failures
grep "AUTHENTICATION.*FAILED" logs/securevault-security.log

# Monitor error rate
tail -f logs/securevault-error.log

# Check performance issues
grep "SLOW" logs/securevault-performance.log
```

## 🛡️ Security Considerations

### Sensitive Data Protection
- **Email Masking**: `j***n@example.com` instead of `john@example.com`
- **Password Redaction**: All password fields logged as `***REDACTED***`
- **Token Sanitization**: JWT tokens and API keys are masked
- **IP Logging**: Client IPs logged for security but consider GDPR implications

### Audit Compliance
- **Immutable Logs**: Use append-only log storage in production
- **Integrity Checking**: Consider log signing for tamper detection
- **Retention Policies**: Audit logs kept for 1 year for compliance
- **Access Control**: Restrict log file access to authorized personnel only

## 🔄 Log Rotation Details

### Automatic Rotation Triggers
1. **Time-Based**: New file created daily at midnight
2. **Size-Based**: New file created when current exceeds size limit
3. **Compression**: Historical files automatically compressed (.gz)
4. **Cleanup**: Old files deleted based on retention policy

### Manual Log Management
```bash
# View current log sizes
du -h logs/*.log

# Manually compress old logs (if needed)
gzip logs/securevault-application.log.old

# Clean up old archived logs (beyond retention)
find logs/archive -name "*.log.gz" -mtime +30 -delete
```

## 🚨 Troubleshooting

### Common Issues

#### 1. Log Files Not Created
```bash
# Check directory permissions
ls -la logs/
# Ensure application can write to log directory
chmod 755 logs/
```

#### 2. Excessive Log Growth
```bash
# Check which logs are growing fastest
du -sh logs/* | sort -rh

# Temporarily reduce log level
# Edit application.properties:
logging.level.com.securevault=WARN
```

#### 3. Performance Impact
- Enable async logging (already configured)
- Increase buffer sizes in logback-spring.xml
- Consider log sampling for high-volume events

#### 4. Missing Correlation IDs
```java
// Ensure MDC is set in controllers
MDC.put("correlationId", UUID.randomUUID().toString());
try {
    // Process request
} finally {
    MDC.clear();
}
```

## 📈 Performance Optimization

### Async Logging Benefits
- Non-blocking log writes
- Improved response times
- Better throughput under load
- Configurable queue sizes and discard policies

### Buffer Configuration
```xml
<!-- High-performance settings -->
<appender name="ASYNC_APPLICATION" class="ch.qos.logback.classic.AsyncAppender">
    <queueSize>2048</queueSize>           <!-- Larger queue for burst traffic -->
    <discardingThreshold>0</discardingThreshold>  <!-- Don't discard any logs -->
    <neverBlock>true</neverBlock>         <!-- Don't block application threads -->
</appender>
```

## 🎯 Best Practices

### 1. Log Level Guidelines
- **TRACE**: Very detailed information, typically only when diagnosing problems
- **DEBUG**: Detailed information for debugging (SQL queries, method entry/exit)
- **INFO**: General information about application flow
- **WARN**: Potentially harmful situations that don't stop execution
- **ERROR**: Error events that might still allow application to continue

### 2. Structured Logging
```java
// Good: Structured with context
logger.info("User {} performed action {} on resource {}", userId, action, resourceId);

// Bad: String concatenation
logger.info("User " + userId + " performed action " + action);
```

### 3. Exception Logging
```java
// Good: Include exception for stack trace
logger.error("Failed to process request for user {}: {}", userId, e.getMessage(), e);

// Bad: No stack trace
logger.error("Failed to process request: " + e.getMessage());
```

### 4. Performance-Sensitive Areas
```java
// Use debug level for frequent operations
if (logger.isDebugEnabled()) {
    logger.debug("Processing item {}: {}", i, expensiveToString(item));
}
```

## 🔗 Integration Points

### Spring Boot Actuator
- Logging endpoints for runtime log level changes
- Health checks for log directory space
- Metrics for log rates and errors

### Monitoring Integration
- Prometheus metrics from log rates
- Alerting on error thresholds
- Dashboard visualization of log trends

This logging configuration provides a production-ready foundation for the SecureVault application with comprehensive monitoring, security, and compliance capabilities.