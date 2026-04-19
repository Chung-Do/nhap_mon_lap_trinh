@echo off
REM Build and Package script for Remote Control Application (Windows)

echo ======================================
echo Remote Control App - Build Script
echo ======================================
echo.

REM Step 1: Build with Maven
echo Step 1: Building with Maven...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo.
    echo ERROR: Maven build failed!
    echo Please check your Java installation and pom.xml configuration
    pause
    exit /b 1
)

echo.
echo Step 2: Maven build completed successfully
echo.

REM Step 2: Package into ZIP files
echo Step 3: Packaging into ZIP files...
call .\scripts\package.bat

if errorlevel 1 (
    echo Build failed!
    exit /b 1
)

echo Server JAR built: target\remote-server.jar

REM Step 2: Build Client JAR
echo.
echo Step 2: Building Client JAR...
call mvn clean package -Pclient -DskipTests

if errorlevel 1 (
    echo Build failed!
    exit /b 1
)

echo Client JAR built: target\remote-client.jar

REM Step 3: Create output directory
if not exist "build" mkdir build

REM Step 4: Convert to EXE
where launch4jc >/dev/null 2>/dev/null
if %errorlevel% == 0 (
    echo.
    echo Step 3: Converting JARs to EXE...
    launch4jc launch4j-server.xml
    launch4jc launch4j-client.xml
    echo EXE files created in build\
) else (
    echo.
    echo Launch4j not found. Skipping EXE conversion.
    echo Install from: https://launch4j.sourceforge.net/
)

echo.
echo === Build Complete! ===
echo.
echo Artifacts:
echo   - target\remote-server.jar
echo   - target\remote-client.jar
if exist "build\RemoteServer.exe" (
    echo   - build\RemoteServer.exe
    echo   - build\RemoteClient.exe
)

echo.
echo To run:
echo   Server: java -jar target\remote-server.jar
echo   Client: java -jar target\remote-client.jar

pause
