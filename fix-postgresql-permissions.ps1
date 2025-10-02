# ============================================
# PostgreSQL 权限修复脚本
# 用于解决 Flyway 权限问题
# ============================================

param(
    [string]$DbHost = "192.168.21.238",
    [string]$Port = "5432",
    [string]$Database = "postgres",
    [string]$SuperUser = "postgres",
    [string]$AppUser = "mortise",
    [string]$Schema = "mortise"
)

Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║     PostgreSQL 权限修复工具                                    ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

Write-Host "配置信息：" -ForegroundColor Yellow
Write-Host "  数据库主机: $DbHost" -ForegroundColor Gray
Write-Host "  端口: $Port" -ForegroundColor Gray
Write-Host "  数据库: $Database" -ForegroundColor Gray
Write-Host "  超级用户: $SuperUser" -ForegroundColor Gray
Write-Host "  应用用户: $AppUser" -ForegroundColor Gray
Write-Host "  Schema: $Schema" -ForegroundColor Gray
Write-Host ""

# 检查 psql 是否可用
Write-Host "检查 PostgreSQL 客户端..." -ForegroundColor Yellow
$psqlPath = Get-Command psql -ErrorAction SilentlyContinue

if (-not $psqlPath) {
    Write-Host "❌ 未找到 psql 命令！" -ForegroundColor Red
    Write-Host ""
    Write-Host "请安装 PostgreSQL 客户端工具，或者手动执行以下 SQL：" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "-- 连接到数据库" -ForegroundColor Gray
    Write-Host "psql -h $DbHost -p $Port -U $SuperUser -d $Database" -ForegroundColor White
    Write-Host ""
    Write-Host "-- 执行以下 SQL" -ForegroundColor Gray
    Write-Host "CREATE SCHEMA IF NOT EXISTS $Schema;" -ForegroundColor White
    Write-Host "ALTER SCHEMA $Schema OWNER TO $AppUser;" -ForegroundColor White
    Write-Host "GRANT ALL PRIVILEGES ON SCHEMA $Schema TO $AppUser;" -ForegroundColor White
    Write-Host "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA $Schema TO $AppUser;" -ForegroundColor White
    Write-Host "GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA $Schema TO $AppUser;" -ForegroundColor White
    Write-Host ""
    Write-Host "或者使用 SQL 文件：" -ForegroundColor Yellow
    Write-Host "psql -h $DbHost -p $Port -U $SuperUser -d $Database -f docs\fix-postgresql-permissions.sql" -ForegroundColor White
    Write-Host ""
    exit 1
}

Write-Host "✅ 找到 psql: $($psqlPath.Source)" -ForegroundColor Green
Write-Host ""

# 生成 SQL 命令
$sqlCommands = @"
-- 授予数据库级别权限（关键！）
GRANT CREATE ON DATABASE $Database TO $AppUser;
GRANT CONNECT ON DATABASE $Database TO $AppUser;

-- 创建 schema
CREATE SCHEMA IF NOT EXISTS $Schema;

-- 更改 schema 所有者
ALTER SCHEMA $Schema OWNER TO $AppUser;

-- 授予权限
GRANT USAGE ON SCHEMA $Schema TO $AppUser;
GRANT CREATE ON SCHEMA $Schema TO $AppUser;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA $Schema TO $AppUser;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA $Schema TO $AppUser;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA $Schema TO $AppUser;

-- 设置默认权限
ALTER DEFAULT PRIVILEGES IN SCHEMA $Schema GRANT ALL PRIVILEGES ON TABLES TO $AppUser;
ALTER DEFAULT PRIVILEGES IN SCHEMA $Schema GRANT ALL PRIVILEGES ON SEQUENCES TO $AppUser;
ALTER DEFAULT PRIVILEGES IN SCHEMA $Schema GRANT ALL PRIVILEGES ON FUNCTIONS TO $AppUser;

-- 验证（PostgreSQL 17 兼容）
SELECT nspname AS schema_name, pg_catalog.pg_get_userbyid(nspowner) AS schema_owner 
FROM pg_catalog.pg_namespace 
WHERE nspname = '$Schema';
"@

# 提示用户
Write-Host "准备执行以下操作：" -ForegroundColor Yellow
Write-Host "  1. 授予数据库 CREATE 权限（允许创建 schema）" -ForegroundColor Gray
Write-Host "  2. 创建 schema: $Schema" -ForegroundColor Gray
Write-Host "  3. 设置 schema 所有者为: $AppUser" -ForegroundColor Gray
Write-Host "  4. 授予 schema 完整权限" -ForegroundColor Gray
Write-Host "  5. 设置默认权限" -ForegroundColor Gray
Write-Host ""

Write-Host "⚠️  需要输入超级用户 ($SuperUser) 的密码" -ForegroundColor Yellow
Write-Host ""

$continue = Read-Host "是否继续? (y/n)"
if ($continue -ne "y") {
    Write-Host "操作已取消" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "执行 SQL 命令..." -ForegroundColor Yellow
Write-Host ""

# 执行 SQL（通过管道传递密码会有安全风险，但这是最简单的方式）
$env:PGPASSWORD = Read-Host "请输入 $SuperUser 的密码" -AsSecureString | ConvertFrom-SecureString -AsPlainText

try {
    # 使用 psql 执行 SQL
    $sqlCommands | psql -h $DbHost -p $Port -U $SuperUser -d $Database -v ON_ERROR_STOP=1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Green
        Write-Host "║  ✅ 权限修复成功！                                            ║" -ForegroundColor Green
        Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Green
        Write-Host ""
        Write-Host "下一步：" -ForegroundColor Yellow
        Write-Host "  1. 重新启动应用: mvn spring-boot:run" -ForegroundColor White
        Write-Host "  2. Flyway 应该能够成功执行迁移" -ForegroundColor White
        Write-Host ""
    } else {
        Write-Host ""
        Write-Host "❌ 执行失败，请检查错误信息" -ForegroundColor Red
        Write-Host ""
        Write-Host "手动执行方法：" -ForegroundColor Yellow
        Write-Host "psql -h $DbHost -p $Port -U $SuperUser -d $Database -f docs\fix-postgresql-permissions.sql" -ForegroundColor White
        Write-Host ""
        exit 1
    }
} catch {
    Write-Host ""
    Write-Host "❌ 执行出错: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "请尝试手动执行：" -ForegroundColor Yellow
    Write-Host "psql -h $DbHost -p $Port -U $SuperUser -d $Database -f docs\fix-postgresql-permissions.sql" -ForegroundColor White
    Write-Host ""
    exit 1
} finally {
    # 清除环境变量中的密码
    $env:PGPASSWORD = $null
}
