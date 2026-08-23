# Spring Boot Configuration Precedence Guide

## Overview

Spring Boot loads configuration from multiple sources and applies them in a specific order of precedence. Understanding this order is crucial for managing application properties across different environments.

## Configuration Precedence Order (Highest to Lowest)

When the same property is defined in multiple sources, Spring Boot uses the value from the source with the **highest precedence**.

### Precedence Hierarchy

1. **Command-line Arguments** (Highest Priority)
2. **Java System Properties** (`System.getProperties()`)
3. **OS Environment Variables**
4. **Profile-specific Properties** (`application-{profile}.properties`)
5. **Application Properties** (`application.properties`)
6. **@PropertySource Annotations**
7. **Default Properties** (Lowest Priority)

---

## Demonstration: Configuration Precedence

### Test Property: `server.port`

We'll configure the same property in multiple sources and observe which value Spring Boot uses.

---

## Experiment 1: Default Configuration Only

### Setup
**File:** `src/main/resources/application.properties`
```properties
server.port=8080
```

### Run Command
```bash
mvn spring-boot:run
```

### Expected Result
```
Tomcat started on port(s): 8080 (http)
```

### Explanation
Only `application.properties` defines the port, so Spring Boot uses **8080**.

---

## Experiment 2: Environment Variable Override

### Setup
**File:** `application.properties`
```properties
server.port=8080
```

**Environment Variable:**
```bash
# Windows (Command Prompt)
set SERVER_PORT=9090
mvn spring-boot:run

# Windows (PowerShell)
$env:SERVER_PORT=9090
mvn spring-boot:run

# Linux/Mac
export SERVER_PORT=9090
mvn spring-boot:run
```

### Expected Result
```
Tomcat started on port(s): 9090 (http)
```

### Explanation
Environment variable `SERVER_PORT` **overrides** the value in `application.properties` because environment variables have **higher precedence**.

**Precedence Applied:**
- ✅ Environment Variable: `SERVER_PORT=9090` (Used)
- ❌ application.properties: `server.port=8080` (Ignored)

---

## Experiment 3: Command-line Argument Override

### Setup
**File:** `application.properties`
```properties
server.port=8080
```

**Environment Variable:**
```bash
export SERVER_PORT=9090  # Set but will be overridden
```

**Command-line Argument:**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=7070"

# OR when running the JAR directly:
java -jar target/securevault-0.0.1-SNAPSHOT.jar --server.port=7070
```

### Expected Result
```
Tomcat started on port(s): 7070 (http)
```

### Explanation
Command-line arguments have the **highest precedence**, overriding both environment variables and properties files.

**Precedence Applied:**
- ✅ Command-line Argument: `--server.port=7070` (Used)
- ❌ Environment Variable: `SERVER_PORT=9090` (Ignored)
- ❌ application.properties: `server.port=8080` (Ignored)

---

## Experiment 4: Profile-Specific Configuration

### Setup

**File:** `src/main/resources/application.properties`
```properties
server.port=8080
```

**File:** `src/main/resources/application-dev.properties`
```properties
server.port=8081
```

**File:** `src/main/resources/application-prod.properties`
```properties
server.port=8082
```

### Run with Active Profile

```bash
# Activate dev profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# OR
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

### Expected Result
```
The following 1 profile is active: "dev"
Tomcat started on port(s): 8081 (http)
```

### Explanation
Profile-specific properties **override** default `application.properties`.

**Precedence Applied:**
- ✅ application-dev.properties: `server.port=8081` (Used)
- ❌ application.properties: `server.port=8080` (Ignored)

---

## Experiment 5: Complete Precedence Demonstration

### Setup All Sources

1. **application.properties**
   ```properties
   server.port=8080
   custom.message=From application.properties
   ```

2. **application-dev.properties**
   ```properties
   server.port=8081
   custom.message=From application-dev.properties
   ```

3. **Environment Variable**
   ```bash
   export SERVER_PORT=9090
   export CUSTOM_MESSAGE="From Environment Variable"
   ```

4. **Command-line Argument**
   ```bash
   --server.port=7070 --custom.message="From Command Line"
   ```

### Run Command
```bash
export SERVER_PORT=9090
export CUSTOM_MESSAGE="From Environment Variable"
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev --server.port=7070 --custom.message='From Command Line'"
```

### Expected Result
```
The following 1 profile is active: "dev"
Tomcat started on port(s): 7070 (http)
custom.message = From Command Line
```

### Precedence Analysis

| Property | application.properties | application-dev.properties | Environment Variable | Command-line | **Winner** |
|----------|------------------------|----------------------------|----------------------|--------------|------------|
| server.port | 8080 | 8081 | 9090 | 7070 | **7070** (Command-line) |
| custom.message | From app.properties | From app-dev.properties | From Env Variable | From Command Line | **From Command Line** |

---

## SecureVault-Specific Configuration Examples

### Example 1: Database Configuration Precedence

```properties
# application.properties (Default - Development)
spring.datasource.url=jdbc:postgresql://localhost:5432/securevault
spring.datasource.username=postgres
spring.datasource.password=dev_password
```

**Production Override (Environment Variables):**
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db.example.com:5432/securevault
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=secure_prod_password
```

**Result:** Production database configuration **overrides** development defaults.

---

### Example 2: JWT Secret Key Precedence

```properties
# application.properties (Default - INSECURE FOR PRODUCTION!)
jwt.secret.key=default_insecure_key_for_dev_only
```

**Production Override (Environment Variable):**
```bash
export JWT_SECRET_KEY="YXNkZmFzZGZhc2RmYXNkZmFzZGZhc2RmYXNkZmFzZGZhc2RmYXNkZmFzZGZhc2RmYXNkZg=="
```

**Result:** Secure production key **overrides** development default.

---

### Example 3: Redis Configuration Precedence

```properties
# application.properties (Local Redis)
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=
```

**Docker Compose Override:**
```yaml
environment:
  SPRING_REDIS_HOST: redis
  SPRING_REDIS_PORT: 6379
  SPRING_REDIS_PASSWORD: redis_password
```

**Result:** Docker service name and password **override** localhost defaults.

---

## Property Naming Conventions

Spring Boot uses relaxed binding for property names:

| Source | Format | Example |
|--------|--------|---------|
| Properties File | Kebab-case | `server.port=8080` |
| Environment Variable | Uppercase + Underscores | `SERVER_PORT=8080` |
| System Property | Dot notation | `-Dserver.port=8080` |
| Command-line | Kebab-case with dashes | `--server.port=8080` |

### Conversion Rules

- Properties file: `spring.datasource.url`
- Environment variable: `SPRING_DATASOURCE_URL`
- Command-line: `--spring.datasource.url`

All three refer to the **same property** but use different naming conventions based on the source.

---

## Best Practices for SecureVault

### 1. Use Defaults for Development
```properties
# application.properties (safe defaults for local development)
spring.datasource.password=postgres
jwt.secret.key=dev_key_not_for_production
```

### 2. Override with Environment Variables in Production
```bash
# Production deployment
export SPRING_DATASOURCE_PASSWORD="$(cat /run/secrets/db-password)"
export JWT_SECRET_KEY="$(cat /run/secrets/jwt-key)"
```

### 3. Use Profiles for Different Environments
```properties
# application-dev.properties
spring.jpa.show-sql=true
logging.level.com.securevault=DEBUG

# application-prod.properties
spring.jpa.show-sql=false
logging.level.com.securevault=INFO
```

### 4. Use Command-line for Testing
```bash
# Quick test with different port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

---

## Verification Steps

### Step 1: Test Default Configuration
```bash
mvn clean spring-boot:run
# Check logs for: Tomcat started on port(s): 8080
```

### Step 2: Test Environment Variable Override
```bash
set SERVER_PORT=9090
mvn spring-boot:run
# Check logs for: Tomcat started on port(s): 9090
```

### Step 3: Test Command-line Override
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=7070"
# Check logs for: Tomcat started on port(s): 7070
```

### Step 4: Create a Test Endpoint
Add this to a controller to inspect property values:

```java
@RestController
public class ConfigTestController {
    
    @Value("${server.port}")
    private String serverPort;
    
    @Value("${spring.datasource.url}")
    private String datasourceUrl;
    
    @GetMapping("/config-test")
    public Map<String, String> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("server.port", serverPort);
        config.put("datasource.url", datasourceUrl);
        return config;
    }
}
```

Access `http://localhost:8080/config-test` to see which values were loaded.

---

## Summary

### Configuration Precedence (Highest → Lowest)
1. ✅ **Command-line arguments** (`--property=value`)
2. ✅ **Environment variables** (`PROPERTY=value`)
3. ✅ **Profile-specific properties** (`application-{profile}.properties`)
4. ✅ **Application properties** (`application.properties`)
5. ✅ **Default values** (in `@Value` annotations)

### Key Takeaways
- Command-line arguments **always win**
- Environment variables **override** properties files
- Profile-specific properties **override** default properties
- Use this hierarchy to manage configurations across environments
- Never hardcode secrets—use environment variables or secret managers

### SecureVault Recommendation
- **Development:** Use `application.properties` with safe defaults
- **Docker:** Use environment variables in `docker-compose.yml`
- **Production:** Use environment variables from secret managers (AWS Secrets Manager, Azure Key Vault, etc.)
- **Testing:** Use command-line arguments for quick overrides
