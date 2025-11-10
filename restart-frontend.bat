@echo off
chcp 65001 >nul
echo ========================================
echo 重启前端开发服务器
echo ========================================
echo.

echo [1/2] 停止旧的前端服务...
taskkill /F /IM node.exe /T >nul 2>&1
timeout /t 2 /nobreak >nul
echo ✓ 已停止

echo.
echo [2/2] 启动新的前端服务...
cd frontend\zszg-frontend
start "知错就改-前端" cmd /k "npm run dev"
echo ✓ 前端服务已在新窗口启动

echo.
echo ========================================
echo ✅ 前端服务重启完成！
echo ========================================
echo.
echo 等待10秒后刷新浏览器页面...
timeout /t 10 /nobreak
echo.
echo 现在可以刷新浏览器了！
pause



