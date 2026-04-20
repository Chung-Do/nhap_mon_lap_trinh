@echo off
echo Compiling Remote Control Application...
echo.

REM Create directories
if not exist "bin" mkdir bin
if not exist "logs" mkdir logs
if not exist "lib" mkdir lib

REM Check JSON library
if not exist "lib\json-20230227.jar" (
    echo WARNING: JSON library not found!
    echo Please download manually from:
    echo https://repo1.maven.org/maven2/org/json/json/20230227/json-20230227.jar
    echo Place it in lib\ folder and run this script again.
    pause
    exit /b 1
)

REM Compile
echo Compiling source files...
javac -d bin -cp "lib\*" src\common\*.java src\server\*.java src\server\handlers\*.java src\server\ui\*.java src\client\*.java src\client\ui\*.java src\client\ui\tabs\*.java

if %errorlevel% equ 0 (
    echo.
    echo Compilation successful!
    echo.
    echo Output: bin\
    echo Logs: logs\
    echo.
    echo To run:
    echo   Server: run-server.bat
    echo   Client: run-client.bat
) else (
    echo.
    echo Compilation failed!
    echo Please check the error messages above.
    pause
    exit /b 1
)

pause
