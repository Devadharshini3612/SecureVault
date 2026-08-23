# Docker Installation and Verification Guide

## Overview

This guide provides step-by-step instructions for installing Docker Desktop on Windows, macOS, and Linux, along with verification steps to ensure proper installation.

---

## Why Docker?

Docker provides:
- ✅ **Consistent Environments**: Same behavior on dev, test, and production
- ✅ **Easy Deployment**: Package application with all dependencies
- ✅ **Isolation**: Each service runs in its own container
- ✅ **Scalability**: Easy to scale services independently
- ✅ **Portability**: Run anywhere Docker is installed

---

## Installation Instructions

### Windows Installation

#### Prerequisites
- Windows 10/11 (64-bit) Pro, Enterprise, or Education
- Or Windows 10/11 Home with WSL 2
- Virtualization must be enabled in BIOS
- At least 4GB RAM (8GB recommended)

#### Installation Steps

1. **Download Docker Desktop**
   - Visit: https://www.docker.com/products/docker-desktop
   - Click "Download for Windows"
   - Save the installer (`Docker Desktop Installer.exe`)

2. **Run the Installer**
   - Double-click the downloaded installer
   - Follow the installation wizard
   - Check "Use WSL 2 instead of Hyper-V" (recommended)
   - Click "OK" to proceed

3. **Enable WSL 2** (if not already enabled)
   ```powershell
   # Open PowerShell as Administrator and run:
   wsl --install
   ```

4. **Restart Computer**
   - Required to complete the installation
   - Docker Desktop will start automatically

5. **Accept License Agreement**
   - Docker Desktop will prompt for license acceptance
   - Read and accept the terms

6. **Complete Setup**
   - Docker Desktop may take a few minutes to start
   - Wait for the Docker icon in the system tray to show "Docker Desktop is running"

#### Troubleshooting Windows

**Issue: "WSL 2 installation is incomplete"**
```powershell
# Update WSL
wsl --update

# Set WSL 2 as default
wsl --set-default-version 2
```

**Issue: "Virtualization is not enabled"**
- Restart computer
- Enter BIOS/UEFI settings (usually F2, F10, or Del key during boot)
- Enable "Intel VT-x" or "AMD-V" virtualization
- Save and restart

**Issue: "Hyper-V is not enabled"**
```powershell
# Enable Hyper-V (Administrator PowerShell)
Enable-WindowsOptionalFeature -Online -FeatureName Microsoft-Hyper-V -All
```

---

### macOS Installation

#### Prerequisites
- macOS 11 (Big Sur) or later
- At least 4GB RAM (8GB recommended)
- 64-bit processor

#### Installation Steps

1. **Download Docker Desktop**
   - Visit: https://www.docker.com/products/docker-desktop
   - Click "Download for Mac"
   - Choose:
     - **Apple Silicon** (M1, M2, M3 chips)
     - **Intel Chip**

2. **Install Docker Desktop**
   - Open the downloaded `.dmg` file
   - Drag Docker icon to Applications folder
   - Open Docker from Applications

3. **Grant Permissions**
   - macOS will ask for permission to install helper components
   - Enter your password when prompted
   - Click "Install" or "OK"

4. **Start Docker Desktop**
   - Docker Desktop will appear in the menu bar
   - Wait for "Docker Desktop is running" status

5. **Optional: Configure Resources**
   - Open Docker Desktop
   - Go to Settings → Resources
   - Adjust CPU, Memory, Swap, and Disk as needed

#### Troubleshooting macOS

**Issue: "Docker Desktop failed to start"**
- Quit Docker Desktop completely
- Remove Docker.app from Applications
- Re-download and reinstall

**Issue: Performance issues**
- Increase allocated resources in Docker Settings
- Recommended: 4 CPUs, 8GB RAM

---

### Linux Installation (Ubuntu/Debian)

#### Prerequisites
- Ubuntu 20.04 LTS or later (or Debian 11+)
- 64-bit system
- Sudo privileges

#### Installation Steps

1. **Update Package Index**
   ```bash
   sudo apt update
   sudo apt upgrade -y
   ```

2. **Install Prerequisites**
   ```bash
   sudo apt install -y \
       apt-transport-https \
       ca-certificates \
       curl \
       gnupg \
       lsb-release
   ```

3. **Add Docker's Official GPG Key**
   ```bash
   curl -fsSL https://download.docker.com/linux/ubuntu/gpg | \
       sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
   ```

4. **Set Up Docker Repository**
   ```bash
   echo \
     "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] \
     https://download.docker.com/linux/ubuntu \
     $(lsb_release -cs) stable" | \
     sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
   ```

5. **Install Docker Engine**
   ```bash
   sudo apt update
   sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
   ```

6. **Start Docker Service**
   ```bash
   sudo systemctl start docker
   sudo systemctl enable docker
   ```

7. **Add Your User to Docker Group** (optional, to run without sudo)
   ```bash
   sudo usermod -aG docker $USER
   ```
   **Important**: Log out and log back in for this to take effect

#### Troubleshooting Linux

**Issue: "Permission denied" when running Docker**
```bash
# Add user to docker group and reload
sudo usermod -aG docker $USER
newgrp docker
```

**Issue: "Docker daemon is not running"**
```bash
# Start Docker service
sudo systemctl start docker

# Check status
sudo systemctl status docker
```

---

## Verification Steps

### Step 1: Check Docker Version

**Command:**
```bash
docker --version
```

**Expected Output:**
```
Docker version 24.0.7, build afdd53b
```

**What This Verifies:**
- ✅ Docker CLI is installed
- ✅ Docker is in your system PATH

---

### Step 2: Check Docker Info

**Command:**
```bash
docker info
```

**Expected Output (partial):**
```
Client:
 Version:    24.0.7
 Context:    default
 Debug Mode: false

Server:
 Containers: 0
  Running: 0
  Paused: 0
  Stopped: 0
 Images: 0
 Server Version: 24.0.7
 Storage Driver: overlay2
 ...
```

**What This Verifies:**
- ✅ Docker daemon is running
- ✅ Docker can communicate with the daemon
- ✅ System information is accessible

---

### Step 3: Run Hello World Container

**Command:**
```bash
docker run hello-world
```

**Expected Output:**
```
Unable to find image 'hello-world:latest' locally
latest: Pulling from library/hello-world
...
Status: Downloaded newer image for hello-world:latest

Hello from Docker!
This message shows that your installation appears to be working correctly.

To generate this message, Docker took the following steps:
 1. The Docker client contacted the Docker daemon.
 2. The Docker daemon pulled the "hello-world" image from the Docker Hub.
 3. The Docker daemon created a new container from that image...
 4. The Docker daemon streamed that output to the Docker client...
```

**What This Verifies:**
- ✅ Docker can pull images from Docker Hub
- ✅ Docker can create containers
- ✅ Docker can run containers
- ✅ Complete Docker workflow is functioning

---

### Step 4: Verify Docker Compose

**Command:**
```bash
docker compose version
```

**Expected Output:**
```
Docker Compose version v2.23.3
```

**What This Verifies:**
- ✅ Docker Compose is installed
- ✅ Can run multi-container applications

---

### Step 5: List Docker Images

**Command:**
```bash
docker images
```

**Expected Output:**
```
REPOSITORY    TAG       IMAGE ID       CREATED        SIZE
hello-world   latest    9c7a54a9a43c   5 months ago   13.3kB
```

**What This Verifies:**
- ✅ Docker can store and list images
- ✅ hello-world image was successfully downloaded

---

### Step 6: List Docker Containers

**Command:**
```bash
docker ps -a
```

**Expected Output:**
```
CONTAINER ID   IMAGE         COMMAND    CREATED         STATUS                     PORTS     NAMES
abc123def456   hello-world   "/hello"   2 minutes ago   Exited (0) 2 minutes ago             clever_name
```

**What This Verifies:**
- ✅ Docker can create and track containers
- ✅ Container ran successfully (Status: Exited (0))

---

### Step 7: Clean Up Test Container

**Command:**
```bash
# Remove stopped containers
docker rm $(docker ps -aq)

# Remove hello-world image
docker rmi hello-world
```

**Expected Output:**
```
abc123def456
Untagged: hello-world:latest
Deleted: sha256:9c7a54a9a43c...
```

---

## Docker Desktop GUI Verification (Windows/Mac)

### Check Docker Desktop Dashboard

1. **Open Docker Desktop**
   - Windows: Click Docker icon in system tray
   - Mac: Click Docker icon in menu bar

2. **Verify Dashboard Elements**
   - ✅ Containers tab shows no errors
   - ✅ Images tab is accessible
   - ✅ Volumes tab is accessible
   - ✅ Status shows "Running"

3. **Check Settings**
   - Open Settings/Preferences
   - Verify Resources allocation
   - Check Docker Engine configuration

---

## Common Docker Commands Reference

### Image Management
```bash
# List images
docker images

# Pull an image
docker pull <image-name>:<tag>

# Remove an image
docker rmi <image-name>

# Build an image
docker build -t <image-name> .
```

### Container Management
```bash
# Run a container
docker run <image-name>

# List running containers
docker ps

# List all containers (including stopped)
docker ps -a

# Stop a container
docker stop <container-id>

# Start a stopped container
docker start <container-id>

# Remove a container
docker rm <container-id>

# View container logs
docker logs <container-id>

# Execute command in running container
docker exec -it <container-id> bash
```

### Docker Compose
```bash
# Start services
docker compose up

# Start services in background
docker compose up -d

# Stop services
docker compose down

# View logs
docker compose logs

# Rebuild and start
docker compose up --build
```

### System Management
```bash
# View Docker disk usage
docker system df

# Remove unused data
docker system prune

# Remove all stopped containers, unused networks, dangling images
docker system prune -a
```

---

## Testing SecureVault with Docker

### Step 1: Navigate to Project Directory
```bash
cd "c:\Users\devad\Desktop\secure vault\SecureVault"
```

### Step 2: Create .env File
```bash
# Copy template
copy .env.docker .env

# Edit .env and configure your passwords
notepad .env
```

### Step 3: Build and Run
```bash
# Build and start all services
docker compose up --build -d

# View logs
docker compose logs -f
```

### Step 4: Verify Services Are Running
```bash
# Check running containers
docker compose ps

# Expected output:
# NAME                  STATUS    PORTS
# securevault-app       Up        0.0.0.0:8080->8080/tcp
# securevault-postgres  Up        0.0.0.0:5432->5432/tcp
# securevault-redis     Up        0.0.0.0:6379->6379/tcp
```

### Step 5: Test Application
```bash
# Test health endpoint
curl http://localhost:8080/actuator/health

# Or open in browser
start http://localhost:8080
```

### Step 6: Stop Services
```bash
# Stop all services
docker compose down

# Stop and remove volumes (clears all data)
docker compose down -v
```

---

## Performance Optimization

### Windows/Mac: Increase Resources

1. Open Docker Desktop
2. Go to Settings → Resources
3. Adjust:
   - **CPUs**: 4 (recommended for SecureVault)
   - **Memory**: 8 GB (minimum 4 GB)
   - **Swap**: 2 GB
   - **Disk**: 60 GB

### Linux: No Additional Configuration Needed
Docker on Linux runs natively with better performance.

---

## Troubleshooting Common Issues

### Issue: "Docker daemon is not running"

**Windows/Mac:**
```bash
# Restart Docker Desktop from the application
```

**Linux:**
```bash
sudo systemctl restart docker
```

---

### Issue: "Cannot connect to Docker daemon"

**Solution:**
```bash
# Check Docker service status
docker info

# Windows/Mac: Restart Docker Desktop
# Linux: 
sudo systemctl status docker
sudo systemctl start docker
```

---

### Issue: "Port already in use"

**Solution:**
```bash
# Find process using port 8080
# Windows:
netstat -ano | findstr :8080

# Mac/Linux:
lsof -i :8080

# Stop the process or change port in docker-compose.yml
```

---

### Issue: "No space left on device"

**Solution:**
```bash
# Clean up Docker system
docker system prune -a --volumes

# Remove specific images
docker images
docker rmi <image-id>
```

---

## Security Best Practices

1. ✅ **Keep Docker Updated**: Regularly update Docker Desktop
2. ✅ **Don't Run as Root**: Use non-root users in containers
3. ✅ **Scan Images**: Use `docker scan <image>` to check for vulnerabilities
4. ✅ **Use Official Images**: Pull from trusted sources
5. ✅ **Limit Resources**: Set CPU and memory limits
6. ✅ **Use Secrets**: Don't hardcode passwords in Dockerfiles
7. ✅ **Network Isolation**: Use custom networks for services

---

## Next Steps

After successful Docker installation and verification:

1. ✅ Read `README.md` for SecureVault setup
2. ✅ Configure environment variables in `.env`
3. ✅ Build SecureVault Docker images
4. ✅ Run the complete stack with Docker Compose
5. ✅ Test the application endpoints
6. ✅ Review logs and monitoring

---

## Additional Resources

- **Docker Documentation**: https://docs.docker.com/
- **Docker Hub**: https://hub.docker.com/
- **Docker Compose Docs**: https://docs.docker.com/compose/
- **Best Practices**: https://docs.docker.com/develop/dev-best-practices/
- **Security**: https://docs.docker.com/engine/security/

---

## Summary

### Installation Checklist

- ✅ Docker Desktop installed (Windows/Mac) or Docker Engine (Linux)
- ✅ Docker daemon is running
- ✅ `docker --version` shows version information
- ✅ `docker info` displays system information
- ✅ `docker run hello-world` works successfully
- ✅ `docker compose version` shows Docker Compose version
- ✅ Resources allocated appropriately (4 CPU, 8GB RAM)
- ✅ User added to docker group (Linux only)

### You're Ready When:

- All verification steps pass
- Docker Desktop shows "Running" status
- No error messages in Docker logs
- Test containers can be created and run
- SecureVault services start successfully with `docker compose up`
