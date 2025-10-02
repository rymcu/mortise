# ============================================
# PostgreSQL 权限验证脚本（PostgreSQL 17 兼容）
# ============================================

param(
    [string]$DbHost = "192.168.21.238",
    [string]$Port = "5432",
    [string]$Database = "postgres",
    [string]$User = "mortise",
    [string]$Schema = "mortise"
)

Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║     PostgreSQL 权限验证工具（PostgreSQL 17 兼容）              ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

Write-Host "验证配置：" -ForegroundColor Yellow
Write-Host "  数据库主机: $DbHost" -ForegroundColor Gray
Write-Host "  端口: $Port" -ForegroundColor Gray
Write-Host "  数据库: $Database" -ForegroundColor Gray
Write-Host "  用户: $User" -ForegroundColor Gray
Write-Host "  Schema: $Schema" -ForegroundColor Gray
Write-Host ""

# 检查 psql 是否可用
$psqlPath = Get-Command psql -ErrorAction SilentlyContinue

if (-not $psqlPath) {
    Write-Host "⚠️  未找到 psql 命令" -ForegroundColor Yellow
    Write-Host "无法自动验证，请手动执行以下 SQL 验证：" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "SELECT nspname, pg_get_userbyid(nspowner) AS owner" -ForegroundColor White
    Write-Host "FROM pg_catalog.pg_namespace" -ForegroundColor White
    Write-Host "WHERE nspname = '$Schema';" -ForegroundColor White
    Write-Host ""
    Write-Host "预期结果：owner 应该是 '$User'" -ForegroundColor Gray
    Write-Host ""
    exit 0
}

Write-Host "✅ 找到 psql: $($psqlPath.Source)" -ForegroundColor Green
Write-Host ""

# 验证 SQL
$verifySQL = @"
-- 查询 schema 所有者（PostgreSQL 17 兼容）
SELECT 
    nspname AS schema_name, 
    pg_catalog.pg_get_userbyid(nspowner) AS owner,
    CASE 
        WHEN pg_catalog.pg_get_userbyid(nspowner) = '$User' 
        THEN 'OK'
        ELSE 'PERMISSION_DENIED'
    END AS status
FROM pg_catalog.pg_namespace
WHERE nspname = '$Schema';
"@

Write-Host "正在验证权限..." -ForegroundColor Yellow
Write-Host ""

# 提示输入密码
Write-Host "⚠️  需要输入用户 ($User) 的密码" -ForegroundColor Yellow
$password = Read-Host "密码" -AsSecureString
$env:PGPASSWORD = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($password)
)

try {
    # 执行验证查询
    $result = $verifySQL | psql -h $DbHost -p $Port -U $User -d $Database -t -A -F '|' 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
        Write-Host "║  查询结果                                                      ║" -ForegroundColor Cyan
        Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
        Write-Host ""
        
        if ($result -match "OK") {
            # 解析结果
            $parts = $result -split '\|'
            $schemaName = $parts[0].Trim()
            $owner = $parts[1].Trim()
            $status = $parts[2].Trim()
            
            Write-Host "  Schema: $schemaName" -ForegroundColor White
            Write-Host "  所有者: $owner" -ForegroundColor White
            Write-Host ""
            Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Green
            Write-Host "║  ✅ 权限配置正确！                                            ║" -ForegroundColor Green
            Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Green
            Write-Host ""
            Write-Host "您可以安全地运行应用，Flyway 应该能够成功执行迁移。" -ForegroundColor Green
            Write-Host ""
        } elseif ($result -match "PERMISSION_DENIED") {
            $parts = $result -split '\|'
            $schemaName = $parts[0].Trim()
            $owner = $parts[1].Trim()
            
            Write-Host "  Schema: $schemaName" -ForegroundColor White
            Write-Host "  当前所有者: $owner" -ForegroundColor White
            Write-Host "  预期所有者: $User" -ForegroundColor White
            Write-Host ""
            Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Red
            Write-Host "║  ❌ 权限配置错误！                                            ║" -ForegroundColor Red
            Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Red
            Write-Host ""
            Write-Host "Schema '$Schema' 的所有者不是 '$User'！" -ForegroundColor Red
            Write-Host ""
            Write-Host "请运行修复脚本：" -ForegroundColor Yellow
            Write-Host "  .\fix-postgresql-permissions.ps1" -ForegroundColor White
            Write-Host ""
        } elseif ($result.Length -eq 0 -or $result -match "0 rows") {
            Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Yellow
            Write-Host "║  ⚠️  Schema 不存在                                            ║" -ForegroundColor Yellow
            Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Yellow
            Write-Host ""
            Write-Host "Schema '$Schema' 不存在！" -ForegroundColor Yellow
            Write-Host ""
            Write-Host "这是正常的，首次启动时 Flyway 会自动创建。" -ForegroundColor Gray
            Write-Host "但需要确保用户有创建 schema 的权限。" -ForegroundColor Gray
            Write-Host ""
            Write-Host "建议运行修复脚本提前创建：" -ForegroundColor Yellow
            Write-Host "  .\fix-postgresql-permissions.ps1" -ForegroundColor White
            Write-Host ""
        } else {
            Write-Host "查询结果：" -ForegroundColor Gray
            Write-Host $result -ForegroundColor White
            Write-Host ""
        }
    } else {
        Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Red
        Write-Host "║  ❌ 验证失败                                                   ║" -ForegroundColor Red
        Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Red
        Write-Host ""
        Write-Host "错误信息：" -ForegroundColor Red
        Write-Host $result -ForegroundColor White
        Write-Host ""
        
        if ($result -match "password authentication failed") {
            Write-Host "💡 提示：密码错误，请检查密码是否正确" -ForegroundColor Yellow
        } elseif ($result -match "could not connect") {
            Write-Host "💡 提示：无法连接数据库，请检查：" -ForegroundColor Yellow
            Write-Host "  1. 数据库服务是否运行" -ForegroundColor Gray
            Write-Host "  2. 主机地址和端口是否正确" -ForegroundColor Gray
            Write-Host "  3. 防火墙是否允许连接" -ForegroundColor Gray
        }
        Write-Host ""
    }
} catch {
    Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Red
    Write-Host "║  ❌ 执行出错                                                   ║" -ForegroundColor Red
    Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Red
    Write-Host ""
    Write-Host "错误：$_" -ForegroundColor Red
    Write-Host ""
} finally {
    # 清除密码
    $env:PGPASSWORD = $null
}

Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
