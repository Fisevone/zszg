@echo off
chcp 65001 >nul
echo ================================================
echo 🚀 启动 Redis
echo ================================================
echo.

:: 检查Redis是否已经在运行
netstat -ano | findstr :6379 >nul 2>&1
if %errorlevel%==0 (
    echo ✅ Redis 已经在运行
    echo.
    redis-cli ping
    pause
    exit /b 0
)

echo ⚠️  Redis 未运行,尝试启动...
echo.

:: 尝试启动Memurai服务
net start Memurai >nul 2>&1
if %errorlevel%==0 (
    echo ✅ Memurai 服务已启动
    timeout /t 2 >nul
    redis-cli ping
    pause
    exit /b 0
)

:: 尝试WSL Redis
wsl -e sudo service redis-server status >nul 2>&1
if %errorlevel%==0 (
    wsl -e sudo service redis-server start
    if %errorlevel%==0 (
        echo ✅ WSL Redis 已启动
        timeout /t 2 >nul
        redis-cli ping
        pause
        exit /b 0
    )
)

:: 都失败了
echo.
echo ❌ 无法启动 Redis
echo.
echo 💡 解决方案:
echo    1. 运行 install-redis.bat 安装Redis
echo    2. 或者跳过Redis,系统将降级运行(速度较慢)
echo.
pause























