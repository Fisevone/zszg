@echo off
chcp 65001
echo ========================================
echo 编译并启动知错就改后端
echo ========================================
echo.

echo [步骤1/3] 清理旧的编译文件...
call mvn clean -q
if errorlevel 1 (
    echo [错误] 清理失败！
    pause
    exit /b 1
)
echo [成功] 清理完成

echo.
echo [步骤2/3] 编译项目...
call mvn compile -DskipTests
if errorlevel 1 (
    echo [错误] 编译失败！请检查上面的错误信息
    pause
    exit /b 1
)
echo [成功] 编译完成

echo.
echo [步骤3/3] 启动应用...
echo.
call mvn spring-boot:run

pause


