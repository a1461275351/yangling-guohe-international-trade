@echo off
chcp 65001 >nul 2>&1

REM ============================================================
REM  外综服平台 v1.0.4 启动脚本
REM  使用 JDK 17 启动（如果路径不同请修改 JAVA17 变量）
REM ============================================================

set JAVA17="C:\Program Files\Zulu\zulu-17\bin\java.exe"

REM 如果你装到别的路径，改成下面这种：
REM set JAVA17="D:\soft\java\jdk-17\bin\java.exe"

if not exist %JAVA17% (
    echo [ERROR] JDK 17 未找到: %JAVA17%
    echo 请先安装 JDK 17, 或修改本脚本中的 JAVA17 变量为实际路径
    pause
    exit /b 1
)

if not exist "uploads" mkdir uploads
if not exist "logs" mkdir logs

echo Starting backend with JDK 17...
start /b "" %JAVA17% -Xms512m -Xmx1024m -jar trade-platform-1.0.4.jar --spring.config.location=application-prod.yml > logs\app.log 2>&1

timeout /t 15 /nobreak > nul

netstat -an | findstr ":8081" | findstr "LISTENING" > nul
if %errorlevel% equ 0 (
    echo [OK] Backend started on port 8081
    echo.
    echo 访问 http://localhost/ 或 http://服务器IP/
    echo 默认账号: GUOHE / admin / Admin@123
) else (
    echo [FAIL] Backend did not start - check logs\app.log
    echo.
    echo 末尾日志:
    powershell -c "Get-Content logs\app.log -Tail 30"
)
pause
