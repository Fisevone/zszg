# 设置编码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   快速启动后端服务" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 获取脚本所在目录
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendPath = Join-Path $scriptPath "backend\zszg-backend"
$jarPath = Join-Path $backendPath "target\zszg-backend-0.0.1-SNAPSHOT.jar"

Write-Host "[1/3] 停止旧进程..." -ForegroundColor Yellow
Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle -eq "" } | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

Write-Host "[2/3] 检查JAR文件..." -ForegroundColor Yellow
if (-not (Test-Path $jarPath)) {
    Write-Host "JAR文件不存在，正在编译..." -ForegroundColor Yellow
    Set-Location $backendPath
    mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Host "编译失败！" -ForegroundColor Red
        pause
        exit 1
    }
    Set-Location $scriptPath
}

Write-Host "[3/3] 启动后端服务..." -ForegroundColor Green
Write-Host "后端正在启动，请等待30秒..." -ForegroundColor Yellow
Write-Host "日志将输出到 backend-log.txt" -ForegroundColor Gray
Write-Host ""

# 启动后端
$logPath = Join-Path $scriptPath "backend-log.txt"
Start-Process -FilePath "java" -ArgumentList "-jar", "`"$jarPath`"" -RedirectStandardOutput $logPath -NoNewWindow

Write-Host "后端服务已启动！" -ForegroundColor Green
Write-Host "请等待30秒让服务完全启动..." -ForegroundColor Yellow
Write-Host ""
Write-Host "现在可以刷新浏览器测试了！" -ForegroundColor Cyan
Write-Host ""






















