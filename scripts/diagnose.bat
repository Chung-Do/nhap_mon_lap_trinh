@echo off
REM Diagnostic script to verify build environment and configuration
REM Usage: .\scripts\diagnose.bat

echo.
echo ======================================
echo Environment Diagnostics
echo ======================================
echo.

REM Check Java
echo [1/5] Checking Java installation...
where java >nul 2>nul
if errorlevel 1 (
    echo ❌ Java NOT found!
    echo Please install from: https://adoptium.net/
    echo.
) else (
    for /f "tokens=*" %%i in ('java -version 2^>^&1 ^| findstr /R "version"') do (
        echo ✓ Java found: %%i
    )
)
echo.

REM Check Maven
echo [2/5] Checking Maven installation...
where mvn >nul 2>nul
if errorlevel 1 (
    echo ❌ Maven NOT found!
    echo Please install from: https://maven.apache.org/
    echo.
) else (
    for /f "tokens=*" %%i in ('mvn -version 2^>^&1 ^| findstr /R "Apache"') do (
        echo ✓ Maven found: %%i
    )
)
echo.

REM Check PowerShell
echo [3/5] Checking PowerShell installation...
where powershell >nul 2>nul
if errorlevel 1 (
    echo ❌ PowerShell NOT found!
    echo (This is unusual - PowerShell should be on Windows)
    echo.
) else (
    echo ✓ PowerShell found
)
echo.

REM Check project files
echo [4/5] Checking project structure...
set MISSING_FILES=0

if not exist "pom.xml" (
    echo ❌ Missing: pom.xml
    set MISSING_FILES=1
)

if not exist "src\" (
    echo ❌ Missing: src folder
    set MISSING_FILES=1
)

if not exist ".github\workflows\build-and-package.yml" (
    echo ❌ Missing: .github\workflows\build-and-package.yml
    set MISSING_FILES=1
)

if not exist "scripts\package.ps1" (
    echo ❌ Missing: scripts\package.ps1
    set MISSING_FILES=1
)

if %MISSING_FILES% equ 0 (
    echo ✓ All project files present
) else (
    echo ❌ Some files missing!
    echo Please check your git clone
)
echo.

REM Check write permissions
echo [5/5] Checking write permissions...
set "TEST_FILE=.build_test"
echo test > %TEST_FILE% 2>nul
if errorlevel 1 (
    echo ❌ Cannot write to project directory!
    echo Check permissions: right-click folder ^> Properties
) else (
    del %TEST_FILE%
    echo ✓ Write permissions OK
)
echo.

REM Final summary
echo ======================================
echo Diagnostics Complete
echo ======================================
echo.
echo Next steps:
echo 1. If any ❌ errors above, fix them first
echo 2. Run: .\build.bat
echo 3. Check for ZIPs: remote-client.zip, remote-server.zip
echo.
pause
