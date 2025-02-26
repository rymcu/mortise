@echo off
setlocal enabledelayedexpansion

:: Check if running as administrator
NET SESSION >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    echo Please run this script as Administrator.
    pause
    exit /b 1
)
set "LOCAL_IP="
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr "IPv4"') do (
    set ip=%%a
    set ip=!ip: =!
    echo !ip! | findstr "^192.168." >nul
    if !errorlevel! equ 0 (
        set "LOCAL_IP=!ip!"
        rem Only the first IP found is taken
        goto :FOUND_IP
    )
)

:FOUND_IP
if not defined LOCAL_IP (
    echo No IP address in the 192.168.* range found.
    pause
    exit /b 1
)

echo Using IP address: !LOCAL_IP!

:: Set hosts file path
set "HOSTS_PATH=%SystemRoot%\System32\drivers\etc\hosts"

:: Define domain mappings to add
for %%D in (
    "rymcu.local"
    "logto.rymcu.local"
    "auth.rymcu.local"
    "npm.rymcu.local"
) do (
    set "domain_entry=!LOCAL_IP! %%~D"

    :: Check if domain entry already exists in hosts file
    findstr /X /C:"!domain_entry!" "%HOSTS_PATH%" >nul
    if errorlevel 1 (
        echo Adding "!domain_entry!"...
        >> "%HOSTS_PATH%" echo !domain_entry!
    ) else (
        echo "!domain_entry!" already exists, skipping.
    )
)

echo Operation completed.
