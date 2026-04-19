# PowerShell script to package Client and Server into self-contained ZIP files
# Uses jpackage (JDK 17+) to bundle the JRE so no Java installation is required.
# Usage: .\scripts\package.ps1

# Stop on error
$ErrorActionPreference = "Stop"

Write-Host "======================================"
Write-Host "Remote Control App - Packaging Script"
Write-Host "======================================"
Write-Host ""

$SourcePath = Split-Path -Parent $MyInvocation.MyCommand.Path | Split-Path -Parent
$TargetPath = Join-Path $SourcePath "target"
$DistPath   = Join-Path $SourcePath "dist"
$OutPath    = $SourcePath

# ---------------------------------------------------------------------------
# Verify JAR files
# ---------------------------------------------------------------------------
Write-Host "Checking for JAR files..."
$ClientJar = Join-Path $TargetPath "remote-client.jar"
$ServerJar = Join-Path $TargetPath "remote-server.jar"

if (-not (Test-Path $ClientJar)) {
    Write-Host "ERROR: remote-client.jar not found!"
    Write-Host "Please run: mvn clean package -DskipTests"
    exit 1
}
if (-not (Test-Path $ServerJar)) {
    Write-Host "ERROR: remote-server.jar not found!"
    Write-Host "Please run: mvn clean package -DskipTests"
    exit 1
}
Write-Host "✓ JAR files found"
Write-Host ""

# ---------------------------------------------------------------------------
# Clean dist folder
# ---------------------------------------------------------------------------
Write-Host "Cleaning up old dist folder..."
if (Test-Path $DistPath) {
    Remove-Item -Path $DistPath -Recurse -Force
}
Write-Host "✓ Cleaned"
Write-Host ""

# ---------------------------------------------------------------------------
# Helper function: create a self-contained app image using jpackage
# ---------------------------------------------------------------------------
function New-JPackageAppImage {
    param(
        [string]$AppName,
        [string]$SourceJar
    )
    $InputPath = "$DistPath\_input\$AppName"
    $JarName   = [System.IO.Path]::GetFileName($SourceJar)
    New-Item -Path $InputPath -ItemType Directory | Out-Null
    Copy-Item $SourceJar "$InputPath\$JarName"

    Write-Host "Building self-contained $AppName app image (this bundles the JRE)..."
    & jpackage `
        --type app-image `
        --name $AppName `
        --input $InputPath `
        --main-jar $JarName `
        --dest $DistPath `
        --java-options "-Dfile.encoding=UTF-8"
    if ($LASTEXITCODE -ne 0) { Write-Host "ERROR: jpackage failed for $AppName"; exit 1 }
    Write-Host "✓ $AppName app image created"
    Write-Host ""
}

# ---------------------------------------------------------------------------
# Build self-contained app images with jpackage
# ---------------------------------------------------------------------------
New-JPackageAppImage -AppName "RemoteServer" -SourceJar $ServerJar
New-JPackageAppImage -AppName "RemoteClient" -SourceJar $ClientJar

# ---------------------------------------------------------------------------
# Add README and config files to each app image
# ---------------------------------------------------------------------------
$serverReadme = @'
===============================================================
REMOTE CONTROL SERVER APPLICATION
===============================================================

Version: 1.0.0
Author: Your Name

NO JAVA INSTALLATION REQUIRED - Java runtime is bundled.

INSTALLATION:
1. Extract remote-server.zip to any folder
2. Run: RemoteServer.exe
   (Some features require Administrator privileges)

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

TROUBLESHOOTING:
Q: "Port 8888 already in use"
A: Edit config.properties to use a different port

Q: Cannot receive commands from client
A: Check firewall - ensure TCP port 8888 is open
   Run: netstat -an | findstr LISTENING

===============================================================
'@
$serverReadme | Set-Content -Path "$DistPath\RemoteServer\README.txt" -Encoding UTF8

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
$serverConfig | Set-Content -Path "$DistPath\RemoteServer\config.properties" -Encoding UTF8

$clientReadme = @'
===============================================================
REMOTE CONTROL CLIENT APPLICATION
===============================================================

Version: 1.0.0
Author: Your Name

NO JAVA INSTALLATION REQUIRED - Java runtime is bundled.

INSTALLATION:
1. Extract remote-client.zip to any folder
2. Run: RemoteClient.exe

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
Q: Cannot connect to server
A: Ensure server is running and both machines are on same LAN
   Check firewall settings (port 8888)

===============================================================
'@
$clientReadme | Set-Content -Path "$DistPath\RemoteClient\README.txt" -Encoding UTF8

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
$clientConfig | Set-Content -Path "$DistPath\RemoteClient\config.properties" -Encoding UTF8

Write-Host "✓ README and config files added"
Write-Host ""

# ---------------------------------------------------------------------------
# Create ZIP files
# ---------------------------------------------------------------------------
Write-Host "Creating ZIP packages..."

$ClientZipPath = Join-Path $OutPath "remote-client.zip"
if (Test-Path $ClientZipPath) { Remove-Item $ClientZipPath -Force }
Compress-Archive -Path "$DistPath\RemoteClient\*" -DestinationPath $ClientZipPath
Write-Host "✓ Created: remote-client.zip"

$ServerZipPath = Join-Path $OutPath "remote-server.zip"
if (Test-Path $ServerZipPath) { Remove-Item $ServerZipPath -Force }
Compress-Archive -Path "$DistPath\RemoteServer\*" -DestinationPath $ServerZipPath
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
Write-Host "No Java installation needed - extract the ZIP and run the .exe directly."
Write-Host ""
