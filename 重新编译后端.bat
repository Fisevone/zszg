@echo off
chcp 65001 >nul
echo ==========================================
echo 重新编译后端
echo ==========================================
echo.

cd /d "%~dp0backend\zszg-backend"

echo [1/2] 清理旧的编译文件...
call mvnw.cmd clean -DskipTests
if errorlevel 1 (
    echo ❌ 清理失败！
    pause
    exit /b 1
)

echo.
echo [2/2] 重新编译...
call mvnw.cmd compile -DskipTests
if errorlevel 1 (
    echo ❌ 编译失败！
    pause
    exit /b 1
)

echo.
echo ==========================================
echo ✅ 编译成功！
echo ==========================================
echo.
echo 接下来请：
echo 1. 停止旧的后端（如果正在运行）
echo 2. 运行"启动后端.bat"重新启动
echo 3. 清除浏览器缓存：F12 - Console - localStorage.clear()
echo 4. 刷新页面 F5 并重新登录
echo.
pause

