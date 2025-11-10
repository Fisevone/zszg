@echo off
echo Compiling and starting backend...
call mvn clean compile -DskipTests
if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
)
echo Starting server...
mvn spring-boot:run

















