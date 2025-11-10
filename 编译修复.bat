@echo off
chcp 65001 >nul
echo ================================
echo    修复编译错误并重新编译
echo ================================
echo.
cd backend\zszg-backend
echo 正在清理...
call mvn clean
echo.
echo 正在编译...
call mvn compile -DskipTests
echo.
if %ERRORLEVEL% EQU 0 (
    echo ✓ 编译成功！
    echo.
    echo 现在可以启动项目了：
    echo   mvn spring-boot:run
) else (
    echo ✗ 编译失败
    echo.
    echo 请查看上面的错误信息
)
echo.
echo ================================
pause

