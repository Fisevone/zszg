@echo off
chcp 65001 >nul
echo ========================================
echo   知错就改系统 - 数据验证工具
echo ========================================
echo.
echo 正在验证数据...
echo.

mysql -u root -pg54571571 zszg < verify-data.sql

if errorlevel 1 (
    echo.
    echo [错误] 验证失败！请检查数据库连接
    pause
    exit /b 1
)

echo.
echo 验证完成！请查看上方的验证报告
echo.
pause





