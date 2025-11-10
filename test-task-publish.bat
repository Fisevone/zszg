@echo off
chcp 65001 > nul
echo ================================
echo 测试任务发布完整流程
echo ================================
echo.

REM 第1步：登录获取Token
echo [1/3] 登录获取Token...
for /f "tokens=*" %%a in ('curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"teacher\",\"password\":\"123456\"}"') do set LOGIN_RESPONSE=%%a
echo 登录响应: %LOGIN_RESPONSE%
echo.

REM 提取token（这里简化处理，实际项目中应该用JSON解析工具）
REM 因为批处理不好解析JSON，我们直接用完整的登录响应来手动提取

echo.
echo [2/3] 创建任务...
echo 使用Token测试创建任务接口
curl -X POST http://localhost:8080/api/tasks ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFjaGVyIiwicm9sZSI6IlJPTEVfVEVBQ0hFUiIsImlhdCI6MTc2MTYyMTg2OSwiZXhwIjoxNzYyMjI2NjY5fQ.ob3UYW6VFUTgRQ2K7M4z7rAegwRF-0ZyZfnbLe9qmzk" ^
  -d "{\"teacherId\":101,\"teacherName\":\"张老师\",\"classId\":\"高一1班\",\"className\":\"高一1班\",\"title\":\"测试任务\",\"content\":\"明天下午三点之前完成试卷订正\",\"useAI\":true}" ^
  --silent --show-error
echo.
echo.

echo [3/3] 测试发布任务接口...
echo 假设任务ID为1，测试发布接口
curl -X POST http://localhost:8080/api/tasks/1/publish ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFjaGVyIiwicm9sZSI6IlJPTEVfVEVBQ0hFUiIsImlhdCI6MTc2MTYyMTg2OSwiZXhwIjoxNzYyMjI2NjY5fQ.ob3UYW6VFUTgRQ2K7M4z7rAegwRF-0ZyZfnbLe9qmzk" ^
  --silent --show-error
echo.
echo.

echo ================================
echo 测试完成
echo ================================
pause













