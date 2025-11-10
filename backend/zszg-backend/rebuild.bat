@echo off
chcp 65001 >nul
echo ================================
echo    重新编译后端项目
echo ================================
echo.
echo 正在清理旧文件...
call mvn clean

echo.
echo 正在重新编译...
call mvn compile -DskipTests

echo.
echo ================================
if %ERRORLEVEL% EQU 0 (
    echo ✓ 编译成功！
) else (
    echo ✗ 编译失败，请检查错误信息
)
echo ================================
pause

