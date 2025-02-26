@echo off
:: 检查是否以管理员身份运行
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo 请以管理员身份运行此脚本。
    pause
    exit /b
)

:: 指定 hosts 文件路径
set hosts_path=C:\Windows\System32\drivers\etc\hosts

:: 定义需要添加的域名映射
set domain1=127.0.0.1 rymcu.local
set domain2=127.0.0.1 logto.rymcu.local
set domain3=127.0.0.1 auth.rymcu.local

:: 检查域名是否已存在
findstr /x /c:"%domain1%" %hosts_path% >nul
if %errorlevel% equ 0 (
    echo "%domain1%" 已存在，跳过添加。
) else (
    echo 正在添加 "%domain1%"...
    echo %domain1% >> %hosts_path%
)

findstr /x /c:"%domain2%" %hosts_path% >nul
if %errorlevel% equ 0 (
    echo "%domain2%" 已存在，跳过添加。
) else (
    echo 正在添加 "%domain2%"...
    echo %domain2% >> %hosts_path%
)

findstr /x /c:"%domain3%" %hosts_path% >nul
if %errorlevel% equ 0 (
    echo "%domain3%" 已存在，跳过添加。
) else (
    echo 正在添加 "%domain3%"...
    echo %domain3% >> %hosts_path%
)

echo 操作完成。
pause
