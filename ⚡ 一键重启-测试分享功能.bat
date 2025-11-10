@echo off
chcp 65001 >nul
echo ========================================
echo    🌍 一键重启 - 测试分享功能
echo ========================================
echo.

echo 📍 步骤1: 停止前端进程...
echo.
taskkill /F /IM node.exe >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :5173') do (
    taskkill /F /PID %%a >nul 2>&1
)
echo ✅ 前端进程已停止
echo.

timeout /t 2 >nul

echo 📍 步骤2: 启动前端（强制5173端口）...
echo.
cd frontend\zszg-frontend

REM 清理Vite缓存
if exist node_modules\.vite (
    echo 清理Vite缓存...
    rmdir /s /q node_modules\.vite 2>nul
)

start "前端服务 - 5173端口" cmd /k "npm run dev -- --port 5173 --host 0.0.0.0"

echo.
echo ========================================
echo ✅ 前端已重启完成
echo ========================================
echo.
echo 🌐 访问地址: http://localhost:5173
echo.
echo 💡 测试步骤：
echo    1. 访问 http://localhost:5173
echo    2. 按 Ctrl+Shift+R 强制刷新
echo    3. 登录学生账号
echo    4. 进入"错题本"
echo    5. 找到错题，点击"分享到共享池"按钮
echo    6. 选择范围（班级/年级/全校）
echo    7. 添加标签（可选）
echo    8. 点击"确认分享"
echo    9. 查看是否显示"已共享"标记
echo.
echo 📚 详细说明：查看 "🌍 错题分享到共享池功能-使用说明.md"
echo.

pause






