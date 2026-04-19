# GitHub Actions CI/CD Guide

## 📦 Automated Build & Package Pipeline

This project uses GitHub Actions to automatically build and package the application whenever you push code.

---

## 🚀 How It Works

### Automatic Triggers

The workflow defined in `.github/workflows/build-and-package.yml` runs automatically on:

- ✅ Push to `main` branch
- ✅ Push to `master` branch  
- ✅ Push to `develop` branch
- ✅ Pull Requests to these branches
- ✅ Manual trigger (GitHub Actions tab → Run workflow button)

### Build Pipeline

```
1. Checkout Code
   ↓
2. Setup Java 11 (temurin/adoptium)
   ↓
3. Build with Maven
   ├─ Compile source code
   ├─ Run tests (skipped: -DskipTests)
   ├─ Resolve dependencies
   └─ Generate JARs (remote-client.jar, remote-server.jar)
   ↓
4. Run Packaging Script (.ps1)
   ├─ Create dist/ directory structure
   ├─ Copy JAR files
   ├─ Generate README, config, run scripts
   ├─ Create remote-client.zip
   └─ Create remote-server.zip
   ↓
5. Upload Artifacts
   ├─ remote-client-package (ZIP)
   └─ remote-server-package (ZIP)
   ↓
6. (Optional) Create Release on Git Tag
```

---

## 📥 Download Build Artifacts

### For Each Push/PR

1. Go to **GitHub Repository** → **Actions** tab
2. Click on the latest **workflow run** (green checkmark or red X)
3. Scroll down to **Artifacts** section
4. Download:
   - `remote-client-package` (remote-client.zip)
   - `remote-server-package` (remote-server.zip)

### For Tagged Releases

1. When you push a Git tag: `git tag v1.0.0 && git push --tags`
2. A new **Release** is automatically created
3. Go to **Releases** tab to download ZIPs

---

## 🛠️ Local Build (Same as CI/CD)

You can replicate the exact same build locally:

### Windows

```batch
REM Option 1: One-command build + package
.\build.bat

REM Option 2: Build only
mvn clean package -DskipTests

REM Option 3: Package only (after build)
.\scripts\package.bat
```

### Mac/Linux

```bash
# Option 1: One-command build + package
bash build.sh

# Option 2: Build only
mvn clean package -DskipTests

# Option 3: Package only (after build)
bash ./scripts/package.sh
```

---

## 📋 Workflow File Explained

**Location:** `.github/workflows/build-and-package.yml`

### Key Sections

#### 1. Triggers
```yaml
on:
  push:
    branches: [ main, master, develop ]  # Auto-trigger on push
  pull_request:
    branches: [ main, master, develop ]  # Auto-trigger on PR
  workflow_dispatch:                     # Manual trigger allowed
```

#### 2. Runner
```yaml
runs-on: windows-latest  # Run on Windows VM (GitHub-hosted)
```

#### 3. Java Setup
```yaml
- uses: actions/setup-java@v3
  with:
    java-version: '11'                # JDK 11
    distribution: 'temurin'           # Temurin/Adoptium JDK
    cache: maven                      # Cache Maven dependencies
```

#### 4. Maven Build
```yaml
- run: mvn clean package -DskipTests -q
```

#### 5. Packaging
```yaml
- run: powershell -NoProfile -File .\scripts\package.ps1
```

#### 6. Upload Artifacts
```yaml
- uses: actions/upload-artifact@v3
  with:
    name: remote-client-package
    path: remote-client.zip
```

---

## 🔧 Customization

### Change Build Branches

Edit `.github/workflows/build-and-package.yml`:

```yaml
on:
  push:
    branches: [ main, feature/* ]  # Add or change branches
  pull_request:
    branches: [ main, feature/* ]
```

### Change JDK Version

```yaml
- uses: actions/setup-java@v3
  with:
    java-version: '17'  # Change to JDK 17 if needed
```

### Change Runner OS

```yaml
runs-on: ubuntu-latest         # For Linux
# or
runs-on: macos-latest          # For Mac
```

### Disable Tests

Currently disabled with `-DskipTests`. To run tests:

```yaml
run: mvn clean package  # Remove -DskipTests
```

### Artifact Retention

By default, artifacts are kept for 90 days. To change:

```yaml
- uses: actions/upload-artifact@v3
  with:
    name: remote-client-package
    path: remote-client.zip
    retention-days: 7  # Keep for 7 days
```

---

## 📊 Monitoring Builds

### GitHub Actions Dashboard

1. Go to **Actions** tab in repository
2. See all workflow runs with status:
   - 🟢 **Success** (green checkmark)
   - 🔴 **Failed** (red X)
   - 🟡 **In Progress** (loading spinner)

### Detailed Logs

Click on a workflow run to see:
- Build output
- Step-by-step execution
- Error messages (if any)

---

## ⚠️ Troubleshooting

### Build Failed - "Java not found"

**Fix:** The workflow uses `actions/setup-java@v3` which installs Java automatically.
If it still fails, check:
- Java version compatibility (pom.xml says `<source>11</source>`)
- Maven version compatibility

### Build Failed - "Maven not found"

**Fix:** Maven is pre-installed on GitHub-hosted runners.
If custom runner, ensure Maven is installed:

```bash
# Install Maven on Ubuntu
sudo apt-get install maven

# On Mac
brew install maven

# On Windows
choco install maven
```

### Artifacts Not Generated

**Possible causes:**
1. Maven build failed → Check Maven output logs
2. Packaging script failed → Check PowerShell script logs
3. Insufficient disk space → Unlikely on GitHub runners

**Fix:** Check detailed logs in GitHub Actions dashboard

### ZIP File Corrupted

**Fix:** Re-run workflow manually
```
GitHub → Actions → [workflow] → Run workflow → Run workflow (confirm)
```

---

## 🔐 GitHub Secrets (Optional)

For advanced use cases (e.g., auto-upload to servers), you can use GitHub Secrets:

1. Go to **Settings** → **Secrets and variables** → **Actions**
2. Create new repository secret (e.g., `DEPLOY_SERVER_IP`)
3. Use in workflow: `${{ secrets.DEPLOY_SERVER_IP }}`

Example:
```yaml
- run: |
    echo "Uploading to server..."
    scp remote-client.zip user@${{ secrets.DEPLOY_SERVER_IP }}:/opt/
```

---

## 📈 Future Enhancements

Current workflow can be extended to:

1. ✨ **Code Coverage**: Add Jacoco plugin, upload to Codecov
2. ✨ **Code Quality**: Integrate SonarQube or CodeFactor
3. ✨ **Security Scanning**: Add OWASP/Snyk vulnerability checks
4. ✨ **Auto-Release**: Create GitHub Release automatically
5. ✨ **Notifi​cations**: Slack/Discord notifications on build status
6. ✨ **Multi-OS Build**: Run on Windows, Linux, Mac in parallel
7. ✨ **.EXE Generation**: Call Launch4j in CI/CD pipeline
8. ✨ **Docker Integration**: Build Docker images for deployment

---

## 📚 Resources

- **GitHub Actions Docs**: https://docs.github.com/actions
- **Maven Plugin Docs**: https://maven.apache.org/plugins/
- **Java JDK**: https://adoptium.net/
- **Speed up builds**: https://github.com/actions/setup-java#caching-packages-data

---

## ✅ Checklist

Before committing to GitHub:

- [ ] `pom.xml` configured correctly
- [ ] `scripts/package.ps1` is executable
- [ ] `.github/workflows/build-and-package.yml` exists
- [ ] Local build works: `.\build.bat` or `bash build.sh`
- [ ] No large binary files committed (use `.gitignore`)
- [ ] `.gitignore` includes: `target/`, `dist/`, `*.zip`, `*.log`

**You're all set! GitHub Actions will now automatically build your project! 🚀**

