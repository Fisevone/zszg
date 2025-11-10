@echo off
chcp 65001 > nul
echo ========================================
echo Complete Task Flow Test
echo ========================================
echo.

echo Step 1: Login...
for /f "delims=" %%i in ('curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"teacher\",\"password\":\"123456\"}"') do set LOGIN_RESP=%%i
echo %LOGIN_RESP%
echo.

echo Step 2: Create Task...
curl -s -X POST http://localhost:8080/api/tasks -H "Content-Type: application/json" -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFjaGVyIiwicm9sZSI6IlJPTEVfVEVBQ0hFUiIsImlhdCI6MTc2MTYyMTg5OCwiZXhwIjoxNzYyMjI2Njk4fQ.xMnbZOBSbYtdEKpMOJLsNOLJMjKHooFQu4rkmQrEnQc" -d "{\"teacherId\":101,\"teacherName\":\"Teacher\",\"classId\":\"Class1\",\"className\":\"Class1\",\"title\":\"Task5\",\"content\":\"Test\",\"useAI\":false}" > task-result.json
type task-result.json
echo.
echo.

echo Step 3: Publish Task ID 5...
curl -w "\nHTTP Status: %%{http_code}\n" -X POST http://localhost:8080/api/tasks/5/publish -H "Content-Type: application/json" -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFjaGVyIiwicm9sZSI6IlJPTEVfVEVBQ0hFUiIsImlhdCI6MTc2MTYyMTg5OCwiZXhwIjoxNzYyMjI2Njk4fQ.xMnbZOBSbYtdEKpMOJLsNOLJMjKHooFQu4rkmQrEnQc"
echo.
echo.

echo ========================================
del task-result.json 2>nul
pause













