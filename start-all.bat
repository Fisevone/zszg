@echo off
chcp 65001
echo ========================================
echo 启动知错就改完整项目
echo ========================================
echo.

echo [1/4] 检查MySQL服务...
sc query MySQL80 | find "RUNNING" >nul
if errorlevel 1 (
    echo [错误] MySQL服务未运行，请先启动MySQL！
    pause
    exit /b 1
)
echo [成功] MySQL服务正在运行

echo.
echo [2/4] 检查Redis服务...
netstat -ano | findstr :6379 >nul 2>&1
if errorlevel 1 (
    echo [警告] Redis未运行！
    echo.
    echo 💡 Redis用于缓存AI结果，大幅提升性能
    echo 💡 运行 install-redis.bat 安装Redis
    echo 💡 或运行 start-redis.bat 启动Redis
    echo.
    echo ⚠️  系统将在无缓存模式下运行（性能较差）
    echo.
    choice /C YN /M "是否继续启动"
    if errorlevel 2 (
        exit /b 1
    )
) else (
    echo [成功] Redis服务正在运行
)

echo.
echo [3/4] 启动后端服务...
cd /d "%~dp0backend\zszg-backend"
start "知错就改-后端" cmd /k "mvn spring-boot:run"

echo 等待后端启动（20秒）...
timeout /t 20 /nobreak >nul

echo.
echo [4/4] 启动前端服务...
cd /d "%~dp0frontend\zszg-frontend"
start "知错就改-前端" cmd /k "npm run dev"

echo.
echo ========================================
echo 启动完成！
echo ========================================
echo 后端地址: http://localhost:8080
echo 前端地址: http://localhost:5173
echo.
echo 系统状态:
echo   ✅ MySQL: 运行中
netstat -ano | findstr :6379 >nul 2>&1
if errorlevel 1 (
    echo   ⚠️  Redis: 未运行 (性能降低)
) else (
    echo   ✅ Redis: 运行中 (缓存加速)
)
echo.
echo 注意：请等待两个窗口都显示启动成功后再访问
echo ========================================
pause


