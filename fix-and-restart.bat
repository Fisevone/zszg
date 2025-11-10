@echo off
chcp 65001 >nul
echo ========================================
echo 修复并重启前端
echo ========================================
echo.

echo [1/3] 停止所有Node进程...
taskkill /F /IM node.exe /T >nul 2>&1
taskkill /F /PID 8532 /F /T >nul 2>&1
timeout /t 3 /nobreak >nul
echo ✓ 已停止

echo.
echo [2/3] 清理缓存...
cd frontend\zszg-frontend
if exist node_modules\.vite (
    rmdir /s /q node_modules\.vite
    echo ✓ Vite缓存已清理
)
cd ..\..

echo.
echo [3/3] 启动前端服务...
cd frontend\zszg-frontend
start "知错就改-前端" cmd /k "npm run dev"
cd ..\..

echo.
echo ========================================
echo ✅ 前端服务重启完成！
echo ========================================
echo.
echo 等待15秒让服务完全启动...
timeout /t 15 /nobreak
echo.
echo 现在刷新浏览器页面！
pause



