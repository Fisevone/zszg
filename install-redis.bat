@echo off
chcp 65001 >nul
echo ================================================
echo 📦 Redis 安装脚本 for Windows
echo ================================================
echo.

echo ℹ️  Redis是一个高性能的内存数据库
echo ℹ️  用于缓存AI分析结果，大幅提升系统性能
echo.

echo 💡 安装方式选择：
echo.
echo [1] 使用 Memurai (Redis for Windows 推荐)
echo [2] 使用 WSL + Redis (需要WSL环境)
echo [3] 跳过Redis安装 (系统将降级运行,但速度较慢)
echo.
set /p choice="请选择 (1/2/3): "

if "%choice%"=="1" goto install_memurai
if "%choice%"=="2" goto install_wsl_redis
if "%choice%"=="3" goto skip_redis

:install_memurai
echo.
echo ================================================
echo 📥 方案1: 安装 Memurai (推荐)
echo ================================================
echo.
echo Memurai是专为Windows优化的Redis兼容版本
echo.
echo 请按照以下步骤手动安装:
echo.
echo 1. 访问: https://www.memurai.com/get-memurai
echo 2. 下载 Memurai Developer Edition (免费)
echo 3. 运行安装程序,一路Next即可
echo 4. 安装完成后,Memurai会自动作为Windows服务启动
echo 5. 默认端口: 6379 (无需修改配置)
echo.
echo 💡 安装完成后,重新运行 start-all.bat 即可
echo.
pause
goto end

:install_wsl_redis
echo.
echo ================================================
echo 📥 方案2: WSL + Redis
echo ================================================
echo.
echo 此方案需要先安装WSL (Windows Subsystem for Linux)
echo.
echo 步骤:
echo 1. 以管理员权限运行PowerShell
echo 2. 执行: wsl --install
echo 3. 重启电脑
echo 4. 进入WSL终端
echo 5. 执行: sudo apt update
echo 6. 执行: sudo apt install redis-server -y
echo 7. 启动Redis: sudo service redis-server start
echo.
echo 💡 安装完成后,重新运行 start-all.bat 即可
echo.
pause
goto end

:skip_redis
echo.
echo ================================================
echo ⚠️  跳过Redis安装
echo ================================================
echo.
echo 系统将在没有缓存的情况下运行
echo 影响:
echo   ❌ AI分析速度较慢 (每次都需要调用API)
echo   ❌ API调用次数增加 (可能产生更多费用)
echo   ❌ 系统并发能力下降
echo.
echo 建议: 生产环境请务必安装Redis
echo.
echo 💡 您可以稍后随时运行此脚本安装Redis
echo.
pause
goto end

:end
echo.
echo ================================================
echo ✅ 完成
echo ================================================
echo.
echo 下一步:
echo   - 如果已安装Redis,运行: start-all.bat
echo   - 测试Redis连接: redis-cli ping (应返回PONG)
echo.
pause























