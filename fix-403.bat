@echo off
chcp 65001 >nul
echo ======================================
echo   修复学生端提交错题403错误
echo ======================================
echo.
echo 停止后端服务...
taskkill /F /IM java.exe 2>nul
timeout /t 2 >nul

echo.
echo 重新编译后端...
cd backend\zszg-backend
call mvn clean compile -DskipTests
if errorlevel 1 (
    echo 编译失败！
    pause
    exit /b 1
)

echo.
echo 启动后端服务...
start "知错就改-后端" cmd /k "mvn spring-boot:run"

echo.
echo 等待后端启动（15秒）...
timeout /t 15 >nul

echo.
echo 修复完成！
echo.
echo 请执行以下步骤：
echo   1. 刷新浏览器页面（F5）
echo   2. 如果仍然出错，请先退出登录
echo   3. 重新登录学生账号
echo   4. 再次尝试提交错题
echo.
pause

















