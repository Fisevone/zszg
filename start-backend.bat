
@echo off
chcp 65001
echo ========================================
echo 启动知错就改后端服务
echo ========================================
echo.

echo 切换到后端目录...
cd /d "%~dp0backend\zszg-backend"

echo.
echo 检查MySQL服务...
sc query MySQL80 | find "RUNNING" >nul
if errorlevel 1 (
    echo [错误] MySQL服务未运行，请先启动MySQL！
    pause
    exit /b 1
)
echo [成功] MySQL服务正在运行

echo.
echo 启动Spring Boot应用...
echo 提示：首次启动可能需要下载依赖，请耐心等待
echo.
mvn spring-boot:run

pause


