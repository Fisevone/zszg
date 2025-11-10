@echo off
chcp 65001 >nul
cls
echo ========================================
echo    启动后端服务
echo ========================================
echo.

echo [1/3] 停止旧进程...
taskkill /F /IM java.exe 2>nul
timeout /t 2 /nobreak >nul

echo [2/3] 编译项目...
cd /d "%~dp0backend\zszg-backend"
if not exist "pom.xml" (
    echo 错误：找不到 pom.xml 文件！
    pause
    exit /b 1
)

call mvn clean package -DskipTests
if errorlevel 1 (
    echo.
    echo 编译失败！请检查错误信息。
    echo.
    pause
    exit /b 1
)

echo.
echo [3/3] 启动服务...
if not exist "target\zszg-backend-0.0.1-SNAPSHOT.jar" (
    echo 错误：找不到编译后的jar文件！
    pause
    exit /b 1
)

echo.
echo ========================================
echo    后端服务正在启动...
echo    请不要关闭此窗口
echo ========================================
echo.

java -jar target\zszg-backend-0.0.1-SNAPSHOT.jar

pause






















