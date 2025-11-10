@echo off
chcp 65001 >nul
echo ======================================
echo 创建班级管理相关表
echo ======================================
echo.

echo 正在连接数据库...
mysql -u root -pg54571571 < update-classroom-tables.sql

if %errorlevel% equ 0 (
    echo.
    echo ✅ 表创建成功！
    echo.
) else (
    echo.
    echo ❌ 表创建失败，请检查：
    echo 1. MySQL是否正在运行
    echo 2. 用户名密码是否正确（当前：root/123456）
    echo 3. 数据库zszg_db是否存在
    echo.
)

pause

