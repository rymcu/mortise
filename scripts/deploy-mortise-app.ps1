[CmdletBinding()]
param(
    [Alias('Host')]
    [string]$DeployHost = '192.168.88.146',
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path,
    [string]$JarPath = '',
    [switch]$Build,
    [switch]$SkipSmoke
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Write-Step {
    param([string]$Message)

    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Assert-Command {
    param([string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "未找到命令: $Name"
    }
}

function Invoke-CheckedCommand {
    param(
        [scriptblock]$Script,
        [string]$ErrorMessage
    )

    & $Script
    if ($LASTEXITCODE -ne 0) {
        throw $ErrorMessage
    }
}

function Invoke-RemoteBash {
    param(
        [string]$RemoteHost,
        [string[]]$Lines
    )

    $script = (($Lines -join "`n") + "`n") -replace "`r", ""
    $encoded = [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes($script))
    & ssh "root@$($RemoteHost)" "printf '%s' '$encoded' | base64 -d | bash"
    if ($LASTEXITCODE -ne 0) {
        throw "远端命令执行失败"
    }
}

Assert-Command 'ssh'
Assert-Command 'scp'

if ([string]::IsNullOrWhiteSpace($JarPath)) {
    $JarPath = Join-Path $ProjectRoot 'mortise-app\target\mortise.jar'
}

if ($Build -or -not (Test-Path -LiteralPath $JarPath)) {
    Assert-Command 'mvn'
    Write-Step '构建 mortise-app jar'
    Push-Location $ProjectRoot
    try {
        Invoke-CheckedCommand -ErrorMessage 'mortise-app 构建失败' -Script {
            mvn -pl mortise-app -am clean package -DskipTests
        }
    }
    finally {
        Pop-Location
    }
}

if (-not (Test-Path -LiteralPath $JarPath)) {
    throw "未找到待部署 jar: $JarPath"
}

$timestamp = Get-Date -Format 'yyyyMMddHHmmss'
$backupPath = "/opt/mortise/mortise.jar.bak-$timestamp"

Write-Step '备份线上 mortise.jar'
Invoke-RemoteBash -RemoteHost $DeployHost -Lines @(
    'set -e',
    "cp /opt/mortise/mortise.jar '$backupPath'"
)

Write-Step '上传新 mortise.jar'
Invoke-CheckedCommand -ErrorMessage '上传 mortise.jar 失败' -Script {
    scp $JarPath "root@$($DeployHost):/opt/mortise/mortise.jar"
}

Write-Step '重建镜像并重启 mortise-app'
Invoke-RemoteBash -RemoteHost $DeployHost -Lines @(
    'set -e',
    'docker build -f /opt/mortise/Dockerfile.runtime -t mortise-app:latest /opt/mortise',
    'docker compose -f /opt/mortise/docker-compose.app.yaml up -d mortise-app'
)

if (-not $SkipSmoke) {
    Write-Step '校验后端健康状态'
    Invoke-RemoteBash -RemoteHost $DeployHost -Lines @(
        'set -e',
        'timeout_seconds=120',
        'interval_seconds=5',
        'until curl -fsS http://127.0.0.1:9999/mortise/actuator/health; do',
        '  timeout_seconds=$((timeout_seconds - interval_seconds))',
        '  if [ "$timeout_seconds" -le 0 ]; then',
        '    docker inspect --format "{{.State.Status}} {{if .State.Health}}{{.State.Health.Status}}{{end}}" mortise-app || true',
        '    docker logs --tail 200 mortise-app 2>&1 || true',
        '    exit 1',
        '  fi',
        '  sleep "$interval_seconds"',
        'done',
        'docker inspect --format "{{.State.Status}} {{if .State.Health}}{{.State.Health.Status}}{{end}}" mortise-app',
        'docker logs --tail 20 mortise-app 2>&1 || true'
    )
}

$rollbackCommand = @(
    "scp root@$($DeployHost):$backupPath `"$JarPath`"",
    "scp `"$JarPath`" root@$($DeployHost):/opt/mortise/mortise.jar",
    "ssh root@$($DeployHost) `"docker build -f /opt/mortise/Dockerfile.runtime -t mortise-app:latest /opt/mortise && docker compose -f /opt/mortise/docker-compose.app.yaml up -d mortise-app`""
) -join "`n"

Write-Step '回滚命令'
Write-Host $rollbackCommand

[pscustomobject]@{
    Host = $DeployHost
    BackupPath = $backupPath
    RollbackCommand = $rollbackCommand
}
