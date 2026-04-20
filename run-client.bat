@echo off
echo Starting Remote Control Client...
echo.

if not exist "bin" (
    echo Project not compiled yet!
    echo Run: compile.bat
    pause
    exit /b 1
)

java -cp "bin;lib\*" client.ClientMain
pause
