@echo off
chcp 65001 > nul

echo Testing create task...
curl -X POST http://localhost:8080/api/tasks -H "Content-Type: application/json" -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFjaGVyIiwicm9sZSI6IlJPTEVfVEVBQ0hFUiIsImlhdCI6MTc2MTYyMjQyMCwiZXhwIjoxNzYyMjI3MjIwfQ.9dzBa4Lt1lKEGmPQA2Ds5ou5LSXHRlBcHktA2-dAENA" -d "{\"teacherId\":101,\"teacherName\":\"Teacher\",\"classId\":\"高一1班\",\"className\":\"高一1班\",\"title\":\"Test\",\"content\":\"Test\",\"useAI\":false}"
echo.
echo.

echo Testing publish task ID 7...
curl -w "\nHTTP Status: %%{http_code}\n" -X POST http://localhost:8080/api/tasks/7/publish -H "Content-Type: application/json" -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFjaGVyIiwicm9sZSI6IlJPTEVfVEVBQ0hFUiIsImlhdCI6MTc2MTYyMjQyMCwiZXhwIjoxNzYyMjI3MjIwfQ.9dzBa4Lt1lKEGmPQA2Ds5ou5LSXHRlBcHktA2-dAENA"
echo.

pause













