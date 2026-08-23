# SecureVault - Deployment Quick Start Guide

## 🚀 Quick Deployment Guide

This guide gets SecureVault running in under 5 minutes using Docker.

---

## Prerequisites

- Docker Desktop installed ([Installation Guide](DOCKER_INSTALLATION_GUIDE.md))
- 8GB RAM available
- 5GB disk space

---

## Option 1: Docker Compose (Recommended)

### Step 1: Clone/Navigate to Project
```bash
cd "c:\Users\devad\Desktop\secure vault\SecureVault"
```

### Step 2: Configure Environment Variables
```bash
# Copy template
copy .env.docker .env

# Edit with your secure passwords
notepad .env
```

**Minimum Required Changes in `.env`:**
```properties
DB_PASSWORD=your_secure_postgres_password
REDIS_PASSWORD=your_secure_redis_password
JWT_SECRET_KEY=your_base64_jwt_secret
AES_ENCRYPTION_KEY=your_base64_aes_key
```

**Generate Secure Keys:**
```bash
# JWT Secret (Windows PowerShell)
[Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes("your-32-character-secret-key!!"))

# AES Key (Use any Base64 encoded 32-byte string)
[Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes("your-32-character-aes-key!!!"))
```

### Step 3: Start All Services
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

### Step 4: Verify Services
```bash
# Check status
docker compose ps

# View logs
docker compose logs securevault

# Test health
curl http://localhost:8080/actuator/health
```

### Step 5: Test the API
```bash
# Register a user
curl -X POST http://localhost:8080/api/users/register ^
  -H "Content-Type: application/json" ^
  -d "{\"fullName\":\"Test User\",\"email\":\"test@example.com\",\"password\":\"Test123!\"}"

# Login
curl -X POST http://localhost:8080/api/users/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@example.com\",\"password\":\"Test123!\"}"
```

---

## Option 2: Local Development (Without Docker)

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL running locally
- Redis running locally

### Step 1: Configure Database
```sql
-- Create database
CREATE DATABASE securevault;
```

### Step 2: Set Environment Variables
```bash
# Windows (Command Prompt)
set SPRING_DATASOURCE_PASSWORD=your_postgres_password
set JWT_SECRET_KEY=your_jwt_secret
set AES_ENCRYPTION_KEY=your_aes_key

# Windows (PowerShell)
$env:SPRING_DATASOURCE_PASSWORD="your_postgres_password"
$env:JWT_SECRET_KEY="your_jwt_secret"
$env:AES_ENCRYPTION_KEY="your_aes_key"
```

### Step 3: Build and Run
```bash
# Build
mvn clean package

# Run
java -jar target/securevault-0.0.1-SNAPSHOT.jar

# Or run directly
mvn spring-boot:run
```

---

## Useful Commands

### Docker Compose Commands
```bash
# Start services
docker compose up -d

# Stop services
docker compose down

# View logs
docker compose logs -f

# Restart a service
docker compose restart securevault

# Rebuild and restart
docker compose up --build -d

# Remove all data (fresh start)
docker compose down -v
```

### Docker Commands
```bash
# List running containers
docker ps

# View specific service logs
docker logs securevault-app

# Execute command in container
docker exec -it securevault-app sh

# Check resource usage
docker stats
```

---

## Access Points

Once running, access:

- **Application**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379

---

## Default Credentials

### PostgreSQL
- Host: `localhost` (or `postgres` in Docker network)
- Port: `5432`
- Database: `securevault`
- Username: `postgres`
- Password: Set in `.env` file

### Redis
- Host: `localhost` (or `redis` in Docker network)
- Port: `6379`
- Password: Set in `.env` file

---

## Troubleshooting

### Issue: Port Already in Use

**Solution:**
```bash
# Check what's using port 8080
netstat -ano | findstr :8080

# Change port in .env
SERVER_PORT=8081
```

### Issue: Cannot Connect to Database

**Solution:**
```bash
# Check if PostgreSQL container is running
docker compose ps postgres

# View PostgreSQL logs
docker compose logs postgres

# Restart PostgreSQL
docker compose restart postgres
```

### Issue: Redis Connection Failed

**Solution:**
```bash
# Check Redis status
docker compose ps redis

# Test Redis connection
docker exec -it securevault-redis redis-cli ping

# Should return: PONG
```

### Issue: Application Won't Start

**Solution:**
```bash
# View application logs
docker compose logs securevault

# Check environment variables
docker compose exec securevault env | findstr SPRING

# Rebuild image
docker compose build securevault
docker compose up -d
```

---

## Testing the Application

### 1. Register a User
```bash
curl -X POST http://localhost:8080/api/users/register ^
  -H "Content-Type: application/json" ^
  -d "{\"fullName\":\"John Doe\",\"email\":\"john@example.com\",\"password\":\"SecurePass123!\"}"
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/users/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"john@example.com\",\"password\":\"SecurePass123!\"}"
```

**Save the token from response:**
```bash
set TOKEN=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 3. Create a Credential
```bash
curl -X POST http://localhost:8080/api/credentials ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"serviceName\":\"Gmail\",\"username\":\"john@gmail.com\",\"password\":\"MyGmailPass123!\",\"category\":\"EMAIL\"}"
```

### 4. List Credentials
```bash
curl -X GET http://localhost:8080/api/credentials ^
  -H "Authorization: Bearer %TOKEN%"
```

---

## Production Deployment Checklist

Before deploying to production:

- [ ] Change all default passwords in `.env`
- [ ] Generate new JWT secret key
- [ ] Generate new AES encryption key
- [ ] Set `SPRING_PROFILES_ACTIVE=prod`
- [ ] Configure proper logging levels
- [ ] Set up SSL/TLS certificates
- [ ] Configure firewall rules
- [ ] Set up backup strategy
- [ ] Configure monitoring and alerts
- [ ] Review security settings

---

## Documentation

### Complete Guides
- [Installation Guide](DOCKER_INSTALLATION_GUIDE.md) - Docker installation
- [Testing Guide](TESTING_VERIFICATION_GUIDE.md) - Complete testing procedures
- [Configuration Guide](CONFIGURATION_PRECEDENCE_GUIDE.md) - Environment configuration
- [Implementation Summary](IMPLEMENTATION_SUMMARY.md) - All completed tasks
- [Maven Build Guide](MAVEN_BUILD_ANALYSIS.md) - Build process
- [Dependency Audit](DEPENDENCY_AUDIT.md) - All dependencies
- [IoC Analysis](IOC_CONTAINER_ANALYSIS.md) - Spring beans
- [Startup Analysis](SPRING_BOOT_STARTUP_ANALYSIS.md) - Boot process

---

## Support

For issues or questions:
1. Check the [Testing & Verification Guide](TESTING_VERIFICATION_GUIDE.md)
2. Review application logs: `docker compose logs securevault`
3. Check database logs: `docker compose logs postgres`
4. Check Redis logs: `docker compose logs redis`

---

## Next Steps

After successful deployment:

1. ✅ Test all API endpoints
2. ✅ Verify caching is working
3. ✅ Check database persistence
4. ✅ Monitor application logs
5. ✅ Set up regular backups
6. ✅ Configure monitoring tools
7. ✅ Plan for scaling

---

## Summary

**Deployment Time**: ~5 minutes  
**Services**: 3 containers (App, PostgreSQL, Redis)  
**Memory Usage**: ~2-3 GB  
**Status**: ✅ Production Ready

SecureVault is now running and ready to use! 🎉
