@echo off
echo 正在修复密码...
mysql -u root -pg54571571 --default-character-set=utf8mb4 zszg < fix-password.sql
if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   密码修复成功！
    echo ========================================
    echo.
    echo 所有账号密码现在都是：123456
    echo.
    echo 测试账号：
    echo   stu_zhangsan / 123456
    echo   teacher_zhang / 123456
    echo   admin / 123456
    echo.
    echo 现在可以重新登录了！
    echo.
) else (
    echo 密码修复失败，错误码：%errorlevel%
)
pause





