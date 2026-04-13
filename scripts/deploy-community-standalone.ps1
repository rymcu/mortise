[CmdletBinding()]
param(
    [Alias('Host')]
    [string]$DeployHost = '192.168.88.146',
    [ValidateSet('standalone-root', 'site-community')]
    [string]$SiteMode = 'standalone-root',
    [switch]$SkipBackend,
    [switch]$SkipSite,
    [switch]$SkipSmoke,
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path,
    [string]$StandaloneAppName = 'community-standalone-deploy',
    [string]$BetterSqlite3Version = '12.6.2'
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

function New-StandaloneCommunityApp {
    param(
        [string]$FrontendRootPath,
        [string]$AppName
    )

    $templatePath = Join-Path $FrontendRootPath 'templates\standalone'
    $appPath = Join-Path $FrontendRootPath "apps\$AppName"
    if (Test-Path -LiteralPath $appPath) {
        Remove-Item -LiteralPath $appPath -Recurse -Force
    }

    Copy-Item -LiteralPath $templatePath -Destination $appPath -Recurse -Force

    $packagePath = Join-Path $appPath 'package.json'
    $packageJson = Get-Content -LiteralPath $packagePath -Raw | ConvertFrom-Json -AsHashtable
    $packageJson.name = "@mortise/$AppName"
    $packageJson.dependencies['@mortise/community-layer'] = 'workspace:*'
    [System.IO.File]::WriteAllText(
        $packagePath,
        (($packageJson | ConvertTo-Json -Depth 20) + "`n"),
        [System.Text.UTF8Encoding]::new($false)
    )

    $appConfigPath = Join-Path $appPath 'app\app.config.ts'
    $appConfigContent = @"
/**
 * 社区 standalone 部署配置
 */
export default defineAppConfig({
  community: {
    basePath: ''
  },
  ui: {
    colors: {
      primary: 'green',
      neutral: 'zinc'
    }
  }
})
"@
    [System.IO.File]::WriteAllText(
        $appConfigPath,
        $appConfigContent.TrimStart() + "`n",
        [System.Text.UTF8Encoding]::new($false)
    )

    return $appPath
}

function Build-SiteArtifact {
    param(
        [string]$FrontendRootPath,
        [string]$Mode,
        [string]$AppName
    )

    $oldNodeOptions = $env:NODE_OPTIONS
    $env:NODE_OPTIONS = '--max-old-space-size=8192'
    try {
        Push-Location $FrontendRootPath
        try {
            if ($Mode -eq 'standalone-root') {
                $appPath = New-StandaloneCommunityApp -FrontendRootPath $FrontendRootPath -AppName $AppName
                Write-Step '安装 standalone 社区发布工位依赖'
                Invoke-CheckedCommand -ErrorMessage 'standalone 社区依赖安装失败' -Script {
                    pnpm install --filter "@mortise/$AppName"
                }

                Write-Step '构建 standalone 社区前端'
                Invoke-CheckedCommand -ErrorMessage 'standalone 社区前端构建失败' -Script {
                    pnpm --filter "@mortise/$AppName" build
                }

                return [pscustomobject]@{
                    OutputPath = Join-Path $appPath '.output'
                    TempAppPath = $appPath
                }
            }

            Write-Step '构建 site-community 前端'
            Invoke-CheckedCommand -ErrorMessage 'site 前端构建失败' -Script {
                pnpm --filter @mortise/site build
            }

            return [pscustomobject]@{
                OutputPath = Join-Path $FrontendRootPath 'apps\site\.output'
                TempAppPath = $null
            }
        }
        finally {
            Pop-Location
        }
    }
    finally {
        $env:NODE_OPTIONS = $oldNodeOptions
    }
}

Assert-Command 'ssh'
Assert-Command 'tar'
Assert-Command 'pnpm'

if ($SkipBackend -and $SkipSite) {
    throw 'SkipBackend 和 SkipSite 不能同时为 true'
}

$frontendRoot = Join-Path $ProjectRoot 'frontend'
$lockPath = Join-Path $frontendRoot 'pnpm-lock.yaml'
$originalLockContent = if (Test-Path -LiteralPath $lockPath) {
    [System.IO.File]::ReadAllText($lockPath)
} else {
    $null
}
$temporaryAppPath = Join-Path $frontendRoot "apps\$StandaloneAppName"
$siteBackupPath = $null
$siteArtifact = $null

try {
    if (-not $SkipSite) {
        $siteArtifact = Build-SiteArtifact -FrontendRootPath $frontendRoot -Mode $SiteMode -AppName $StandaloneAppName

        if (-not (Test-Path -LiteralPath $siteArtifact.OutputPath)) {
            throw "未找到待部署站点产物: $($siteArtifact.OutputPath)"
        }
    }

    if (-not $SkipBackend) {
        Write-Step '部署 mortise-app'
        & (Join-Path $PSScriptRoot 'deploy-mortise-app.ps1') -Host $DeployHost -Build -SkipSmoke
        if ($LASTEXITCODE -ne 0) {
            throw 'mortise-app 部署失败'
        }
    }

    if (-not $SkipSite) {
        $timestamp = Get-Date -Format 'yyyyMMddHHmmss'
        $siteBackupPath = "/opt/mortise/frontend/site/.output.bak-$timestamp"
        $artifactParent = Split-Path -Path $siteArtifact.OutputPath -Parent

        Write-Step '备份远端 site/.output'
        Invoke-RemoteBash -RemoteHost $DeployHost -Lines @(
            'set -e',
            "if [ -d /opt/mortise/frontend/site/.output ]; then mv /opt/mortise/frontend/site/.output '$siteBackupPath'; fi"
        )

        Write-Step '上传新站点产物到远端'
        Invoke-CheckedCommand -ErrorMessage '站点产物上传失败' -Script {
            tar -C $artifactParent -cf - .output | ssh "root@$($DeployHost)" 'cd /opt/mortise/frontend/site && tar -xf -'
        }

        Write-Step '修复远端 better-sqlite3 原生依赖'
        Invoke-RemoteBash -RemoteHost $DeployHost -Lines @(
            'set -e',
            "docker run --rm -v /opt/mortise/frontend/site/.output/server:/work node:22-slim bash -lc ""apt-get update >/dev/null && apt-get install -y python3 make g++ >/dev/null && mkdir -p /tmp/sqlitefix && cd /tmp/sqlitefix && npm init -y >/dev/null && npm install better-sqlite3@$BetterSqlite3Version >/dev/null && rm -rf /work/node_modules/better-sqlite3 && cp -a /tmp/sqlitefix/node_modules/better-sqlite3 /work/node_modules/"""
        )

        Write-Step '重建镜像并重启 mortise-site'
        Invoke-RemoteBash -RemoteHost $DeployHost -Lines @(
            'set -e',
            'docker build -f /opt/mortise/frontend/Dockerfile.site -t mortise-site:latest /opt/mortise/frontend',
            'docker compose -f /opt/mortise/docker-compose.app.yaml up -d mortise-site'
        )

        $rollbackCommand = @(
            "ssh root@$($DeployHost) `"rm -rf /opt/mortise/frontend/site/.output && mv $siteBackupPath /opt/mortise/frontend/site/.output && docker build -f /opt/mortise/frontend/Dockerfile.site -t mortise-site:latest /opt/mortise/frontend && docker compose -f /opt/mortise/docker-compose.app.yaml up -d mortise-site`""
        ) -join "`n"

        Write-Step '站点回滚命令'
        Write-Host $rollbackCommand
    }

    if (-not $SkipSmoke) {
        Write-Step '执行社区 smoke 校验'
        & (Join-Path $PSScriptRoot 'smoke-community.ps1') -Host $DeployHost -SiteMode $SiteMode
        if ($LASTEXITCODE -ne 0) {
            throw '社区 smoke 校验失败'
        }
    }
}
finally {
    if (Test-Path -LiteralPath $temporaryAppPath) {
        Remove-Item -LiteralPath $temporaryAppPath -Recurse -Force
    }

    if ($null -ne $originalLockContent -and (Test-Path -LiteralPath $lockPath)) {
        $currentLockContent = [System.IO.File]::ReadAllText($lockPath)
        if ($currentLockContent -ne $originalLockContent) {
            [System.IO.File]::WriteAllText(
                $lockPath,
                $originalLockContent,
                [System.Text.UTF8Encoding]::new($false)
            )
        }
    }
}
