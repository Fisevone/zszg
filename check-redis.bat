@echo off
chcp 65001 >nul
echo ================================================
echo 🔍 检查 Redis 状态
echo ================================================
echo.

:: 检查Redis是否在运行
netstat -ano | findstr :6379 >nul 2>&1
if %errorlevel%==0 (
    echo ✅ Redis 正在运行 (端口 6379)
    echo.
    
    :: 尝试ping Redis
    redis-cli ping >nul 2>&1
    if %errorlevel%==0 (
        echo ✅ Redis 连接正常
        echo.
        
        :: 显示Redis信息
        echo 📊 Redis 信息:
        redis-cli INFO server | findstr "redis_version"
        redis-cli INFO memory | findstr "used_memory_human"
        redis-cli DBSIZE
    ) else (
        echo ⚠️  Redis 正在运行,但无法连接
        echo 💡 可能需要检查密码配置
    )
) else (
    echo ❌ Redis 未运行
    echo.
    echo 💡 启动方式:
    echo    - Memurai: 服务应该自动启动,检查Windows服务
    echo    - WSL Redis: wsl -e sudo service redis-server start
    echo    - 或运行: install-redis.bat 安装Redis
)

echo.
echo ================================================
pause























