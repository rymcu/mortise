Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$specRoot = Join-Path $PSScriptRoot "..\specs" | Resolve-Path
$indexPath = Join-Path $specRoot "INDEX.md"

$excludeFiles = @("README.md", "WORKFLOW.md", "INDEX.md")

Push-Location $specRoot
try {
    $specFiles = Get-ChildItem -Path $specRoot -Filter "*.md" -Recurse |
        Where-Object { $_.FullName -notmatch "\\templates\\" } |
        Where-Object { $excludeFiles -notcontains $_.Name } |
        Sort-Object FullName

    $lines = New-Object System.Collections.Generic.List[string]
    $lines.Add("# 需求规范索引")
    $lines.Add("")
    $lines.Add("此索引汇总仓库中的需求规范文档。")
    $lines.Add("")
    $lines.Add("- 生成脚本: docs/scripts/generate-spec-index.ps1")
    $lines.Add("- 更新命令: pwsh ./docs/scripts/generate-spec-index.ps1")
    $lines.Add("")
    $lines.Add("| 业务域 | 标题 | 文件 | 状态 |")
    $lines.Add("| --- | --- | --- | --- |")

    foreach ($file in $specFiles) {
        $preview = Get-Content -Path $file.FullName -TotalCount 40
        $titleLine = $preview | Where-Object { $_ -match "^#\s+" } | Select-Object -First 1
        $title = if ($titleLine) { $titleLine -replace "^#\s+", "" } else { $file.BaseName }

        $domainLine = $preview | Where-Object { $_ -match "^-\s*业务域:\s*" } | Select-Object -First 1
        $domain = if ($domainLine) { $domainLine -replace "^-\s*业务域:\s*", "" } else { "Unknown" }

        $statusLine = $preview | Where-Object { $_ -match "^-\s*状态:\s*" } | Select-Object -First 1
        $status = if ($statusLine) { $statusLine -replace "^-\s*状态:\s*", "" } else { "Unknown" }

        $relativePath = Resolve-Path -Relative $file.FullName
        $lines.Add("| $domain | $title | [$relativePath]($relativePath) | $status |")
    }

    Set-Content -Path $indexPath -Value $lines -Encoding UTF8
} finally {
    Pop-Location
}
