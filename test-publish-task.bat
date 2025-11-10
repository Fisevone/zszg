@echo off
chcp 65001 > nul
echo ========================================
echo Test: Publish Task API (Task ID: 4)
echo ========================================
echo.

echo Testing POST /api/tasks/4/publish with teacher token...
echo.

curl -v -X POST http://localhost:8080/api/tasks/4/publish -H "Content-Type: application/json" -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFjaGVyIiwicm9sZSI6IlJPTEVfVEVBQ0hFUiIsImlhdCI6MTc2MTYyMTg5OCwiZXhwIjoxNzYyMjI2Njk4fQ.xMnbZOBSbYtdEKpMOJLsNOLJMjKHooFQu4rkmQrEnQc"

echo.
echo.
echo ========================================
pause













