@echo off
chcp 65001 > nul
echo ========================================
echo Test: Publish Task ID 6
echo ========================================
echo.

echo Testing POST /api/tasks/6/publish...
curl -w "\nHTTP Status: %%{http_code}\n" -X POST http://localhost:8080/api/tasks/6/publish -H "Content-Type: application/json" -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFjaGVyIiwicm9sZSI6IlJPTEVfVEVBQ0hFUiIsImlhdCI6MTc2MTYyMjA1MSwiZXhwIjoxNzYyMjI2ODUxfQ.CYpdqXcpWYKIxJrVK_wn5atn2Tu-Pt97EDgkxWPHp4w"
echo.
echo.

echo ========================================
pause













