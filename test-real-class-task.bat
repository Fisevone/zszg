@echo off
chcp 65001 > nul
echo ========================================
echo Test with REAL Class
echo ========================================
echo.

echo Step 1: Login as teacher...
curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"teacher\",\"password\":\"123456\"}" > login.json
type login.json
echo.
echo.

echo Step 2: Create task for real class (高一1班)...
curl -s -X POST http://localhost:8080/api/tasks -H "Content-Type: application/json" -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFjaGVyIiwicm9sZSI6IlJPTEVfVEVBQ0hFUiIsImlhdCI6MTc2MTYyMjA1MSwiZXhwIjoxNzYyMjI2ODUxfQ.CYpdqXcpWYKIxJrVK_wn5atn2Tu-Pt97EDgkxWPHp4w" -d "{\"teacherId\":101,\"teacherName\":\"Teacher\",\"classId\":\"高一1班\",\"className\":\"高一1班\",\"title\":\"Test Task for Class\",\"content\":\"Complete homework\",\"useAI\":false}" > create-task.json
type create-task.json
echo.
echo.

echo Step 3: Publish task (assuming ID 7)...
curl -w "\nHTTP Status: %%{http_code}\n" -X POST http://localhost:8080/api/tasks/7/publish -H "Content-Type: application/json" -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFjaGVyIiwicm9sZSI6IlJPTEVfVEVBQ0hFUiIsImlhdCI6MTc2MTYyMjA1MSwiZXhwIjoxNzYyMjI2ODUxfQ.CYpdqXcpWYKIxJrVK_wn5atn2Tu-Pt97EDgkxWPHp4w"
echo.
echo.

del login.json 2>nul
del create-task.json 2>nul

echo ========================================
pause













