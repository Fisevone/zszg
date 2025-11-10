@echo off
chcp 65001 > nul
echo ================================
echo 测试 teacher 账号登录
echo ================================
echo.

curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"teacher\",\"password\":\"123456\"}" ^
  --silent --show-error

echo.
echo.
echo ================================
pause













