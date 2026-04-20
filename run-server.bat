@echo off
echo Starting Remote Control Server...
echo.

if not exist "bin" (
    echo Project not compiled yet!
    echo Run: compile.bat
    pause
    exit /b 1
)

java -cp "bin;lib\*" server.ServerMain
pause
