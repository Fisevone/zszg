@echo off
chcp 65001 >nul
echo ======================================
echo 修复数据库表结构
echo ======================================
echo.

echo 📌 请确保 MySQL 服务正在运行
echo.

set /p MYSQL_USER="请输入MySQL用户名 (默认: root): "
if "%MYSQL_USER%"=="" set MYSQL_USER=root

set /p MYSQL_PASS="请输入MySQL密码: "

echo.
echo 🔧 正在修复数据库表结构...
echo.

mysql -u %MYSQL_USER% -p%MYSQL_PASS% < fix-database-schema.sql

if errorlevel 1 (
    echo.
    echo ❌ 数据库修复失败！
    echo 请检查：
    echo 1. MySQL 服务是否运行
    echo 2. 用户名和密码是否正确
    echo 3. 数据库 zszg 是否存在
    pause
    exit /b 1
)

echo.
echo ✅ 数据库表结构修复成功！
echo.
echo 📋 修复内容：
echo   ✓ 添加 t_question.type 字段
echo   ✓ 更新 content/answer/analysis 为 LONGTEXT 类型
echo.
echo 现在可以重新启动后端服务了！
echo.
pause








