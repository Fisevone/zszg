@echo off
chcp 65001 >nul
echo.
echo ================================================
echo    🔨 编译后端代码
echo ================================================
echo.

cd /d "%~dp0backend\zszg-backend"

echo 清理旧的编译文件...
call mvn clean
echo.

echo 编译新代码（跳过测试）...
call mvn package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ 编译成功！
    echo.
    echo 💡 提示：现在可以运行"重启后端.bat"来启动服务
    echo.
) else (
    echo.
    echo ❌ 编译失败！请检查代码错误
    echo.
)

pause





















