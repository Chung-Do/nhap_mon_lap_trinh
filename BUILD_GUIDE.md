# Remote Control App - Build & Package Guide

## Quick Start

### Build locally:
```bash
# Windows
.\build.bat

# Unix/Mac
bash build.sh
```

### GitHub Actions - Automatic Build

Every push to `main` or `master` branch will trigger automatic build via GitHub Actions.

**Artifacts are available in:**
- GitHub Actions tab → Workflow run → Artifacts
- Download `remote-client-package` and `remote-server-package` ZIPs

## Manual Build & Package

### Prerequisites
- Java 11+
- Maven 3.6+

### Step 1: Build with Maven
```bash
mvn clean package -DskipTests
```

This creates:
- `target/remote-client.jar` (Client application)
- `target/remote-server.jar` (Server application)

### Step 2: Package as ZIP

#### Windows PowerShell
```powershell
.\scripts\package.ps1
```

#### Windows Batch
```batch
.\scripts\package.bat
```

#### Unix/Mac Bash
```bash
bash ./scripts/package.sh
```

This creates:
- `remote-client.zip` - Client package with JAR and config
- `remote-server.zip` - Server package with JAR and config

## Package Contents

### remote-client.zip
```
remote-client/
├── remote-client.jar        (Application executable)
├── README.txt               (Instructions)
├── config.properties        (Configuration)
└── run.bat                  (Windows launcher)
```

### remote-server.zip
```
remote-server/
├── remote-server.jar        (Application executable)
├── README.txt               (Instructions)
├── config.properties        (Configuration)
└── run.bat                  (Windows launcher)
```

## Running the Application

### From JAR directly
```bash
java -jar remote-client.jar
java -jar remote-server.jar
```

### From Windows launcher
```batch
cd remote-client
double-click run.bat
```

## Advanced: Build .EXE (Optional)

To convert JAR to .exe Windows executable:

1. Install Launch4j: https://launch4j.sourceforge.net/
2. Configure `launch4j-client.xml` and `launch4j-server.xml`
3. Run:
```bash
launch4jc launch4j-client.xml
launch4jc launch4j-server.xml
```

This creates:
- `remote-client.exe`
- `remote-server.exe`

## CI/CD Pipeline

GitHub Actions workflow (`.github/workflows/build-and-package.yml`):

1. ✅ Checkout code on push
2. ✅ Setup Java 11
3. ✅ Build with Maven
4. ✅ Create package structure
5. ✅ Zip client and server
6. ✅ Upload as artifacts
7. ✅ (Optional) Create GitHub Release on tag

### Trigger builds:
- **Automatic**: Push to main, master, develop branches
- **Manual**: GitHub Actions tab → Run workflow

## Troubleshooting

### "mvn command not found"
→ Install Maven from https://maven.apache.org/download.cgi

### "Java not found"
→ Install Java 11+ from https://adoptium.net/

### "PowerShell not recognized"
→ Use `cmd.exe` and run `package.bat` instead

### Build fails with "out of memory"
→ Increase Maven heap: `set MAVEN_OPTS=-Xmx1024m` (Windows)
→ Or: `export MAVEN_OPTS=-Xmx1024m` (Unix/Mac)

## Project Structure

```
nhap_mon_lap_trinh/
├── .github/
│   └── workflows/
│       └── build-and-package.yml    (GitHub Actions CI/CD)
├── scripts/
│   ├── package.ps1                   (PowerShell packaging)
│   ├── package.bat                  (Batch packaging)
│   └── package.sh                   (Bash packaging)
├── src/
│   ├── client/                       (Client source code)
│   ├── common/                       (Shared code)
│   └── server/                       (Server source code)
├── target/
│   ├── remote-client.jar
│   └── remote-server.jar
├── pom.xml                           (Maven configuration)
├── build.bat                         (Windows quick build)
├── build.sh                          (Unix/Mac quick build)
├── launch4j-client.xml              (Launch4j client config)
└── launch4j-server.xml              (Launch4j server config)
```

## Key Points

✅ **pom.xml** config: Builds 2 separate JARs with Maven Assembly Plugin  
✅ **GitHub Actions**: Runs on every push, creates ZIP artifacts  
✅ **Package structure**: Organized directories with config files  
✅ **Scripts**: Cross-platform build automation  
✅ **Launch4j**: Optional .exe generation  

