@echo off
chcp 65001 >nul
echo.
echo ================================================
echo    🚀 重启后端服务
echo ================================================
echo.

REM 停止现有Java进程
echo [1/2] 停止现有后端服务...
taskkill /F /IM java.exe 2>nul
timeout /t 2 /nobreak >nul
echo ✅ 已停止
echo.

REM 启动后端
echo [2/2] 启动后端服务...
cd /d "%~dp0backend\zszg-backend"
start "知错就改后端" java -jar target\zszg-backend-0.0.1-SNAPSHOT.jar

echo.
echo ✅ 后端服务已启动！
echo.
echo 💡 提示：
echo    • 后端启动需要10-15秒
echo    • 请等待看到 "Started ZszgBackendApplication" 字样
echo    • 访问地址：http://localhost:8080
echo.
pause





















