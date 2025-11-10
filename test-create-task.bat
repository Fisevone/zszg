@echo off
chcp 65001 > nul
echo ========================================
echo Test: Create Task API
echo ========================================
echo.

echo Testing POST /api/tasks with teacher token...
echo.

curl -X POST http://localhost:8080/api/tasks -H "Content-Type: application/json" -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFjaGVyIiwicm9sZSI6IlJPTEVfVEVBQ0hFUiIsImlhdCI6MTc2MTYyMTg5OCwiZXhwIjoxNzYyMjI2Njk4fQ.xMnbZOBSbYtdEKpMOJLsNOLJMjKHooFQu4rkmQrEnQc" -d "{\"teacherId\":101,\"teacherName\":\"ZhangTeacher\",\"classId\":\"Class1\",\"className\":\"Class1\",\"title\":\"Test Task\",\"content\":\"Complete homework\",\"useAI\":true}"

echo.
echo.
echo ========================================
pause













