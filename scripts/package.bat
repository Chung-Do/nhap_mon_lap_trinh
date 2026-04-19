@echo off
REM Batch script to package Client and Server into ZIP files
REM Usage: .\scripts\package.bat

setlocal enabledelayedexpansion

echo.
echo ======================================
echo Remote Control App - Packaging Script
echo ======================================
echo.

REM Get script directory
set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."
set "TARGET_PATH=%PROJECT_ROOT%\target"
set "DIST_PATH=%PROJECT_ROOT%\dist"
set "OUT_PATH=%PROJECT_ROOT%"

REM Check if JAR files exist
echo Checking for JAR files...
if not exist "%TARGET_PATH%\remote-client.jar" (
    echo ERROR: remote-client.jar not found!
    echo Please run: mvn clean package -DskipTests
    pause
    exit /b 1
)

if not exist "%TARGET_PATH%\remote-server.jar" (
    echo ERROR: remote-server.jar not found!
    echo Please run: mvn clean package -DskipTests
    pause
    exit /b 1
)

echo OK - JAR files found
echo.

REM Clean up old dist folder
echo Cleaning up old dist folder...
if exist "%DIST_PATH%" (
    rmdir /s /q "%DIST_PATH%"
)
echo OK - Cleaned
echo.

REM Create Client package structure
echo Creating Client package structure...
mkdir "%DIST_PATH%\remote-client" 2>nul
copy "%TARGET_PATH%\remote-client.jar" "%DIST_PATH%\remote-client\" >nul

REM Create Client README
(
echo.
echo ===============================================================
echo REMOTE CONTROL CLIENT APPLICATION
echo ===============================================================
echo.
echo Version: 1.0.0
echo Language: Java 11+
echo Author: Your Name
echo Date: Run this from the extracted ZIP
echo.
echo PREREQUISITES:
echo - Java 11 or higher installed
echo - Network connectivity to Server (LAN)
echo.
echo INSTALLATION:
echo 1. Extract remote-client.zip to a folder
echo 2. Ensure Java is in your system PATH
echo 3. Run: java -jar remote-client.jar
echo.
echo FEATURES:
echo - List/Start/Stop Applications
echo - List/Start/Stop/Kill Processes
echo - Screenshot Capture
echo - Keylogger
echo - File Transfer (Download/Upload)
echo - System Control (Shutdown/Restart)
echo - Webcam Stream
echo - Network Monitoring
echo - Remote Desktop
echo - System Lock
echo.
echo TROUBLESHOOTING:
echo Q: "Java not found"
echo A: Install Java 11+ from https://adoptium.net/
echo.
echo Q: Cannot connect to server
echo A: Ensure server is running and both machines are on same LAN
echo    Check firewall settings (port 8888)
echo.
echo For more info, see: log.md in project root
echo.
echo ===============================================================
) > "%DIST_PATH%\remote-client\README.txt"

REM Create Client launcher batch file
(
echo @echo off
echo REM Launch Remote Control Client
echo REM Prerequisites: Java 11+
echo.
echo setlocal enabledelayedexpansion
echo.
echo set JAR_FILE=remote-client.jar
echo.
echo if not exist "!JAR_FILE!" (
echo     echo ERROR: !JAR_FILE! not found in current directory
echo     echo Please ensure you extracted all files from the ZIP
echo     pause
echo     exit /b 1
echo )
echo.
echo where java ^>nul 2^>nul
echo if errorlevel 1 (
echo     echo ERROR: Java not found!
echo     echo Please install Java 11+ from https://adoptium.net/
echo     pause
echo     exit /b 1
echo )
echo.
echo echo Starting Remote Control Client...
echo java -jar "!JAR_FILE!"
echo pause
) > "%DIST_PATH%\remote-client\run.bat"

REM Create Client config template
(
echo # Remote Control Client Configuration
echo # Edit this file to customize behavior
echo.
echo # Server connection settings
echo server.host=192.168.1.100
echo server.port=8888
echo connection.timeout=10000
echo.
echo # GUI settings
echo gui.window.width=1200
echo gui.window.height=800
echo gui.log.buffer.lines=1000
echo.
echo # Screenshot settings
echo screenshot.quality=90
echo screenshot.format=PNG
echo.
echo # Webcam settings
echo webcam.fps=15
echo webcam.quality=80
echo.
echo # File transfer settings
echo file.chunk.size=1048576
echo.
echo # Network monitoring settings
echo monitor.refresh.interval=1000
) > "%DIST_PATH%\remote-client\config.properties"

echo OK - Client package created
echo.

REM Create Server package structure
echo Creating Server package structure...
mkdir "%DIST_PATH%\remote-server" 2>nul
copy "%TARGET_PATH%\remote-server.jar" "%DIST_PATH%\remote-server\" >nul

REM Create Server README
(
echo.
echo ===============================================================
echo REMOTE CONTROL SERVER APPLICATION
echo ===============================================================
echo.
echo Version: 1.0.0
echo Language: Java 11+
echo Author: Your Name
echo Date: Run this from the extracted ZIP
echo.
echo PREREQUISITES:
echo - Java 11 or higher installed
echo - Network connectivity to Client (LAN)
echo - Windows OS (some features are Windows-specific)
echo.
echo INSTALLATION:
echo 1. Extract remote-server.zip to a folder
echo 2. Ensure Java is in your system PATH
echo 3. Run: java -jar remote-server.jar
echo.
echo FEATURES:
echo - Application Management
echo - Process Control
echo - Screen Capture
echo - Keylogger
echo - File Transfer (Upload/Download)
echo - System Control (Shutdown/Restart)
echo - Webcam Capture
echo - Network Monitoring
echo - Remote Desktop
echo - System Lock
echo.
echo SECURITY WARNING:
echo This server application allows remote control of your system!
echo Only run this on a machine you own and control.
echo Use on untrusted networks at your own risk.
echo.
echo INSTALLATION AS SERVICE (Optional):
echo To run as Windows Service, use NSSM:
echo 1. Download NSSM: https://nssm.cc/
echo 2. Run: nssm install RemoteControlServer "java -jar remote-server.jar"
echo 3. Run: nssm start RemoteControlServer
echo.
echo TROUBLESHOOTING:
echo Q: "Java not found"
echo A: Install Java 11+ from https://adoptium.net/
echo.
echo Q: "Port 8888 already in use"
echo A: Edit config.properties to use different port
echo.
echo Q: Cannot receive commands from client
echo A: Check firewall - ensure TCP port 8888 is open
echo    Run: netstat -an ^| findstr LISTENING
echo.
echo For more info, see: log.md in project root
echo.
echo ===============================================================
) > "%DIST_PATH%\remote-server\README.txt"

REM Create Server launcher batch file
(
echo @echo off
echo REM Launch Remote Control Server
echo REM Prerequisites: Java 11+
echo REM NOTE: Some features require Administrator privileges
echo.
echo setlocal enabledelayedexpansion
echo.
echo set JAR_FILE=remote-server.jar
echo.
echo if not exist "!JAR_FILE!" (
echo     echo ERROR: !JAR_FILE! not found in current directory
echo     echo Please ensure you extracted all files from the ZIP
echo     pause
echo     exit /b 1
echo )
echo.
echo where java ^>nul 2^>nul
echo if errorlevel 1 (
echo     echo ERROR: Java not found!
echo     echo Please install Java 11+ from https://adoptium.net/
echo     pause
echo     exit /b 1
echo )
echo.
echo echo Starting Remote Control Server...
echo echo NOTE: Some features may require Administrator privileges
echo java -jar "!JAR_FILE!"
echo pause
) > "%DIST_PATH%\remote-server\run.bat"

REM Create Server config template
(
echo # Remote Control Server Configuration
echo # Edit this file to customize behavior
echo.
echo # Server listening settings
echo server.port=8888
echo server.listen.timeout=30000
echo.
echo # GUI settings
echo gui.window.width=800
echo gui.window.height=600
echo gui.show.on.startup=true
echo gui.log.buffer.lines=1000
echo.
echo # System control settings
echo allow.system.shutdown=true
echo allow.system.restart=true
echo allow.lock.system=true
echo.
echo # File transfer settings
echo file.chunk.size=1048576
echo file.transfer.timeout=300000
echo.
echo # Keylogger settings
echo keylogger.enabled=true
echo keylogger.log.file=keylog.txt
echo.
echo # Webcam settings
echo webcam.enabled=true
echo webcam.fps=15
echo webcam.quality=80
echo.
echo # Network monitoring settings
echo monitor.enabled=true
echo monitor.refresh.interval=1000
) > "%DIST_PATH%\remote-server\config.properties"

echo OK - Server package created
echo.

REM Create ZIP files using PowerShell
echo Creating ZIP packages...

powershell -NoProfile -Command "Compress-Archive -Path '%DIST_PATH%\remote-client\*' -DestinationPath '%OUT_PATH%\remote-client.zip' -Force"
echo OK - Created: remote-client.zip

powershell -NoProfile -Command "Compress-Archive -Path '%DIST_PATH%\remote-server\*' -DestinationPath '%OUT_PATH%\remote-server.zip' -Force"
echo OK - Created: remote-server.zip

echo.
echo ======================================
echo PACKAGING COMPLETED SUCCESSFULLY
echo ======================================
echo.
echo Output files:
echo   • %OUT_PATH%\remote-client.zip
echo   • %OUT_PATH%\remote-server.zip
echo.
echo Next step: Extract ZIPs and run run.bat
echo.
pause
