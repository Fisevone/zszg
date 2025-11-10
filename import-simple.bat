@echo off
echo 正在导入数据...
cd backend\zszg-backend\src\main\resources
mysql -u root -pg54571571 --default-character-set=utf8mb4 zszg < init-data.sql
if %errorlevel% equ 0 (
    echo 数据导入成功！
) else (
    echo 数据导入失败，错误码：%errorlevel%
)
cd ..\..\..\..\..
pause





