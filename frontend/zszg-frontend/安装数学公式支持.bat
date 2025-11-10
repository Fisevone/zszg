@echo off
chcp 65001 >nul
echo ==========================================
echo  安装数学公式渲染支持 (KaTeX)
echo ==========================================
echo.

cd /d "%~dp0"

echo [1/2] 安装依赖包...
echo.
call npm install

if errorlevel 1 (
    echo.
    echo ❌ 安装失败！
    echo.
    pause
    exit /b 1
)

echo.
echo ==========================================
echo ✅ 安装成功！
echo ==========================================
echo.
echo 数学公式渲染功能已启用，支持：
echo  - 分数：$\frac{2}{5}$
echo  - 平方根：$\sqrt{2}$
echo  - 上标/下标：$x^2$, $x_1$
echo  - 希腊字母：$\alpha$, $\beta$
echo  - 积分/求和：$\int$, $\sum$
echo.
echo 接下来请：
echo 1. 如果前端正在运行，请按 Ctrl+C 停止
echo 2. 运行 npm run dev 重新启动前端
echo 3. 刷新浏览器页面即可看到公式正确渲染
echo.
pause

