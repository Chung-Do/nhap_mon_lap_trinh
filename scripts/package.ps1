# PowerShell script to package Client and Server into ZIP files
# Usage: .\scripts\package.ps1

# Stop on error
$ErrorActionPreference = "Stop"

Write-Host "======================================"
Write-Host "Remote Control App - Packaging Script"
Write-Host "======================================"
Write-Host ""

$SourcePath = Split-Path -Parent $MyInvocation.MyCommand.Path | Split-Path -Parent
$TargetPath = Join-Path $SourcePath "target"
$DistPath = Join-Path $SourcePath "dist"
$OutPath = $SourcePath

# Check if JAR files exist
Write-Host "Checking for JAR files..."
$ClientJar = Join-Path $TargetPath "remote-client.jar"
$ServerJar = Join-Path $TargetPath "remote-server.jar"

if (-not (Test-Path $ClientJar)) {
    Write-Host "❌ ERROR: remote-client.jar not found!"
    Write-Host "Please run: mvn clean package -DskipTests"
    exit 1
}

if (-not (Test-Path $ServerJar)) {
    Write-Host "❌ ERROR: remote-server.jar not found!"
    Write-Host "Please run: mvn clean package -DskipTests"
    exit 1
}

Write-Host "✓ JAR files found"
Write-Host ""

# Clean up old dist folder
Write-Host "Cleaning up old dist folder..."
if (Test-Path $DistPath) {
    Remove-Item -Path $DistPath -Recurse -Force
}
Write-Host "✓ Cleaned"
Write-Host ""

# Create Client package structure
Write-Host "Creating Client package structure..."
New-Item -Path "$DistPath\remote-client" -ItemType Directory | Out-Null
Copy-Item -Path $ClientJar -Destination "$DistPath\remote-client\remote-client.jar"

# Create Client README
$clientReadme = @'
===============================================================
REMOTE CONTROL CLIENT APPLICATION
===============================================================

Version: 1.0.0
Language: Java 11+
Author: Your Name
Installation Date: Run this from the extracted ZIP

PREREQUISITES:
- Java 11 or higher installed
- Network connectivity to Server (LAN)

INSTALLATION:
1. Extract remote-client.zip to a folder
2. Ensure Java is in your system PATH
3. Run: java -jar remote-client.jar

FEATURES:
- List/Start/Stop Applications
- List/Start/Stop/Kill Processes
- Screenshot Capture
- Keylogger
- File Transfer (Download/Upload)
- System Control (Shutdown/Restart)
- Webcam Stream
- Network Monitoring
- Remote Desktop
- System Lock

TROUBLESHOOTING:
Q: "Java not found"
A: Install Java 11+ from https://adoptium.net/

Q: Cannot connect to server
A: Ensure server is running and both machines are on same LAN
   Check firewall settings (port 8888)

For more info, see: log.md in project root

===============================================================
'@
$clientReadme | Set-Content -Path "$DistPath\remote-client\README.txt" -Encoding UTF8

# Create Client launcher batch file
$clientRunBat = @'
@echo off
REM Launch Remote Control Client
REM Prerequisites: Java 11+

setlocal enabledelayedexpansion

set JAR_FILE=remote-client.jar

if not exist "!JAR_FILE!" (
    echo ERROR: !JAR_FILE! not found in current directory
    echo Please ensure you extracted all files from the ZIP
    pause
    exit /b 1
)

where java >nul 2>nul
if errorlevel 1 (
    echo ERROR: Java not found!
    echo Please install Java 11+ from https://adoptium.net/
    pause
    exit /b 1
)

echo Starting Remote Control Client...
java -jar "!JAR_FILE!"
pause
'@
$clientRunBat | Set-Content -Path "$DistPath\remote-client\run.bat" -Encoding ASCII

# Create config template for Client
$clientConfig = @'
# Remote Control Client Configuration
# Edit this file to customize behavior

# Server connection settings
server.host=192.168.1.100
server.port=8888
connection.timeout=10000

# GUI settings
gui.window.width=1200
gui.window.height=800
gui.log.buffer.lines=1000

# Screenshot settings
screenshot.quality=90
screenshot.format=PNG

# Webcam settings
webcam.fps=15
webcam.quality=80

# File transfer settings
file.chunk.size=1048576

# Network monitoring settings
monitor.refresh.interval=1000
'@
$clientConfig | Set-Content -Path "$DistPath\remote-client\config.properties" -Encoding UTF8

Write-Host "✓ Client package created"
Write-Host ""

# Create Server package structure
Write-Host "Creating Server package structure..."
New-Item -Path "$DistPath\remote-server" -ItemType Directory | Out-Null
Copy-Item -Path $ServerJar -Destination "$DistPath\remote-server\remote-server.jar"

# Create Server README
$serverReadme = @'
===============================================================
REMOTE CONTROL SERVER APPLICATION
===============================================================

Version: 1.0.0
Language: Java 11+
Author: Your Name
Installation Date: Run this from the extracted ZIP

PREREQUISITES:
- Java 11 or higher installed
- Network connectivity to Client (LAN)
- Windows OS (some features are Windows-specific)

INSTALLATION:
1. Extract remote-server.zip to a folder
2. Ensure Java is in your system PATH
3. Run: java -jar remote-server.jar

FEATURES:
- Application Management
- Process Control
- Screen Capture
- Keylogger
- File Transfer (Upload/Download)
- System Control (Shutdown/Restart)
- Webcam Capture
- Network Monitoring
- Remote Desktop
- System Lock

SECURITY WARNING:
This server application allows remote control of your system!
Only run this on a machine you own and control.
Use on untrusted networks at your own risk.

INSTALLATION AS SERVICE (Optional):
To run as Windows Service, use NSSM:
1. Download NSSM: https://nssm.cc/
2. Run: nssm install RemoteControlServer "java -jar remote-server.jar"
3. Run: nssm start RemoteControlServer

TROUBLESHOOTING:
Q: "Java not found"
A: Install Java 11+ from https://adoptium.net/

Q: "Port 8888 already in use"
A: Edit config.properties to use different port

Q: Cannot receive commands from client
A: Check firewall - ensure TCP port 8888 is open
   Run: netstat -an | findstr LISTENING

For more info, see: log.md in project root

===============================================================
'@
$serverReadme | Set-Content -Path "$DistPath\remote-server\README.txt" -Encoding UTF8

# Create Server launcher batch file
$serverRunBat = @'
@echo off
REM Launch Remote Control Server
REM Prerequisites: Java 11+
REM NOTE: Some features require Administrator privileges

setlocal enabledelayedexpansion

set JAR_FILE=remote-server.jar

if not exist "!JAR_FILE!" (
    echo ERROR: !JAR_FILE! not found in current directory
    echo Please ensure you extracted all files from the ZIP
    pause
    exit /b 1
)

where java >nul 2>nul
if errorlevel 1 (
    echo ERROR: Java not found!
    echo Please install Java 11+ from https://adoptium.net/
    pause
    exit /b 1
)

echo Starting Remote Control Server...
echo NOTE: Some features may require Administrator privileges
java -jar "!JAR_FILE!"
pause
'@
$serverRunBat | Set-Content -Path "$DistPath\remote-server\run.bat" -Encoding ASCII

# Create config template for Server
$serverConfig = @'
# Remote Control Server Configuration
# Edit this file to customize behavior

# Server listening settings
server.port=8888
server.listen.timeout=30000

# GUI settings
gui.window.width=800
gui.window.height=600
gui.show.on.startup=true
gui.log.buffer.lines=1000

# System control settings
allow.system.shutdown=true
allow.system.restart=true
allow.lock.system=true

# File transfer settings
file.chunk.size=1048576
file.transfer.timeout=300000

# Keylogger settings
keylogger.enabled=true
keylogger.log.file=keylog.txt

# Webcam settings
webcam.enabled=true
webcam.fps=15
webcam.quality=80

# Network monitoring settings
monitor.enabled=true
monitor.refresh.interval=1000
'@
$serverConfig | Set-Content -Path "$DistPath\remote-server\config.properties" -Encoding UTF8

Write-Host "✓ Server package created"
Write-Host ""

# Create ZIP files
Write-Host "Creating ZIP packages..."

# Client ZIP
$ClientZipPath = Join-Path $OutPath "remote-client.zip"
if (Test-Path $ClientZipPath) {
    Remove-Item $ClientZipPath -Force
}
Compress-Archive -Path "$DistPath\remote-client\*" -DestinationPath $ClientZipPath
Write-Host "✓ Created: remote-client.zip"

# Server ZIP
$ServerZipPath = Join-Path $OutPath "remote-server.zip"
if (Test-Path $ServerZipPath) {
    Remove-Item $ServerZipPath -Force
}
Compress-Archive -Path "$DistPath\remote-server\*" -DestinationPath $ServerZipPath
Write-Host "✓ Created: remote-server.zip"

Write-Host ""
Write-Host "======================================"
Write-Host "✓ PACKAGING COMPLETED SUCCESSFULLY"
Write-Host "======================================"
Write-Host ""
Write-Host "Output files:"
Write-Host "  • $ClientZipPath"
Write-Host "  • $ServerZipPath"
Write-Host ""
Write-Host "Next step: Extract ZIPs and run run.bat"
Write-Host ""
