@echo off
chcp 65001 >nul
echo 正在插入测试数据...
mysql -u root -pg54571571 zszg < insert-test-data.sql
if %errorlevel% equ 0 (
    echo.
    echo ✅ 测试数据插入成功！
    echo.
    echo 查看插入的数据：
    mysql -u root -pg54571571 zszg -e "SELECT COUNT(*) AS '学生数' FROM t_user WHERE role='ROLE_STUDENT';"
    mysql -u root -pg54571571 zszg -e "SELECT COUNT(*) AS '题目数' FROM t_question;"
    mysql -u root -pg54571571 zszg -e "SELECT COUNT(*) AS '错题数' FROM t_error_book;"
    mysql -u root -pg54571571 zszg -e "SELECT COUNT(*) AS '待审核共享' FROM t_share_pool WHERE status='PENDING';"
) else (
    echo ❌ 插入失败，请检查错误信息
)
pause



