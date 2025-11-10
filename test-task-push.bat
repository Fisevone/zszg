@echo off
chcp 65001 >nul
echo ======================================
echo 测试教师任务推送功能
echo ======================================
echo.

echo 📌 此脚本将：
echo   1. 检查现有数据
echo   2. 修复学生班级信息
echo   3. 创建测试任务
echo   4. 为学生分配任务
echo.

set /p MYSQL_USER="请输入MySQL用户名 (默认: root): "
if "%MYSQL_USER%"=="" set MYSQL_USER=root

set /p MYSQL_PASS="请输入MySQL密码: "

echo.
echo 🔧 正在执行测试脚本...
echo.

mysql -u %MYSQL_USER% -p%MYSQL_PASS% < test-task-push.sql

if errorlevel 1 (
    echo.
    echo ❌ 测试失败！
    pause
    exit /b 1
)

echo.
echo ✅ 测试任务创建成功！
echo.
echo 📋 接下来请：
echo   1. 刷新学生端浏览器（Ctrl + Shift + R）
echo   2. 点击"教师推送"菜单
echo   3. 应该能看到"【测试任务】第三章错题整理"
echo.
echo 💡 如果还是看不到，请截图控制台Console的输出
echo.
pause








