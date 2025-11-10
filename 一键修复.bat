@echo off
chcp 65001 >nul
color 0A
title 一键修复 - 操作失败问题

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                                                              ║
echo ║          🚑 一键修复 - 操作失败问题                          ║
echo ║                                                              ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

echo 📋 问题说明：
echo    - 后端返回的用户信息不完整
echo    - API调用方式错误
echo    - 需要重启后端和清除缓存
echo.
echo ════════════════════════════════════════════════════════════════
echo.

echo [步骤1/4] 停止旧的后端进程...
echo.

:: 查找并停止Java进程
for /f "tokens=2" %%a in ('tasklist ^| findstr "java.exe"') do (
    echo 找到Java进程: %%a
    taskkill /F /PID %%a >nul 2>&1
)

echo ✅ 已停止旧进程
timeout /t 2 >nul

echo.
echo ════════════════════════════════════════════════════════════════
echo.
echo [步骤2/4] 清理后端编译缓存...
echo.

cd backend\zszg-backend
if exist target (
    echo 清理 target 目录...
    rd /s /q target >nul 2>&1
)

echo ✅ 缓存已清理
timeout /t 1 >nul

echo.
echo ════════════════════════════════════════════════════════════════
echo.
echo [步骤3/4] 重新编译后端...
echo.

call mvnw.cmd clean compile -DskipTests -q

if %ERRORLEVEL% NEQ 0 (
    echo ❌ 编译失败！请检查错误信息
    pause
    exit /b 1
)

echo ✅ 编译成功
timeout /t 1 >nul

echo.
echo ════════════════════════════════════════════════════════════════
echo.
echo [步骤4/4] 启动后端服务...
echo.

echo 正在启动，请等待...
echo.
echo ⚠️  后端将在新窗口中启动
echo ⚠️  请不要关闭那个窗口！
echo.

start "知错就改-后端服务" cmd /k "cd /d %cd% && mvnw.cmd spring-boot:run"

timeout /t 3 >nul

echo ✅ 后端已启动（请查看新窗口）
echo.

cd ..\..

echo ════════════════════════════════════════════════════════════════
echo.
echo 🎉 自动修复完成！
echo.
echo ════════════════════════════════════════════════════════════════
echo.
echo 📋 后续操作：
echo.
echo 1. ✅ 等待后端完全启动（约30秒）
echo    → 新窗口中看到 "Started ZszgBackendApplication"
echo.
echo 2. 🌐 打开浏览器：http://localhost:5173
echo.
echo 3. 🔧 按 F12 打开开发者工具
echo    → 在 Console 中输入：localStorage.clear()
echo    → 按回车后刷新页面（F5）
echo.
echo 4. 🔑 重新登录：
echo    → 用户名：teacher
echo    → 密码：123456
echo.
echo 5. ✨ 测试功能是否正常
echo.
echo ════════════════════════════════════════════════════════════════
echo.
echo 💡 提示：
echo    - 如果前端未启动，请在新CMD中运行：
echo      cd frontend\zszg-frontend
echo      npm run dev
echo.
echo    - 如果还有问题，请查看：
echo      📄 🚑 紧急修复-操作失败问题.md
echo.
echo ════════════════════════════════════════════════════════════════
echo.

pause

