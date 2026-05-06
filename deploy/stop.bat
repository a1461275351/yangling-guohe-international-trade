@echo off
chcp 65001 >nul 2>&1

echo Stopping backend...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8081" ^| findstr "LISTENING"') do (
    taskkill /PID %%a /F
)
echo Done.
pause
