@echo off
chcp 65001 >nul 2>&1

if not exist "uploads" mkdir uploads
if not exist "logs" mkdir logs

echo Starting backend...
start /b java -jar trade-platform-1.0.0.jar --spring.config.location=application-prod.yml > logs\app.log 2>&1

timeout /t 10 /nobreak > nul

netstat -an | findstr ":8081" | findstr "LISTENING" > nul
if %errorlevel% equ 0 (
    echo [OK] Backend started on port 8081
) else (
    echo [FAIL] Check logs\app.log
)
pause
