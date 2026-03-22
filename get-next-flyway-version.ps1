[CmdletBinding()]
param(
    [string]$RootPath = ".",
    [switch]$Json,
    [switch]$FailOnDuplicates
)

$resolvedRoot = (Resolve-Path -Path $RootPath).Path
# 扫描两类有效迁移目录：
# 1. 标准 DDD 模块：mortise-xx/mortise-xx-infra/src/main/resources/db/migration
# 2. 历史单模块：mortise-xx/src/main/resources/db/migration
$migrationFiles = Get-ChildItem -Path $resolvedRoot -Recurse -File -Filter "V*.sql" |
    Where-Object {
        $_.FullName -like "*\src\main\resources\db\migration\*" -and
        $_.FullName -notlike "*\old-code\*"
    }

$scripts = foreach ($file in $migrationFiles) {
    if ($file.Name -match '^V(?<version>\d+)__(?<description>.+)\.sql$') {
        $relativePath = $file.FullName.Substring($resolvedRoot.Length).TrimStart('\')
        $moduleName = $relativePath.Split('\')[0]

        [PSCustomObject]@{
            Version = [int]$matches.version
            Description = $matches.description
            Module = $moduleName
            RelativePath = $relativePath
        }
    }
}

$orderedScripts = $scripts | Sort-Object Version, RelativePath
$versionGroups = $orderedScripts | Group-Object Version | Sort-Object { [int]$_.Name }
$duplicateGroups = $versionGroups | Where-Object { $_.Count -gt 1 }

$maxVersion = 0
if ($orderedScripts.Count -gt 0) {
    $maxVersion = ($orderedScripts | Measure-Object -Property Version -Maximum).Maximum
}

$nextVersion = $maxVersion + 1
$latestScripts = $orderedScripts | Sort-Object Version -Descending | Select-Object -First 10

$result = [PSCustomObject]@{
    RootPath = $resolvedRoot
    TotalScripts = @($orderedScripts).Count
    MaxVersion = $maxVersion
    NextVersion = $nextVersion
    HasDuplicates = @($duplicateGroups).Count -gt 0
    Duplicates = @(
        foreach ($group in $duplicateGroups) {
            [PSCustomObject]@{
                Version = [int]$group.Name
                Files = @($group.Group | Select-Object -ExpandProperty RelativePath)
            }
        }
    )
    LatestScripts = @($latestScripts)
}

if ($Json) {
    $result | ConvertTo-Json -Depth 6
    exit 0
}

Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║        Mortise Flyway 下一可用版本号查询工具                  ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "仓库路径: $resolvedRoot" -ForegroundColor Gray
Write-Host "扫描脚本数: $(@($orderedScripts).Count)" -ForegroundColor Gray
Write-Host "扫描范围: mortise-xx-infra 与历史单模块迁移目录" -ForegroundColor Gray
Write-Host "当前最大版本: V$maxVersion" -ForegroundColor Yellow
Write-Host "建议下一个版本: V$nextVersion" -ForegroundColor Green
Write-Host ""

if (@($duplicateGroups).Count -gt 0) {
    Write-Host "检测到重复版本号，请先处理冲突再创建新脚本：" -ForegroundColor Red
    foreach ($group in $duplicateGroups) {
        Write-Host "  V$($group.Name)" -ForegroundColor Red
        foreach ($item in $group.Group) {
            Write-Host "    - $($item.RelativePath)" -ForegroundColor DarkYellow
        }
    }
    Write-Host ""
}

Write-Host "最近 10 个已使用版本：" -ForegroundColor Yellow
foreach ($script in $latestScripts) {
    Write-Host "  V$($script.Version)  [$($script.Module)]  $($script.Description)" -ForegroundColor White
}

Write-Host ""

if ($FailOnDuplicates) {
    if (@($duplicateGroups).Count -gt 0) {
        Write-Host "Flyway 版本号校验失败，已阻止继续执行。" -ForegroundColor Red
        exit 1
    }

    Write-Host "Flyway 版本号校验通过，未发现重复版本号。" -ForegroundColor Green
    exit 0
}

Write-Host "创建新 Flyway SQL 前，请使用上面的 V$nextVersion 作为候选版本，并再次确认没有并发新增。" -ForegroundColor Gray