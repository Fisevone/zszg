@echo off
chcp 65001 > nul
echo ========================================
echo Test: Get Current User Info
echo ========================================
echo.

echo Testing GET /api/auth/me with teacher token...
echo.

curl -v -X GET http://localhost:8080/api/auth/me -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFjaGVyIiwicm9sZSI6IlJPTEVfVEVBQ0hFUiIsImlhdCI6MTc2MTYyMTg5OCwiZXhwIjoxNzYyMjI2Njk4fQ.xMnbZOBSbYtdEKpMOJLsNOLJMjKHooFQu4rkmQrEnQc"

echo.
echo.
echo ========================================
pause













