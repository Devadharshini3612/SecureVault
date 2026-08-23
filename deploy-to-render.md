# Quick Deployment Steps for Render

## 🎯 What You Need

1. **GitHub Account** - Create at https://github.com
2. **Render Account** - Create at https://render.com (free tier)
3. **2 GitHub Repositories:**
   - `securevault-backend` (Spring Boot)
   - `securevault-frontend` (React)

---

## 📦 Step 1: Push Code to GitHub

### Backend Repository

```bash
cd "c:\Users\devad\Desktop\secure vault\SecureVault"

# Initialize git (if not already done)
git init
git add .
git commit -m "Initial commit with 2FA email verification"

# Create repository on GitHub, then:
git remote add origin https://github.com/YOUR_USERNAME/securevault-backend.git
git branch -M main
git push -u origin main
```

### Frontend Repository

```bash
cd "c:\Users\devad\Desktop\secure vault\securevault-frontend"

# Initialize git
git init
git add .
git commit -m "Initial commit: SecureVault React frontend"

# Create repository on GitHub, then:
git remote add origin https://github.com/YOUR_USERNAME/securevault-frontend.git
git branch -M main
git push -u origin main
```

---

## 🗄️ Step 2: Create Database & Redis on Render

### Create PostgreSQL

1. Go to https://dashboard.render.com
2. Click **"New +"** → **"PostgreSQL"**
3. Settings:
   - Name: `securevault-db`
   - Database: `securevault`
   - Region: `Oregon`
   - Plan: `Free`
4. Click **"Create Database"**
5. **Copy the Internal Database URL** (you'll need this!)

### Create Redis

1. Click **"New +"** → **"Redis"**
2. Settings:
   - Name: `securevault-redis`
   - Region: `Oregon`
   - Plan: `Free`
3. Click **"Create Redis"**
4. **Copy the Internal Redis URL**

---

## 🚀 Step 3: Deploy Backend

1. Click **"New +"** → **"Web Service"**
2. Connect GitHub and select `securevault-backend`
3. Settings:
   - **Name:** `securevault-backend`
   - **Region:** `Oregon`
   - **Branch:** `main`
   - **Runtime:** `Java`
   - **Build Command:** `mvn clean install -DskipTests`
   - **Start Command:** `java -jar target/securevault-0.0.1-SNAPSHOT.jar`
   - **Plan:** `Free`

4. **Environment Variables** (click "Advanced" → "Add Environment Variable"):

```
SPRING_DATASOURCE_URL = [Your PostgreSQL Internal URL]
SPRING_DATASOURCE_USERNAME = securevault
SPRING_DATASOURCE_PASSWORD = [Your DB Password]
SPRING_REDIS_HOST = [Your Redis Host]
SPRING_REDIS_PORT = 6379
SPRING_REDIS_PASSWORD = [Your Redis Password]
JWT_SECRET_KEY = YXNkZmFzZGZhc2RmYXNkZmFzZGZhc2RmYXNkZmFzZGZhc2RmYXNkZmFzZGZhc2RmYXNkZg==
AES_ENCRYPTION_KEY = YXNkZmFzZGZhc2RmYXNkZmFzZGZhc2Rm
MAIL_USERNAME = dharshinimurali63@gmail.com
MAIL_PASSWORD = pltfwwalplgcjckn
EMAIL_ENABLED = true
SERVER_PORT = 8080
SPRING_PROFILES_ACTIVE = prod
```

5. Click **"Create Web Service"**
6. Wait 5-10 minutes for deployment
7. You'll get a URL like: `https://securevault-backend.onrender.com`

### Test Backend

Visit: `https://securevault-backend.onrender.com/actuator/health`

Should see: `{"status":"UP"}`

---

## 🎨 Step 4: Deploy Frontend

### Update Environment File First

Before deploying, update your backend URL:

```bash
cd "c:\Users\devad\Desktop\secure vault\securevault-frontend"

# Edit .env.production file to:
VITE_API_BASE_URL=https://securevault-backend.onrender.com

# Commit and push
git add .env.production
git commit -m "Update backend URL for production"
git push origin main
```

### Deploy to Render

1. Click **"New +"** → **"Static Site"**
2. Connect GitHub and select `securevault-frontend`
3. Settings:
   - **Name:** `securevault-frontend`
   - **Branch:** `main`
   - **Build Command:** `npm install && npm run build`
   - **Publish Directory:** `dist`
   - **Plan:** `Free`

4. **Environment Variable:**
```
VITE_API_BASE_URL = https://securevault-backend.onrender.com
```

5. Click **"Create Static Site"**
6. Wait 3-5 minutes
7. You'll get a URL like: `https://securevault-frontend.onrender.com`

---

## ✅ Step 5: Test Everything

### 1. Open Frontend
Visit: `https://securevault-frontend.onrender.com`

### 2. Register Account
- Click "Sign Up"
- Create your account

### 3. Login
- Enter credentials
- You should reach the dashboard

### 4. Test Vault
- Go to Vault page
- Add a credential
- Click eye icon to view password
- **Check your email** (dharshinimurali63@gmail.com)
- Enter 2FA code
- Password should be revealed ✅

### 5. Test Edit with 2FA
- Click "Edit" button
- **Check your email** for 2FA code
- Enter code
- Edit form opens ✅

---

## 🎉 Done!

Your SecureVault is now live on:
- **Frontend:** `https://securevault-frontend.onrender.com`
- **Backend:** `https://securevault-backend.onrender.com`

### ⚠️ Important Notes

- **First load takes 30-60 seconds** (free tier cold start)
- Backend spins down after 15 minutes of inactivity
- PostgreSQL free tier expires after 90 days
- Emails are sent to: dharshinimurali63@gmail.com

### 🔄 To Update Your App

Just push to GitHub:

```bash
git add .
git commit -m "Your updates"
git push origin main
```

Render auto-deploys! 🚀

---

## 🆘 Troubleshooting

**Backend won't start?**
- Check logs in Render Dashboard
- Verify all environment variables are set

**Frontend shows network error?**
- Check browser console (F12)
- Verify `VITE_API_BASE_URL` is correct
- Check CORS in backend

**No emails arriving?**
- Check spam folder
- Verify `MAIL_PASSWORD` is correct
- Check backend logs for email errors

**Database connection failed?**
- Use **Internal Database URL** not external
- Check username and password match
