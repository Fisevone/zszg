@echo off
chcp 65001 > nul
cls
echo ==========================================
echo   彻底修复403错误 - 最终版
echo ==========================================
echo.

echo [1/5] 停止所有Java进程...
taskkill /F /IM java.exe >nul 2>&1
timeout /t 3 /nobreak >nul
echo      完成！
echo.

echo [2/5] 删除旧的编译文件...
cd backend\zszg-backend
if exist target rmdir /s /q target
echo      完成！
echo.

echo [3/5] 完全重新编译...
call mvn clean install -DskipTests -q
if errorlevel 1 (
    echo      ❌ 编译失败！
    pause
    exit /b 1
)
echo      编译成功！
echo.

echo [4/5] 验证编译后的文件...
if exist "target\classes\com\zszg\security\JwtAuthenticationFilter.class" (
    echo      ✓ JWT过滤器已编译
) else (
    echo      ❌ JWT过滤器编译失败！
    pause
    exit /b 1
)
echo.

echo [5/5] 启动后端（使用编译后的新代码）...
echo.
echo ==========================================
echo   等待看到: Started ZszgBackendApplication
echo   然后去浏览器测试！
echo ==========================================
echo.
mvn spring-boot:run

