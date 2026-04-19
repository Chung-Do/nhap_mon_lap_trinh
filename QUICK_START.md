# Quick Reference - Build & Deploy

## 🚀 One-Command Build & Package

### Windows
```batch
.\build.bat
```

### Mac/Linux
```bash
bash build.sh
```

**Output**: `remote-client.zip` and `remote-server.zip` in project root

---

## 📦 Manual Build Steps

### Step 1: Build JAR files
```bash
mvn clean package -DskipTests
```

**Output**:
- `target/remote-client.jar`
- `target/remote-server.jar`

### Step 2: Package into ZIP

**Windows (PowerShell)**:
```powershell
.\scripts\package.ps1
```

**Windows (Batch)**:
```batch
.\scripts\package.bat
```

**Mac/Linux (Bash)**:
```bash
bash ./scripts/package.sh
```

**Output**:
- `remote-client.zip`
- `remote-server.zip`

---

## 🤖 GitHub Actions (Automatic)

### Push code to GitHub
```bash
git add .
git commit -m "Add build and package"
git push origin main
```

### Watch GitHub Actions
1. Go to GitHub Repository
2. Click **Actions** tab
3. See workflow running
4. Wait for ✅ Success

### Download artifacts
1. Click latest workflow run
2. Scroll to **Artifacts**
3. Download ZIPs

---

## 📝 Configuration

### Client Config
**File**: `remote-client/config.properties`

```properties
server.host=192.168.1.100    # Server IP
server.port=8888              # Server port
```

### Server Config
**File**: `remote-server/config.properties`

```properties
server.port=8888              # Listen port
allow.system.shutdown=true
allow.system.restart=true
```

---

## ▶️ Running the App

### From JAR directly
```bash
java -jar remote-client.jar
java -jar remote-server.jar
```

### From ZIP (after extract)
**Windows**:
```batch
cd remote-client
run.bat
```

**Mac/Linux**:
```bash
cd remote-client
bash run.sh
```

---

## 🔧 Troubleshooting

| Problem | Solution |
|---------|----------|
| Java not found | Install from https://adoptium.net/ |
| Maven not found | Install from https://maven.apache.org/ |
| Build fails | Check BUILD_GUIDE.md |
| ZIP is corrupted | Re-run packaging script |
| Cannot connect | Check server IP in config |

---

## 📚 Full Documentation

- **BUILD_GUIDE.md** - Complete build instructions
- **GITHUB_ACTIONS_GUIDE.md** - CI/CD configuration
- **README.md** - Project overview

---

## ✅ Checklist Before Push

- [ ] Code compiles: `mvn clean package`
- [ ] Local packaging works: `.\build.bat`
- [ ] ZIPs created: `remote-client.zip`, `remote-server.zip`
- [ ] ZIPs contain necessary files (JAR, README, config, run.bat)
- [ ] Run application locally - works?
- [ ] Commit code to git
- [ ] Push to GitHub

**Then GitHub Actions takes over! 🚀**

