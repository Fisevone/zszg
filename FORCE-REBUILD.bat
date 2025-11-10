@echo off
chcp 65001 > nul
cls
echo =============================================
echo   强制重新编译并启动（解决403问题）
echo =============================================
echo.

cd backend\zszg-backend

echo [1/4] 清理旧的编译文件...
rmdir /s /q target 2>nul
echo      完成！
echo.

echo [2/4] 重新编译（这次一定会用新代码）...
call mvn clean compile -DskipTests -X
if errorlevel 1 (
    echo.
    echo ❌ 编译失败！
    pause
    exit /b 1
)
echo      编译成功！
echo.

echo [3/4] 打包项目...
call mvn package -DskipTests
echo      打包完成！
echo.

echo [4/4] 启动后端...
echo.
echo =========================================
echo   等待看到: Started ZszgBackendApplication
echo =========================================
echo.
mvn spring-boot:run

















