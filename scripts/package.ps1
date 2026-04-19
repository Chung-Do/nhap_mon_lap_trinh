# PowerShell script to create single-file .exe bundles using jlink + Launch4j + Warp Packer.
# The resulting .exe requires no Java installation on the target machine.
# Usage: .\scripts\package.ps1

$ErrorActionPreference = "Stop"

Write-Host "======================================"
Write-Host "Remote Control App - Packaging Script"
Write-Host "======================================"
Write-Host ""

$RootPath   = Split-Path -Parent $MyInvocation.MyCommand.Path | Split-Path -Parent
$TargetPath = Join-Path $RootPath "target"
$DistPath   = Join-Path $RootPath "dist"

# Warp packer must be placed at repo root (downloaded by CI or manually)
$WarpPacker = Join-Path $RootPath "warp-packer.exe"

# ---------------------------------------------------------------------------
# Verify prerequisites
# ---------------------------------------------------------------------------
Write-Host "Checking prerequisites..."

$ClientJar = Join-Path $TargetPath "remote-client.jar"
$ServerJar = Join-Path $TargetPath "remote-server.jar"

if (-not (Test-Path $ClientJar)) {
    Write-Error "remote-client.jar not found. Run: mvn clean package -DskipTests"
}
if (-not (Test-Path $ServerJar)) {
    Write-Error "remote-server.jar not found. Run: mvn clean package -DskipTests"
}
if (-not (Test-Path $WarpPacker)) {
    Write-Error "warp-packer.exe not found at $WarpPacker"
}

Write-Host "✓ JAR files found"
Write-Host "✓ warp-packer.exe found"
Write-Host ""

# ---------------------------------------------------------------------------
# Clean dist folder
# ---------------------------------------------------------------------------
Write-Host "Cleaning up old dist folder..."
if (Test-Path $DistPath) { Remove-Item -Path $DistPath -Recurse -Force }
New-Item -Path $DistPath -ItemType Directory | Out-Null
Write-Host "✓ Cleaned"
Write-Host ""

# ---------------------------------------------------------------------------
# Create a minimal JRE with jlink (shared by both apps, ~40 MB)
# ---------------------------------------------------------------------------
$JrePath = Join-Path $DistPath "jre"
$Modules = (
    "java.base,"           +  # core runtime
    "java.datatransfer,"   +  # clipboard (AWT)
    "java.desktop,"        +  # Swing/AWT GUI, BufferedImage (webcam)
    "java.logging,"        +  # java.util.logging
    "java.management,"     +  # JMX / JNA platform
    "java.naming,"         +  # JNDI (JNA dependency)
    "java.net.http,"       +  # HttpClient
    "java.prefs,"          +  # Preferences API
    "java.sql,"            +  # JDBC (transitive from several libs)
    "java.xml,"            +  # XML / DOM (JSON/config parsing)
    "jdk.unsupported"         # sun.misc.Unsafe (JNA, JNativeHook)
).Replace(" ", "")

Write-Host "Creating minimal JRE with jlink..."
& jlink --output $JrePath --add-modules $Modules --no-header-files --no-man-pages --compress=2
if ($LASTEXITCODE -ne 0) { Write-Error "jlink failed" }
Write-Host "✓ Minimal JRE created at $JrePath"
Write-Host ""

# ---------------------------------------------------------------------------
# Helper: build one single-file .exe (Launch4j wrapper + warp-packer bundle)
# ---------------------------------------------------------------------------
function Build-SingleExe {
    param(
        [string]$AppName,       # e.g. "RemoteServer"
        [string]$SourceJar,     # absolute path to fat JAR
        [string]$L4jTemplate,   # path to launch4j XML template
        [string]$ExeBaseName,   # inner exe filename without extension
        [string]$ConfigContent, # content for config.properties
        [string]$ReadmeContent  # content for README.txt
    )

    $StagingDir = Join-Path $DistPath "staging\$AppName"
    New-Item -Path $StagingDir -ItemType Directory -Force | Out-Null

    # Copy minimal JRE into staging dir so launch4j can find it at path "jre"
    Write-Host "[$AppName] Copying JRE..."
    Copy-Item -Path $JrePath -Destination (Join-Path $StagingDir "jre") -Recurse

    # Generate a temp launch4j config with absolute jar / outfile paths
    $TempXml    = Join-Path $DistPath "l4j-$AppName.xml"
    $OutExePath = Join-Path $StagingDir "$ExeBaseName.exe"
    [xml]$XmlDoc = Get-Content $L4jTemplate
    $XmlDoc.launch4jConfig.jar     = $SourceJar
    $XmlDoc.launch4jConfig.outfile = $OutExePath
    $XmlDoc.Save($TempXml)

    # Run Launch4j to wrap the JAR into a .exe that uses the bundled jre/
    Write-Host "[$AppName] Running Launch4j..."
    & launch4jc $TempXml
    if ($LASTEXITCODE -ne 0) { Write-Error "launch4jc failed for $AppName" }
    Write-Host "[$AppName] ✓ $ExeBaseName.exe created"

    # Place config and README alongside the exe (warp will bundle them too).
    # On first run warp extracts to %LOCALAPPDATA%\warp\packages\<sha256-of-exe>\
    # so config.properties lives there; launch4j chdir=. ensures the JVM CWD
    # matches that directory.
    $ConfigContent | Set-Content -Path (Join-Path $StagingDir "config.properties") -Encoding UTF8
    $ReadmeContent | Set-Content -Path (Join-Path $StagingDir "README.txt")         -Encoding UTF8

    # Bundle everything into a single self-contained .exe with warp-packer
    $FinalExe = Join-Path $RootPath "$AppName.exe"
    Write-Host "[$AppName] Running warp-packer..."
    & $WarpPacker --arch windows-x64 --input_dir $StagingDir --exec "$ExeBaseName.exe" --output $FinalExe
    if ($LASTEXITCODE -ne 0) { Write-Error "warp-packer failed for $AppName" }
    Write-Host "[$AppName] ✓ $AppName.exe (single-file, no Java needed)"
    Write-Host ""
}

# ---------------------------------------------------------------------------
# Config / README text blocks
# ---------------------------------------------------------------------------
$ServerConfig = @'
# Remote Control Server Configuration
server.port=8888
server.listen.timeout=30000
gui.window.width=800
gui.window.height=600
gui.show.on.startup=true
gui.log.buffer.lines=1000
allow.system.shutdown=true
allow.system.restart=true
allow.lock.system=true
file.chunk.size=1048576
file.transfer.timeout=300000
keylogger.enabled=true
keylogger.log.file=keylog.txt
webcam.enabled=true
webcam.fps=15
webcam.quality=80
monitor.enabled=true
monitor.refresh.interval=1000
'@

$ServerReadme = @'
REMOTE CONTROL SERVER APPLICATION
==================================
Version: 1.0.0

NO JAVA INSTALLATION REQUIRED.
Double-click RemoteServer.exe to start.
(Some features require Administrator privileges.)

FEATURES: App/Process management, Screen capture, Keylogger,
          File transfer, System control, Webcam, Network monitor.

SECURITY WARNING:
This server allows remote control of your system.
Only run on machines you own. Use on trusted networks only.

TROUBLESHOOTING:
- Port 8888 in use  : edit config.properties (in %LOCALAPPDATA%\warp\...)
- Firewall blocked  : allow TCP port 8888 inbound
'@

$ClientConfig = @'
# Remote Control Client Configuration
server.host=192.168.1.100
server.port=8888
connection.timeout=10000
gui.window.width=1200
gui.window.height=800
gui.log.buffer.lines=1000
screenshot.quality=90
screenshot.format=PNG
webcam.fps=15
webcam.quality=80
file.chunk.size=1048576
monitor.refresh.interval=1000
'@

$ClientReadme = @'
REMOTE CONTROL CLIENT APPLICATION
==================================
Version: 1.0.0

NO JAVA INSTALLATION REQUIRED.
Double-click RemoteClient.exe to start.

TROUBLESHOOTING:
- Cannot connect: ensure server is running and both machines
  are on the same LAN. Check firewall (TCP port 8888).
'@

# ---------------------------------------------------------------------------
# Build both executables
# ---------------------------------------------------------------------------
Build-SingleExe `
    -AppName       "RemoteServer" `
    -SourceJar     $ServerJar `
    -L4jTemplate   (Join-Path $RootPath "launch4j-server.xml") `
    -ExeBaseName   "RemoteServer" `
    -ConfigContent $ServerConfig `
    -ReadmeContent $ServerReadme

Build-SingleExe `
    -AppName       "RemoteClient" `
    -SourceJar     $ClientJar `
    -L4jTemplate   (Join-Path $RootPath "launch4j-client.xml") `
    -ExeBaseName   "RemoteClient" `
    -ConfigContent $ClientConfig `
    -ReadmeContent $ClientReadme

# ---------------------------------------------------------------------------
# Summary
# ---------------------------------------------------------------------------
Write-Host "======================================"
Write-Host "✓ PACKAGING COMPLETED SUCCESSFULLY"
Write-Host "======================================"
Write-Host ""
Write-Host "Output files (single-file, click to run):"
Write-Host "  • $(Join-Path $RootPath 'RemoteServer.exe')"
Write-Host "  • $(Join-Path $RootPath 'RemoteClient.exe')"
Write-Host ""
Write-Host "No Java required. First run extracts in ~2-3 s; subsequent runs are instant."
Write-Host ""
