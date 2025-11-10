@echo off
chcp 65001 > nul
cls
echo ========================================
echo    修复403错误 - 最终解决方案
echo ========================================
echo.
echo [1/3] 停止旧进程...
taskkill /F /IM java.exe >nul 2>&1
timeout /t 2 /nobreak >nul

echo [2/3] 重新编译后端（使用修复后的代码）...
cd backend\zszg-backend
call mvn clean compile -DskipTests
if errorlevel 1 (
    echo.
    echo ❌ 编译失败！请检查错误信息。
    pause
    exit /b 1
)

echo.
echo [3/3] 启动后端服务...
echo.
echo ========================================
echo  后端正在启动，请等待...
echo  看到 "Started ZszgBackendApplication" 
echo  就说明启动成功了！
echo ========================================
echo.
mvn spring-boot:run

















