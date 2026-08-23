# SecureVault - Enterprise Password Manager

<div align="center">

![SecureVault Logo](https://img.shields.io/badge/SecureVault-v1.0-blue.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)
![React](https://img.shields.io/badge/React-18.2-61DAFB.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

**A modern, secure, and feature-rich password management solution for enterprises and individuals**

[Features](#features) • [Installation](#installation) • [Usage](#usage) • [API Documentation](#api-documentation) • [Security](#security)

</div>

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [System Architecture](#system-architecture)
- [Installation Guide](#installation-guide)
  - [Prerequisites](#prerequisites)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
  - [Docker Deployment](#docker-deployment)
- [Configuration](#configuration)
- [Usage Guide](#usage-guide)
- [API Documentation](#api-documentation)
- [Security Features](#security-features)
- [Advanced Features](#advanced-features)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

---

## 🌟 Overview

**SecureVault** is a comprehensive enterprise-grade password management system designed to securely store, manage, and share credentials across teams and individuals. Built with modern technologies and best security practices, SecureVault provides a robust solution for credential management needs.

### Key Highlights

✅ **Military-Grade Encryption** - AES-256 encryption for all stored credentials  
✅ **Zero-Knowledge Architecture** - Server never sees unencrypted passwords  
✅ **Two-Factor Authentication** - Email-based 2FA for credential access  
✅ **Secure Credential Sharing** - Granular permission controls (Read/Edit)  
✅ **Password Health Monitoring** - Real-time breach detection via HaveIBeenPwned API  
✅ **Advanced Search** - Fuzzy matching with keyboard shortcuts (Ctrl+K)  
✅ **Progressive Web App** - Installable on desktop and mobile  
✅ **Password Generator** - Customizable secure password generation  
✅ **Audit Logging** - Complete activity tracking and security monitoring  
✅ **Performance Optimized** - Redis caching and database optimization  

---

## 🚀 Features

### Core Features

#### 1. **Credential Management**
- Store unlimited credentials with encryption
- Organize by categories (Personal, Work, Finance, Social)
- Bulk import/export capabilities
- Version history and recovery
- Custom fields and notes

#### 2. **Advanced Search**
- Fuzzy text matching algorithm
- Search by name, username, URL, or category
- Keyboard shortcut (`Ctrl+K`) for quick access
- Real-time search results
- Search result highlighting

#### 3. **Password Health Monitor**
- Analyze password strength (Weak, Fair, Good, Strong, Excellent)
- Detect breached passwords using HaveIBeenPwned API
- Identify reused passwords across credentials
- Password age tracking
- Security score dashboard

#### 4. **Secure Credential Sharing**
- Share credentials with other users
- Granular permissions:
  - **READ**: View-only access
  - **EDIT**: Full modification rights
- Revoke access instantly
- Track who has access
- Audit trail for shared credentials

#### 5. **Two-Factor Authentication (2FA)**
- Email-based verification codes
- **Credential Access Protection**: 2FA required when viewing passwords
- Setup via Dashboard
- Backup codes for recovery
- 5-minute code expiration

#### 6. **Password Generator**
- Customizable length (8-128 characters)
- Character type selection:
  - Uppercase letters
  - Lowercase letters
  - Numbers
  - Special symbols
- Password strength indicator
- Copy-to-clipboard functionality

#### 7. **Progressive Web App (PWA)**
- Installable on desktop and mobile
- Offline access capability
- Background sync
- Push notifications
- App-like experience

### Additional Features

- **Dashboard Analytics**: Visual metrics and statistics
- **Audit Logs**: Complete activity tracking
- **Security Monitoring**: Real-time threat detection
- **Performance Monitoring**: Response time tracking
- **Cache Management**: Redis-powered caching
- **Mobile Responsive**: Optimized for all devices
- **Dark Mode Ready**: Theme support
- **Export Data**: JSON export for backups

---

## 🛠 Technology Stack

### Backend

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 17 | Core programming language |
| **Spring Boot** | 3.2.5 | Application framework |
| **Spring Security** | 3.2.5 | Authentication & authorization |
| **Spring Data JPA** | 3.2.5 | Database ORM |
| **PostgreSQL** | 14+ | Primary database |
| **Redis** | 7+ | Caching layer |
| **JWT** | - | Token-based authentication |
| **Maven** | 3.9+ | Build tool |
| **Docker** | 20+ | Containerization |
| **Lombok** | 1.18.30 | Code generation |

### Frontend

| Technology | Version | Purpose |
|------------|---------|---------|
| **React** | 18.2 | UI framework |
| **Redux Toolkit** | - | State management |
| **React Router** | 6+ | Client-side routing |
| **Axios** | - | HTTP client |
| **Tailwind CSS** | 3+ | Styling framework |
| **Vite** | - | Build tool |
| **PWA** | - | Progressive Web App |

### Security & Encryption

- **AES-256-GCM** - Symmetric encryption
- **PBKDF2** - Password hashing
- **BCrypt** - User password hashing
- **HTTPS/TLS** - Transport security
- **CORS** - Cross-origin protection

---

## 🏗 System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    CLIENT LAYER                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Browser    │  │    Mobile    │  │   PWA App    │     │
│  │  (React UI)  │  │  (React UI)  │  │  (Installed) │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   APPLICATION LAYER                          │
│  ┌───────────────────────────────────────────────────────┐ │
│  │          Spring Boot REST API (Port 8080)             │ │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌─────────┐ │ │
│  │  │   Auth   │ │   CRUD   │ │  Sharing │ │  Search │ │ │
│  │  │Controller│ │Controller│ │Controller│ │ Service │ │ │
│  │  └──────────┘ └──────────┘ └──────────┘ └─────────┘ │ │
│  └───────────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────────┐ │
│  │               Business Logic Layer                     │ │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌─────────┐ │ │
│  │  │   User   │ │Credential│ │  Sharing │ │   2FA   │ │ │
│  │  │ Service  │ │ Service  │ │ Service  │ │ Service │ │ │
│  │  └──────────┘ └──────────┘ └──────────┘ └─────────┘ │ │
│  └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    DATA LAYER                                │
│  ┌──────────────┐              ┌──────────────┐            │
│  │  PostgreSQL  │              │    Redis     │            │
│  │   Database   │              │    Cache     │            │
│  │  (Port 5432) │              │  (Port 6379) │            │
│  └──────────────┘              └──────────────┘            │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 Installation Guide

### Prerequisites

Before installation, ensure you have:

- **Java 17+** (OpenJDK recommended)
- **Node.js 16+** and **npm/yarn**
- **PostgreSQL 14+**
- **Redis 7+**
- **Docker & Docker Compose** (for containerized deployment)
- **Maven 3.9+**
- **Git**

---

### Backend Setup

#### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/securevault.git
cd securevault/SecureVault
```

#### 2. Configure Database

Create a PostgreSQL database:

```sql
CREATE DATABASE securevault;
CREATE USER securevault_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE securevault TO securevault_user;
```

#### 3. Configure Application

Edit `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/securevault
spring.datasource.username=securevault_user
spring.datasource.password=your_secure_password

# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379

# JWT Configuration
jwt.secret=your-256-bit-secret-key-here-change-in-production
jwt.expiration=86400000

# Server Configuration
server.port=8080
```

#### 4. Build the Application

```bash
mvn clean package -DskipTests
```

#### 5. Run the Backend

```bash
java -jar target/securevault-0.0.1-SNAPSHOT.jar
```

The backend will start on `http://localhost:8080`

---

### Frontend Setup

#### 1. Navigate to Frontend Directory

```bash
cd securevault-frontend
```

#### 2. Install Dependencies

```bash
npm install
# or
yarn install
```

#### 3. Configure Environment

Create `.env` file:

```env
VITE_API_URL=http://localhost:8080
VITE_APP_NAME=SecureVault
```

#### 4. Run Development Server

```bash
npm run dev
# or
yarn dev
```

The frontend will start on `http://localhost:3000`

#### 5. Build for Production

```bash
npm run build
# or
yarn build
```

---

### Docker Deployment

#### Quick Start with Docker Compose

1. **Ensure Docker is running**

2. **Build and start all services:**

```bash
docker-compose up -d
```

This will start:
- Backend (port 8080)
- PostgreSQL (port 5432)
- Redis (port 6379)

3. **Access the application:**
- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080`

#### Manual Docker Build

**Build Backend:**

```bash
docker build -t securevault-backend .
docker run -p 8080:8080 securevault-backend
```

**Build Frontend:**

```bash
cd securevault-frontend
docker build -t securevault-frontend .
docker run -p 3000:3000 securevault-frontend
```

---

## ⚙️ Configuration

### Environment Variables

#### Backend

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/securevault` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `securevault_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | - |
| `SPRING_DATA_REDIS_HOST` | Redis host | `localhost` |
| `SPRING_DATA_REDIS_PORT` | Redis port | `6379` |
| `JWT_SECRET` | JWT signing secret | - |
| `JWT_EXPIRATION` | Token expiration (ms) | `86400000` |

#### Frontend

| Variable | Description | Default |
|----------|-------------|---------|
| `VITE_API_URL` | Backend API URL | `http://localhost:8080` |
| `VITE_APP_NAME` | Application name | `SecureVault` |

---

## 📖 Usage Guide

### Getting Started

#### 1. **Register an Account**

Navigate to `http://localhost:3000/register` and create an account:
- Email address
- Strong password (min 8 characters)
- Confirm password

#### 2. **Login**

Use your credentials to login at `http://localhost:3000/login`

#### 3. **Add Your First Credential**

1. Click **"Add Credential"** button
2. Fill in the details:
   - Title (e.g., "Gmail Account")
   - Username/Email
   - Password
   - URL (optional)
   - Category
   - Notes (optional)
3. Click **"Save"**

#### 4. **Enable Two-Factor Authentication**

1. Go to **Dashboard**
2. Scroll to **"2FA Demo"** section
3. Click **"Enable Email 2FA"**
4. Enter your backup email
5. Save the backup codes

### Core Operations

#### Viewing Credentials

1. Navigate to **Vault** page
2. Browse your stored credentials
3. Click **eye icon** 👁️ to view password
   - If 2FA is enabled, enter verification code from email
4. Click **copy icon** to copy password

#### Sharing Credentials

1. Go to **Sharing** page
2. Click **"Share Credential"** button
3. Select credential from dropdown
4. Enter recipient's email
5. Choose permission level:
   - **READ**: View only
   - **EDIT**: Full access
6. Click **"Share"**

#### Monitoring Password Health

1. Navigate to **Security** page
2. View **Password Health Monitor**
3. See metrics:
   - Weak passwords
   - Breached passwords
   - Reused passwords
   - Password age
4. Click **"Fix"** to update weak passwords

#### Using Advanced Search

- Press **Ctrl+K** anywhere in the app
- Or click the search icon
- Type credential name, username, or URL
- Select from results

#### Generating Secure Passwords

1. Click **"Password Generator"** in navigation
2. Customize options:
   - Length (8-128 characters)
   - Include uppercase/lowercase
   - Include numbers/symbols
3. Click **"Generate"**
4. Copy the password

---

## 🔌 API Documentation

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "name": "John Doe"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "userId": 1,
    "email": "user@example.com",
    "name": "John Doe"
  }
}
```

### Credential Endpoints

#### Get All Credentials
```http
GET /api/credentials
Authorization: Bearer {token}
```

#### Create Credential
```http
POST /api/credentials
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Gmail Account",
  "username": "user@gmail.com",
  "password": "SecurePass123!",
  "url": "https://gmail.com",
  "category": "Personal",
  "notes": "My personal email"
}
```

#### Update Credential
```http
PUT /api/credentials/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Updated Title",
  "password": "NewSecurePass456!"
}
```

#### Delete Credential
```http
DELETE /api/credentials/{id}
Authorization: Bearer {token}
```

### Sharing Endpoints

#### Share Credential
```http
POST /api/share
Authorization: Bearer {token}
Content-Type: application/json

{
  "credentialId": 5,
  "recipientEmail": "recipient@example.com",
  "permission": "READ"
}
```

#### Get My Shared Credentials
```http
GET /api/share/my-shares
Authorization: Bearer {token}
```

#### Get Credentials Shared With Me
```http
GET /api/share/received
Authorization: Bearer {token}
```

#### Revoke Share
```http
DELETE /api/share/{shareId}
Authorization: Bearer {token}
```

### 2FA Endpoints

#### Enable 2FA (Email)
```http
POST /api/2fa/enable/email
Authorization: Bearer {token}
Content-Type: application/json

{
  "backupEmail": "backup@example.com"
}
```

#### Send Verification Code
```http
POST /api/2fa/send-code
Authorization: Bearer {token}
```

#### Verify Code
```http
POST /api/2fa/verify
Authorization: Bearer {token}
Content-Type: application/json

{
  "code": "123456"
}
```

#### Get 2FA Status
```http
GET /api/2fa/status
Authorization: Bearer {token}
```

### Search Endpoint

#### Search Credentials
```http
GET /api/credentials/search?query=gmail
Authorization: Bearer {token}
```

---

## 🔒 Security Features

### Encryption

- **AES-256-GCM** encryption for all stored passwords
- **PBKDF2** key derivation with 100,000 iterations
- **BCrypt** for user authentication passwords
- **Salt** generated per credential
- **IV** (Initialization Vector) for each encryption

### Authentication & Authorization

- **JWT** token-based authentication
- **24-hour** token expiration
- **Role-based** access control (RBAC)
- **CORS** protection
- **CSRF** protection

### Two-Factor Authentication

- **Email-based** verification
- **6-digit** codes with 5-minute expiration
- **Backup codes** for account recovery
- **Required for sensitive operations** (viewing passwords)

### Security Headers

```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000
Content-Security-Policy: default-src 'self'
```

### Audit Logging

- All credential operations logged
- User authentication events tracked
- Failed login attempts monitored
- Share/revoke operations audited

---

## 🎯 Advanced Features

### Password Health Analysis

The system analyzes passwords using multiple criteria:

1. **Strength Score** (0-100):
   - Length (up to 20 points)
   - Character variety (up to 40 points)
   - Complexity patterns (up to 40 points)

2. **Breach Detection**:
   - Integration with HaveIBeenPwned API
   - K-anonymity protocol (privacy-preserving)
   - Real-time breach checking

3. **Reuse Detection**:
   - Identifies duplicate passwords
   - Warns about security risks

4. **Age Tracking**:
   - Days since last password change
   - Recommendations for updates

### Caching Strategy

Redis caching improves performance:

- **Credential lists**: 5-minute TTL
- **User profiles**: 15-minute TTL
- **Search results**: 2-minute TTL
- **Dashboard metrics**: 1-minute TTL

### Performance Optimization

- **Database indexing** on frequently queried fields
- **Connection pooling** for database
- **Lazy loading** of credentials
- **Pagination** for large datasets
- **Async operations** for notifications

---

## 🧪 Testing

### Backend Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CredentialServiceTest

# Generate coverage report
mvn jacoco:report
```

### Frontend Tests

```bash
# Run unit tests
npm run test

# Run with coverage
npm run test:coverage

# Run E2E tests
npm run test:e2e
```

---

## 🐛 Troubleshooting

### Common Issues

#### Backend won't start

**Problem**: Port 8080 already in use

**Solution**:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <process_id> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

#### Database connection error

**Problem**: Cannot connect to PostgreSQL

**Solution**:
1. Check PostgreSQL is running: `pg_isready`
2. Verify credentials in `application.properties`
3. Check firewall settings
4. Ensure database exists: `psql -l`

#### Redis connection error

**Problem**: Cannot connect to Redis

**Solution**:
```bash
# Start Redis
redis-server

# Test connection
redis-cli ping
# Should return: PONG
```

#### Frontend build fails

**Problem**: Dependency conflicts

**Solution**:
```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install
```

#### 2FA code not received

**Problem**: Email codes not showing

**Solution**:
- Check Docker logs: `docker logs securevault-app`
- The code is logged in console for development
- In production, configure actual email service

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/AmazingFeature`
3. Commit your changes: `git commit -m 'Add some AmazingFeature'`
4. Push to the branch: `git push origin feature/AmazingFeature`
5. Open a Pull Request

---

## 📧 Support

For support, email support@securevault.com or open an issue on GitHub.

---

## 🙏 Acknowledgments

- [HaveIBeenPwned API](https://haveibeenpwned.com/) - Breach detection
- [Spring Framework](https://spring.io/) - Backend framework
- [React](https://reactjs.org/) - Frontend framework
- [Tailwind CSS](https://tailwindcss.com/) - Styling framework

---

## 📊 Project Stats

![GitHub Stars](https://img.shields.io/github/stars/yourusername/securevault?style=social)
![GitHub Forks](https://img.shields.io/github/forks/yourusername/securevault?style=social)
![GitHub Issues](https://img.shields.io/github/issues/yourusername/securevault)
![GitHub Pull Requests](https://img.shields.io/github/issues-pr/yourusername/securevault)

---

<div align="center">

**Built with ❤️ by the SecureVault Team**

[⬆ Back to Top](#securevault---enterprise-password-manager)

</div>
