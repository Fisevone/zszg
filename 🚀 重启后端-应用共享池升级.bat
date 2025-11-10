@echo off
chcp 65001 >nul
color 0A
title 重启后端 - 应用共享池升级

echo ========================================
echo    🎉 共享池功能全面升级
echo ========================================
echo.
echo ✨ 新增功能：
echo   1️⃣  浏览次数真实统计
echo   2️⃣  点赞功能（防重复）
echo   3️⃣  收藏功能（可取消）
echo   4️⃣  AI智能推荐
echo   5️⃣  复制到错题本
echo   6️⃣  前后端状态同步
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
echo 🚀 正在启动后端（已应用新功能）...
cd backend\zszg-backend
start cmd /k "title 后端服务 - 共享池已升级 && mvn spring-boot:run"

echo.
echo ========================================
echo    ✅ 后端启动中...
echo ========================================
echo.
echo 📝 请等待30秒左右，后端会自动：
echo   - 创建新的数据库表（t_share_interaction）
echo   - 更新SharePool表（添加views字段）
echo   - 启动所有新接口
echo.
echo 🎯 启动完成后，测试这些功能：
echo   1. 进入学生端 → 共享池
echo   2. 点击"点赞"按钮
echo   3. 点击"收藏"按钮
echo   4. 点击"AI为你推荐"
echo   5. 点击"复制到错题本"
echo.
echo 📖 详细说明请查看：
echo    🎉 共享池功能全面升级-使用说明.md
echo.
pause






