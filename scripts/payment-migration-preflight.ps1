[CmdletBinding()]
param(
    [string] $SshTarget = 'root@192.168.88.147',
    [string] $RemoteEnv = '/opt/mortise/.env',
    [int] $ExpectedLatestVersion = 440
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
$migrationFiles = @(
    Join-Path $repoRoot 'mortise-member/mortise-member-infra/src/main/resources/db/migration/V441__Add_Member_Client_Session_Scope.sql'
    Join-Path $repoRoot 'mortise-commerce/mortise-commerce-infra/src/main/resources/db/migration/V442__Create_Commerce_Payment_Event_Inbox.sql'
    Join-Path $repoRoot 'mortise-commerce/mortise-commerce-infra/src/main/resources/db/migration/V443__Create_Commerce_Payment_Shadow_Audit.sql'
)

function Assert-Condition {
    param(
        [bool] $Condition,
        [string] $Message
    )

    if (-not $Condition) {
        throw $Message
    }
}

function Invoke-RemoteSql {
    param(
        [string] $Sql
    )

    $remoteCommand = ('set -euo pipefail; set -a; . "' + $RemoteEnv +
        '"; set +a; docker exec -i -e PGPASSWORD="$POSTGRES_PASSWORD" mortise-postgres ' +
        'psql --quiet --tuples-only --no-align --set=ON_ERROR_STOP=1 ' +
        '--username="$POSTGRES_USER" --dbname="$POSTGRES_DB" --file=-')
    $output = $Sql | ssh $SshTarget $remoteCommand
    if ($LASTEXITCODE -ne 0) {
        throw "远端 PostgreSQL 只读检查失败，ssh exit code=$LASTEXITCODE"
    }
    return @($output | ForEach-Object { $_.ToString().Trim() } | Where-Object { $_ })
}

try {
    foreach ($migrationFile in $migrationFiles) {
        Assert-Condition (Test-Path -LiteralPath $migrationFile -PathType Leaf) `
            "迁移文件不存在: $migrationFile"
    }

    $sql = @"
SELECT COALESCE(MAX(version::integer), 0)
  FROM mortise.flyway_schema_history
 WHERE success;
SELECT COALESCE(string_agg(version, ',' ORDER BY installed_rank), 'NONE')
  FROM mortise.flyway_schema_history
 WHERE success AND version IN ('441', '442', '443');
SELECT COUNT(*)
  FROM information_schema.columns
 WHERE table_schema = 'mortise'
   AND table_name = 'mortise_member_client_session'
   AND column_name = 'scope';
SELECT COALESCE(to_regclass('mortise.mortise_commerce_event_inbox')::text, 'ABSENT');
SELECT COALESCE(to_regclass('mortise.mortise_commerce_payment_shadow_audit')::text, 'ABSENT');
"@
    $dbResults = Invoke-RemoteSql -Sql $sql
    Assert-Condition ($dbResults.Count -eq 5) `
        "远端检查返回行数异常: $($dbResults.Count)"

    $latestVersion = [int] $dbResults[0]
    $appliedPaymentVersions = $dbResults[1]
    Assert-Condition ($latestVersion -eq $ExpectedLatestVersion) `
        "Flyway 最新成功版本为 V$latestVersion，预期为 V$ExpectedLatestVersion；停止，不执行迁移"
    Assert-Condition ($appliedPaymentVersions -eq 'NONE') `
        "V441/V442/V443 已有成功记录: $appliedPaymentVersions；停止，不重复执行"
    Assert-Condition ($dbResults[2] -eq '0') `
        "V441 scope 列已存在: $($dbResults[2])；先核对 Flyway 历史，不执行迁移"
    Assert-Condition ($dbResults[3] -eq 'ABSENT') `
        "V442 Inbox 表已存在: $($dbResults[3])；先人工核对，不执行迁移"
    Assert-Condition ($dbResults[4] -eq 'ABSENT') `
        "V443 shadow 审计表已存在: $($dbResults[4])；先人工核对，不执行迁移"

    $fileResults = foreach ($migrationFile in $migrationFiles) {
        $hash = Get-FileHash -LiteralPath $migrationFile -Algorithm SHA256
        [ordered]@{
            version = [int] [regex]::Match(
                (Split-Path -Leaf $migrationFile), '^V(\d+)__').Groups[1].Value
            path = $migrationFile.Substring($repoRoot.Length + 1).Replace('\', '/')
            sha256 = $hash.Hash.ToLowerInvariant()
        }
    }

    [ordered]@{
        status = 'ok'
        readOnly = $true
        sshTarget = $SshTarget
        flywayLatestSuccessfulVersion = $latestVersion
        unappliedVersions = @(441, 442, 443)
        checkedColumnsAbsent = @(
            'mortise.mortise_member_client_session.scope'
        )
        checkedTablesAbsent = @(
            'mortise.mortise_commerce_event_inbox'
            'mortise.mortise_commerce_payment_shadow_audit'
        )
        migrationFiles = @($fileResults)
    } | ConvertTo-Json -Depth 5
} catch {
    [ordered]@{
        status = 'error'
        readOnly = $true
        message = $_.Exception.Message
    } | ConvertTo-Json -Compress
    exit 1
}
