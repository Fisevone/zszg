@echo off
chcp 65001 >nul
echo ========================================
echo   启动后端服务
echo ========================================
echo.

cd /d "%~dp0backend\zszg-backend"

echo [1/2] 编译项目...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo 编译失败！
    pause
    exit /b 1
)

echo.
echo [2/2] 启动服务...
echo 后端正在启动，请等待...
echo.

start "知错就改-后端" java -jar target\zszg-backend-0.0.1-SNAPSHOT.jar

echo.
echo ========================================
echo   后端服务已启动！
echo   请等待30秒让服务完全启动
echo   然后刷新浏览器测试
echo ========================================
echo.
timeout /t 5 /nobreak >nul






















