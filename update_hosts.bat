@echo off
setlocal enabledelayedexpansion

:: Check if running as administrator
NET SESSION >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    echo Please run this script as Administrator.
    pause
    exit /b 1
)

:: Set hosts file path
set "HOSTS_PATH=%SystemRoot%\System32\drivers\etc\hosts"

:: Define domain mappings to add
for %%D in (
    "127.0.0.1 rymcu.local"
    "127.0.0.1 logto.rymcu.local"
    "127.0.0.1 auth.rymcu.local"
    "127.0.0.1 npm.rymcu.local"
) do (
    set "domain_entry=%%~D"

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
