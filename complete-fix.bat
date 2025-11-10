@echo off
chcp 65001 >nul
cls
echo.
echo ========================================
echo    彻底修复前端编译问题
echo ========================================
echo.

echo [步骤 1/5] 停止所有Node进程...
echo.
taskkill /F /IM node.exe >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ 已停止Node进程
) else (
    echo ⚠ 没有运行的Node进程
)
timeout /t 2 /nobreak >nul

echo.
echo [步骤 2/5] 删除Vite缓存...
echo.
cd frontend\zszg-frontend
if exist node_modules\.vite (
    rmdir /s /q node_modules\.vite
    echo ✓ Vite缓存已删除
) else (
    echo ⚠ 没有找到Vite缓存
)

echo.
echo [步骤 3/5] 删除临时文件...
echo.
if exist .vite (
    rmdir /s /q .vite
    echo ✓ .vite文件夹已删除
)
if exist dist (
    rmdir /s /q dist
    echo ✓ dist文件夹已删除
)

echo.
echo [步骤 4/5] 清理npm缓存...
echo.
call npm cache clean --force
echo ✓ npm缓存已清理

echo.
echo [步骤 5/5] 启动前端服务...
echo.
start "知错就改-前端" cmd /k "npm run dev"
cd ..\..

echo.
echo ========================================
echo ✅  修复完成！
echo ========================================
echo.
echo 🔄 正在等待前端服务启动（20秒）...
timeout /t 20 /nobreak

echo.
echo ========================================
echo 🎉 现在可以刷新浏览器了！
echo ========================================
echo.
echo 💡 提示：
echo    - 按F5刷新浏览器
echo    - 如果还有问题，按Ctrl+Shift+R强制刷新
echo    - 或者清除浏览器缓存后再刷新
echo.
pause


