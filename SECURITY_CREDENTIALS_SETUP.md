# Security Credentials Setup Guide

⚠️ **IMPORTANT: This document explains how to securely configure credentials for SecureVault**

---

## 🔐 Overview

SecureVault uses environment variables for all sensitive credentials. **Default values in `application.properties` are placeholders only** and must be replaced with real values via environment variables.

---

## 📋 Required Environment Variables

### For Local Development

Create a `.env` file in the project root (this file is gitignored):

```bash
# Database Credentials
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/securevault
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_local_db_password

# JWT Secret (generate new one for production)
JWT_SECRET_KEY=YXNkZmFzZGZhc2RmYXNkZmFzZGZhc2RmYXNkZmFzZGZhc2RmYXNkZmFzZGZhc2RmYXNkZg==

# AES Encryption Key (generate new one for production)
AES_ENCRYPTION_KEY=YXNkZmFzZGZhc2RmYXNkZmFzZGZhc2Rm

# Email Configuration (Gmail)
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_gmail_app_password
MAIL_FROM=your_email@gmail.com
EMAIL_ENABLED=true

# Redis (optional for local development)
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SPRING_CACHE_TYPE=redis
```

### For Production (Render.com)

Set these environment variables in the Render Dashboard:

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://dpg-xxx.singapore-postgres.render.com:5432/securevault_wfyp` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `securevault` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `[from Render dashboard]` |
| `JWT_SECRET_KEY` | JWT signing secret (Base64) | `[generate new one]` |
| `AES_ENCRYPTION_KEY` | AES encryption key (Base64) | `[generate new one]` |
| `MAIL_USERNAME` | Gmail address | `your_email@gmail.com` |
| `MAIL_PASSWORD` | Gmail App Password | `[16-character app password]` |
| `MAIL_FROM` | Email sender address | `your_email@gmail.com` |
| `EMAIL_ENABLED` | Enable email sending | `true` |
| `SERVER_PORT` | Server port | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active profile | `prod` |
| `SPRING_CACHE_TYPE` | Cache type | `none` (or `redis` if available) |

---

## 🔑 How to Generate Secure Keys

### Generate JWT Secret Key

```bash
# Linux/Mac
echo -n "your-super-secret-jwt-key-minimum-256-bits-for-security" | base64

# Windows PowerShell
[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("your-super-secret-jwt-key-minimum-256-bits-for-security"))
```

### Generate AES Encryption Key

```bash
# Linux/Mac (32 bytes = 256 bits)
echo -n "12345678901234567890123456789012" | base64

# Windows PowerShell
[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("12345678901234567890123456789012"))
```

⚠️ **IMPORTANT:** Use randomly generated keys for production! The examples above are for demonstration only.

---

## 📧 Gmail App Password Setup

To use Gmail for 2FA emails:

1. **Enable 2-Step Verification** on your Google account:
   - Go to: https://myaccount.google.com/security
   - Enable "2-Step Verification"

2. **Generate App Password**:
   - Go to: https://myaccount.google.com/apppasswords
   - Select "Mail" and "Other (Custom name)"
   - Name it "SecureVault"
   - Copy the 16-character password (format: `xxxx xxxx xxxx xxxx`)

3. **Use the App Password**:
   - Set `MAIL_PASSWORD` environment variable to this app password
   - Remove spaces (use: `xxxxxxxxxxxxxxxx`)

---

## 🔒 Security Best Practices

### ✅ DO:

- ✅ Use environment variables for ALL sensitive data
- ✅ Generate unique JWT/AES keys for each environment
- ✅ Use strong passwords (min 16 characters, random)
- ✅ Rotate credentials regularly (every 90 days)
- ✅ Use Gmail App Passwords (not your actual Gmail password)
- ✅ Keep `.env` files in `.gitignore`
- ✅ Use different credentials for development/production

### ❌ DON'T:

- ❌ Commit credentials to Git
- ❌ Use default/placeholder values in production
- ❌ Share credentials via email or chat
- ❌ Reuse passwords across services
- ❌ Use weak/predictable keys
- ❌ Hardcode credentials in source code

---

## 🚨 If Credentials Are Compromised

If you accidentally committed credentials to GitHub:

### 1. **Immediately Change All Credentials**

- Change database password
- Generate new JWT secret
- Generate new AES encryption key
- Revoke and create new Gmail App Password

### 2. **Remove from Git History**

```bash
# WARNING: This rewrites history - coordinate with team!
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch src/main/resources/application.properties" \
  --prune-empty --tag-name-filter cat -- --all

# Force push (⚠️ DANGEROUS)
git push origin --force --all
```

### 3. **Alternative: Use BFG Repo-Cleaner**

```bash
# Download BFG from: https://rtyley.github.io/bfg-repo-cleaner/
java -jar bfg.jar --delete-files application.properties
git reflog expire --expire=now --all && git gc --prune=now --aggressive
git push origin --force --all
```

### 4. **Rotate All Production Secrets**

- Update Render environment variables immediately
- Redeploy the application
- Monitor for suspicious activity

---

## 📄 Configuration Files Status

| File | Status | Should be in Git? |
|------|--------|-------------------|
| `.env` | Contains real credentials | ❌ NO (gitignored) |
| `.env.example` | Contains placeholders | ✅ YES |
| `.env.docker` | Contains Docker defaults | ✅ YES |
| `.env.production` | Contains public URLs only | ✅ YES |
| `application.properties` | Contains placeholders only | ✅ YES |
| `application-local.properties` | Contains real credentials | ❌ NO (gitignored) |

---

## 🧪 Testing Configuration

To verify your configuration is secure:

### 1. **Check Git Status**

```bash
git status
# Should NOT show .env files
```

### 2. **Search for Credentials in Git**

```bash
git log -S "your_actual_password" --all
# Should return nothing
```

### 3. **Check Remote Repository**

```bash
git log origin/main --oneline | head -20
# Review recent commits for sensitive data
```

### 4. **Test Environment Variables**

```bash
# Linux/Mac
echo $SPRING_DATASOURCE_PASSWORD

# Windows PowerShell
$env:SPRING_DATASOURCE_PASSWORD
```

---

## 📚 Additional Resources

- **Spring Boot Security**: https://spring.io/guides/topicals/spring-security-architecture/
- **OWASP Secrets Management**: https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html
- **Render Environment Variables**: https://render.com/docs/environment-variables
- **Gmail App Passwords**: https://support.google.com/accounts/answer/185833

---

## ✅ Pre-Submission Checklist

Before submitting to your mentor:

- [ ] No credentials in `application.properties` (only placeholders)
- [ ] `.env` files are gitignored
- [ ] `.env.example` exists with placeholder values
- [ ] README includes setup instructions
- [ ] Production credentials are set in Render dashboard
- [ ] Application runs successfully with environment variables
- [ ] No sensitive data in Git history
- [ ] All commits are clean and professional

---

**Last Updated:** August 23, 2026
**Security Status:** ✅ Repository is secure for submission
