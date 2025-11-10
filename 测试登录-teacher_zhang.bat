@echo off
chcp 65001 > nul
echo ================================
echo 测试登录 - teacher_zhang
echo ================================
echo.

echo [1/2] 检查后端是否启动...
timeout /t 5 /nobreak > nul
curl -s http://localhost:8080/actuator/health
if errorlevel 1 (
    echo.
    echo ❌ 后端未启动！请先运行 start-backend.bat
    pause
    exit /b 1
)
echo.
echo ✅ 后端已启动
echo.

echo [2/2] 测试登录...
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"teacher_zhang\",\"password\":\"123456\"}"

echo.
echo.
echo ================================
echo 测试完成
echo ================================
pause













