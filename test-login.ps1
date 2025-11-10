# 测试登录脚本
$body = @{
    username = "teacher"
    password = "123456"
} | ConvertTo-Json

Write-Host "测试登录: teacher / 123456" -ForegroundColor Yellow

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
        -Method POST `
        -Body $body `
        -ContentType "application/json"
    
    Write-Host "`n✅ 登录成功！" -ForegroundColor Green
    Write-Host "`n返回数据：" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 3 | Write-Host
    
    if ($response.data) {
        Write-Host "`n用户信息：" -ForegroundColor Cyan
        Write-Host "  用户名: $($response.data.username)"
        Write-Host "  角色: $($response.data.role)"
        Write-Host "  用户ID: $($response.data.userId)"
        Write-Host "  真实姓名: $($response.data.realName)"
        Write-Host "  Token: $($response.data.token.Substring(0, 50))..."
    }
} catch {
    Write-Host "`n❌ 登录失败！" -ForegroundColor Red
    Write-Host "错误信息: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "响应内容: $responseBody" -ForegroundColor Yellow
    }
}













