@echo off
chcp 65001 >nul
echo ========================================
echo   知错就改系统 - 数据导入工具
echo ========================================
echo.

echo [信息] 准备导入测试数据...
echo.
echo 数据包括：
echo   - 22个用户（1个管理员、5个教师、16个学生）
echo   - 60+个知识点（数学、物理、英语、化学、生物）
echo   - 29道题目（涵盖各学科，含答案解析）
echo   - 19条错题记录（真实的错误原因和纠正方法）
echo   - 13条分享池内容（已审核和待审核）
echo   - 26个学习资源（各科教学资料）
echo.
echo 默认密码：所有账号密码均为 123456
echo.

echo [警告] 此操作将清空现有数据并重新导入！
echo.
set /p confirm=确认要继续吗？(Y/N): 

if /i not "%confirm%"=="Y" (
    echo 操作已取消
    pause
    exit /b
)

echo.
echo [步骤1] 检查MySQL服务...
sc query MySQL80 | find "RUNNING" >nul
if errorlevel 1 (
    echo [错误] MySQL服务未运行！请先启动MySQL服务
    pause
    exit /b 1
)
echo [成功] MySQL服务正在运行

echo.
echo [步骤2] 导入数据到数据库...
echo 正在执行 init-data.sql...
echo.

cd backend\zszg-backend\src\main\resources

mysql -u root -pg54571571 zszg < init-data.sql

if errorlevel 1 (
    echo.
    echo [错误] 数据导入失败！
    echo 可能的原因：
    echo   1. 数据库用户名或密码不正确
    echo   2. 数据库 zszg 不存在
    echo   3. SQL文件有语法错误
    echo.
    echo 请检查 application.yml 中的数据库配置
    cd ..\..\..\..\..
    pause
    exit /b 1
)

cd ..\..\..\..\..

echo.
echo ========================================
echo   ✅ 数据导入成功！
echo ========================================
echo.
echo 测试账号：
echo   管理员：admin / 123456
echo   教师：teacher_zhang / 123456
echo   学生：stu_zhangsan / 123456
echo.
echo 更多账号和使用说明请查看：测试数据说明.md
echo.
echo 现在可以启动系统进行测试了！
echo 运行命令：start-all.bat
echo.
pause





