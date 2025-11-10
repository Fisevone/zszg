@echo off
chcp 65001 >nul
color 0A
title 修复复制功能 - 重启后端

echo ========================================
echo    🔧 修复"复制到错题本"功能
echo ========================================
echo.
echo 🐛 修复内容：
echo   - 解决JSON字段格式错误
echo   - 空字符串自动转换为有效JSON
echo   - 前后端双重保险
echo.
echo ========================================

REM 停止现有的后端进程
echo.
echo 🔄 正在停止旧的后端进程...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do (
    echo    - 发现进程 %%a，正在停止...
    taskkill /F /PID %%a >nul 2>&1
)
timeout /t 2 /nobreak >nul

REM 启动新的后端
echo.
echo 🚀 正在启动后端（已应用修复）...
cd backend\zszg-backend
start cmd /k "title 后端服务 - 复制功能已修复 && mvn spring-boot:run"

echo.
echo ========================================
echo    ✅ 后端启动中...
echo ========================================
echo.
echo 📝 请等待30秒左右，后端启动完成后：
echo.
echo 🎯 测试步骤：
echo   1. 打开 http://localhost:5173
echo   2. 登录学生端（student_li / password123）
echo   3. 点击左侧"共享池"
echo   4. 找到任意错题，点击"复制到错题本"
echo   5. 观察：提示"已复制到你的错题本！"
echo   6. 进入"错题本"，能看到复制的错题
echo.
echo 📖 详细说明请查看：
echo    ✅ 复制到错题本功能-已修复.md
echo.
pause






