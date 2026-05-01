[CmdletBinding()]
param()

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$hookPath = Join-Path $repoRoot ".githooks"

if (-not (Test-Path $hookPath)) {
    Write-Host "未找到 .githooks 目录：$hookPath" -ForegroundColor Red
    exit 1
}

git -C $repoRoot config core.hooksPath .githooks
if ($LASTEXITCODE -ne 0) {
    Write-Host "Git hooksPath 配置失败，请确认当前目录是 Git 仓库。" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "已配置 Git hooksPath -> .githooks" -ForegroundColor Green
Write-Host "后续提交会自动执行 Flyway 版本号重复检查。" -ForegroundColor Green
Write-Host "后续提交会自动检查 PR 模板和当前分支 PR 的规范链接字段。" -ForegroundColor Green
