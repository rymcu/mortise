[CmdletBinding()]
param(
    [Alias('Host')]
    [string]$DeployHost = '192.168.88.146',
    [ValidateSet('standalone-root', 'site-community')]
    [string]$SiteMode = 'standalone-root'
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

function Assert-Http200 {
    param(
        [string]$Uri,
        [hashtable]$Headers = @{}
    )

    $response = Invoke-WebRequest -Uri $Uri -Headers $Headers -SkipHttpErrorCheck
    if ($response.StatusCode -ne 200) {
        throw "接口校验失败: $Uri => $($response.StatusCode)"
    }
    Write-Host "$Uri => 200"
}

Assert-Command 'ssh'

$sitePaths = if ($SiteMode -eq 'standalone-root') {
    @('/topics', '/collections')
} else {
    @('/community', '/community/collections')
}

Write-Step '校验后端健康状态'
Invoke-RemoteBash -RemoteHost $DeployHost -Lines @(
    'set -e',
    'curl -fsS http://127.0.0.1:9999/mortise/actuator/health'
)

Write-Step '校验前台业务路由'
foreach ($path in $sitePaths) {
    Assert-Http200 -Uri "http://$($DeployHost)$path"
}

Write-Step '检查容器状态与最近日志'
Invoke-RemoteBash -RemoteHost $DeployHost -Lines @(
    'set -e',
    'for name in mortise-site mortise-app; do',
    '  running=$(docker inspect -f "{{.State.Running}}" "$name")',
    '  status=$(docker inspect -f "{{.State.Status}}" "$name")',
    '  restart_count=$(docker inspect -f "{{.RestartCount}}" "$name")',
    '  echo "$name status=$status restartCount=$restart_count"',
    '  if [ "$running" != "true" ]; then',
    '    echo "$name 未处于运行态" >&2',
    '    exit 1',
    '  fi',
    'done',
    'site_logs=$(docker logs --tail 80 mortise-site 2>&1 || true)',
    'app_logs=$(docker logs --tail 80 mortise-app 2>&1 || true)',
    'echo "$site_logs"',
    'printf "\n---APP---\n"',
    'echo "$app_logs"',
    'printf "%s\n%s\n" "$site_logs" "$app_logs" | grep -E "ERR_DLOPEN_FAILED|better_sqlite3\.node|Module did not self-register" >/dev/null && exit 1 || true'
)

$headers = @{}
if ($env:MORTISE_ADMIN_BEARER_TOKEN) {
    $token = $env:MORTISE_ADMIN_BEARER_TOKEN.Trim()
    if ($token -notmatch '^(?i)Bearer ') {
        $token = "Bearer $token"
    }
    $headers.Authorization = $token
} elseif ($env:MORTISE_ADMIN_COOKIE) {
    $headers.Cookie = $env:MORTISE_ADMIN_COOKIE.Trim()
}

if ($headers.Count -eq 0) {
    Write-Step '未提供管理端认证信息，跳过 dashboard 接口校验'
    Write-Host '如需校验管理端接口，请设置环境变量 MORTISE_ADMIN_BEARER_TOKEN 或 MORTISE_ADMIN_COOKIE'
    return
}

Write-Step '校验管理端社区看板接口'
@(
    "http://$($DeployHost)/mortise/api/v1/admin/community/dashboard/overview",
    "http://$($DeployHost)/mortise/api/v1/admin/community/dashboard/trends",
    "http://$($DeployHost)/mortise/api/v1/admin/community/dashboard/pending"
) | ForEach-Object {
    Assert-Http200 -Uri $_ -Headers $headers
}
