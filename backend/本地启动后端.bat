@echo off
chcp 65001 >nul 2>&1
cd /d %~dp0

REM ============================================================
REM  外综服平台 - 本地开发启动脚本（仅限本机开发用）
REM  使用 backend/src/main/resources/application.yml 配置
REM  数据库: localhost:3306/trade_platform  端口: 8081
REM ============================================================

set JAVA17="D:\soft\java\jdk-17.0.2\bin\java.exe"

if not exist %JAVA17% (
    echo [ERROR] JDK 17 未找到: %JAVA17%
    echo 请修改本脚本 JAVA17 变量为你本机的 JDK 17 路径
    pause
    exit /b 1
)

if not exist "target\trade-platform-1.0.4.jar" (
    echo [ERROR] 找不到 jar 包: target\trade-platform-1.0.4.jar
    echo 请先执行 mvn clean package -DskipTests 打包
    pause
    exit /b 1
)

echo ============================================================
echo  正在启动后端... (端口 8081)
echo  日志输出到本窗口, 关闭窗口即停止后端
echo ============================================================
echo.

%JAVA17% -jar target\trade-platform-1.0.4.jar

pause
