@echo off
echo ========================================
echo   重新启动后端服务
echo ========================================
echo.

cd backend\zszg-backend

echo [1/3] 清理旧文件...
call mvn clean

echo.
echo [2/3] 编译并打包...
call mvn package -DskipTests

echo.
echo [3/3] 启动后端服务...
echo.
echo 正在启动，请等待...
java -jar target\zszg-backend-0.0.1-SNAPSHOT.jar

pause





