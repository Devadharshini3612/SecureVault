# 📧 Email Configuration Guide for SecureVault

This guide explains how to configure real email sending for 2FA verification codes in SecureVault.

---

## 🎯 Current Setup

**Default Mode:** Console Logging (Development)
- Emails are **NOT sent** to real email addresses
- Verification codes appear in **Docker logs** or **console output**
- Set `app.email.enabled=false` (default)

**Production Mode:** Real Email Sending (SMTP)
- Emails are **sent to actual email addresses**
- Users receive codes in their **inbox**
- Set `app.email.enabled=true`

---

## 📋 Option 1: Gmail SMTP (Recommended for Testing)

### Prerequisites
- Gmail account
- **App Password** (not your regular Gmail password)

### Step 1: Enable Gmail App Password

1. Go to your Google Account: https://myaccount.google.com/
2. Click **Security** (left sidebar)
3. Enable **2-Step Verification** (if not already enabled)
4. After enabling 2FA, find **App passwords**
5. Click **App passwords**
6. Select:
   - App: **Mail**
   - Device: **Other (Custom name)** → Type "SecureVault"
7. Click **Generate**
8. **Copy the 16-character password** (example: `abcd efgh ijkl mnop`)

### Step 2: Configure SecureVault

Edit `application.properties`:

```properties
# Enable real email sending
app.email.enabled=true

# Gmail SMTP Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=abcdefghijklmnop
```

**Replace:**
- `your-email@gmail.com` → Your actual Gmail address
- `abcdefghijklmnop` → Your 16-character App Password (remove spaces)

### Step 3: Restart Backend

```bash
docker restart securevault-app
```

### Step 4: Test

1. Enable 2FA in SecureVault Dashboard
2. Click eye icon on a credential
3. **Check your Gmail inbox** for the verification code! 📧

---

## 📋 Option 2: Other Email Providers

### Outlook/Hotmail

```properties
app.email.enabled=true
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=your-email@outlook.com
spring.mail.password=your-password
```

### Yahoo Mail

```properties
app.email.enabled=true
spring.mail.host=smtp.mail.yahoo.com
spring.mail.port=587
spring.mail.username=your-email@yahoo.com
spring.mail.password=your-app-password
```

### Custom SMTP Server

```properties
app.email.enabled=true
spring.mail.host=smtp.your-domain.com
spring.mail.port=587
spring.mail.username=noreply@your-domain.com
spring.mail.password=your-password
```

---

## 📋 Option 3: Using Environment Variables (Recommended for Production)

Instead of editing `application.properties`, use **environment variables**:

### Docker Compose

Edit `docker-compose.yml`:

```yaml
services:
  app:
    environment:
      - EMAIL_ENABLED=true
      - MAIL_HOST=smtp.gmail.com
      - MAIL_PORT=587
      - MAIL_USERNAME=your-email@gmail.com
      - MAIL_PASSWORD=abcdefghijklmnop
```

Then restart:
```bash
docker-compose down
docker-compose up -d
```

### Docker Run

```bash
docker run -d \
  -e EMAIL_ENABLED=true \
  -e MAIL_HOST=smtp.gmail.com \
  -e MAIL_PORT=587 \
  -e MAIL_USERNAME=your-email@gmail.com \
  -e MAIL_PASSWORD=abcdefghijklmnop \
  -p 8080:8080 \
  securevault-backend
```

### System Environment Variables (Windows)

```powershell
# Set permanently
[System.Environment]::SetEnvironmentVariable('EMAIL_ENABLED', 'true', 'User')
[System.Environment]::SetEnvironmentVariable('MAIL_USERNAME', 'your-email@gmail.com', 'User')
[System.Environment]::SetEnvironmentVariable('MAIL_PASSWORD', 'abcdefghijklmnop', 'User')

# Restart your IDE/terminal
```

### System Environment Variables (Linux/Mac)

Add to `~/.bashrc` or `~/.zshrc`:

```bash
export EMAIL_ENABLED=true
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=abcdefghijklmnop
```

Then:
```bash
source ~/.bashrc
```

---

## 🧪 Testing Email Configuration

### Test 1: Check Logs

After configuration, restart the backend and check logs:

```bash
docker logs -f securevault-app
```

Look for:
- `✅ Email sent successfully to: user@example.com`
- Or errors like: `❌ Failed to send email: ...`

### Test 2: Enable 2FA

1. Login to SecureVault
2. Go to **Dashboard**
3. Scroll to **"Two-Factor Authentication"**
4. Click **"Enable Email 2FA"**
5. Enter your **backup email**
6. Click **Save**

### Test 3: Request Code

1. Go to **Vault**
2. Click **eye icon** 👁️ on any credential
3. **Check your email inbox!**
4. Enter the 6-digit code

---

## ⚠️ Troubleshooting

### Issue: "Invalid credentials" or "Authentication failed"

**Solution:**
- For Gmail: Use **App Password**, not your regular password
- Verify email and password are correct
- Check if 2FA is enabled on your Gmail account

### Issue: "Connection timeout"

**Solution:**
- Check your firewall allows outbound connections on port 587
- Verify SMTP host and port are correct
- Try port 465 (SSL) instead of 587 (TLS):
  ```properties
  spring.mail.port=465
  spring.mail.properties.mail.smtp.ssl.enable=true
  ```

### Issue: "Email sent but not received"

**Solution:**
- Check **spam/junk folder**
- Verify recipient email is correct
- Check Gmail "Sent" folder
- Enable "Less secure app access" in Gmail (not recommended)

### Issue: Still seeing console logs instead of emails

**Solution:**
- Verify `app.email.enabled=true` is set
- Check if `JavaMailSender` bean is created:
  - Look for `JavaMailSender` in startup logs
- Restart backend after configuration changes

---

## 🔒 Security Best Practices

1. **Never commit passwords to Git:**
   ```bash
   # Add to .gitignore
   application-production.properties
   .env
   ```

2. **Use environment variables in production:**
   - Don't hardcode credentials in `application.properties`
   - Use Docker secrets or cloud provider secrets

3. **Use App Passwords:**
   - Never use your main email password
   - Generate app-specific passwords

4. **Rotate credentials regularly:**
   - Change app passwords every 3-6 months
   - Revoke unused app passwords

5. **Monitor email sending:**
   - Set up alerts for failed emails
   - Log email sending attempts

---

## 📊 Current Configuration Status

Check your current email configuration:

```bash
# View current settings (Docker)
docker exec securevault-app env | grep MAIL

# View current settings (Local)
# Check application.properties file
```

---

## 🎯 Quick Start (Gmail Example)

**For quick testing with Gmail:**

1. **Get Gmail App Password:**
   - https://myaccount.google.com/apppasswords
   - Generate password for "Mail" → "Other"

2. **Edit application.properties:**
   ```properties
   app.email.enabled=true
   spring.mail.username=your-email@gmail.com
   spring.mail.password=abcd efgh ijkl mnop
   ```

3. **Rebuild and restart:**
   ```bash
   mvn clean compile
   mvn spring-boot:repackage
   docker cp target/securevault-0.0.1-SNAPSHOT.jar securevault-app:/app/app.jar
   docker restart securevault-app
   ```

4. **Test:**
   - Enable 2FA
   - Request code
   - Check your Gmail inbox! 📧

---

## ✅ Verification Checklist

- [ ] Gmail/Email account set up
- [ ] App Password generated
- [ ] `application.properties` updated
- [ ] `app.email.enabled=true` set
- [ ] Backend restarted
- [ ] Test email sent successfully
- [ ] Email received in inbox
- [ ] 2FA code verified successfully

---

## 📞 Support

If you encounter issues:

1. Check logs: `docker logs securevault-app`
2. Verify configuration in `application.properties`
3. Test SMTP connection manually
4. Check email provider documentation

---

## 🎉 Success!

Once configured, your SecureVault will send real emails! 🚀

**Email Flow:**
1. User enables 2FA → Backup email saved
2. User clicks eye icon → Code generated
3. **Email sent to user's inbox** 📧
4. User enters code → Access granted ✅

---

<div align="center">

**Built with ❤️ for SecureVault**

[⬆ Back to Top](#-email-configuration-guide-for-securevault)

</div>
