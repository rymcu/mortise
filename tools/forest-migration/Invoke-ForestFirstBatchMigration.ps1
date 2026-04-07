[CmdletBinding()]
param(
    [string]$SourceSsh = "root@47.100.218.231",
    [string]$SourceMysqlContainer = "mysql",
    [string]$SourceDatabase = "forest",
    [string]$SourceMysqlPassword = "XzHvhX4CDaN696oQAXdmlcsrqgA06RU3",
    [string]$TargetSsh = "root@192.168.88.147",
    [string]$TargetPostgresContainer = "mortise-postgres",
    [string]$TargetDatabase = "postgres",
    [string]$TargetPostgresUser = "mortise",
    [string]$TargetSchema = "mortise",
    [string]$TargetLegacySchema = "legacy",
    [switch]$SkipExecution
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# 确保 SSH 管道以 UTF-8 读取外部命令输出（中文字段必须）
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$workRoot = Join-Path $repoRoot ".tmp\forest-first-batch"
$extractDir = Join-Path $workRoot "extract"
$sqlDir = Join-Path $workRoot "sql"
$reportDir = Join-Path $workRoot "reports"

function Ensure-Directory {
    param([string]$Path)

    if (-not (Test-Path $Path)) {
        New-Item -ItemType Directory -Path $Path -Force | Out-Null
    }
}

function Write-Stage {
    param([string]$Message)

    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Invoke-SourceMySql {
    param([string]$Sql)

    $remoteCommand = "docker exec -i $SourceMysqlContainer mysql -uroot -p$SourceMysqlPassword -D $SourceDatabase --default-character-set=utf8mb4 --batch --raw --skip-column-names 2>/dev/null"
    $result = $Sql | & ssh $SourceSsh $remoteCommand
    if ($LASTEXITCODE -ne 0) {
        throw "执行 MySQL 查询失败。"
    }
    return @($result)
}

function Invoke-TargetPostgres {
    param(
        [string]$Sql,
        [switch]$AsQuery
    )

    $mode = if ($AsQuery) { "-A -t -F '|'" } else { "" }
    $remoteCommand = "docker exec -i $TargetPostgresContainer psql -U $TargetPostgresUser -d $TargetDatabase -v ON_ERROR_STOP=1 -X -q $mode"
    $result = $Sql | & ssh $TargetSsh $remoteCommand
    if ($LASTEXITCODE -ne 0) {
        throw "执行 PostgreSQL SQL 失败。"
    }
    return @($result)
}

function Convert-JsonLinesToObjects {
    param([string[]]$Lines)

    $objects = [System.Collections.Generic.List[hashtable]]::new()
    foreach ($line in $Lines) {
        if ([string]::IsNullOrWhiteSpace($line)) {
            continue
        }
        $objects.Add(($line | ConvertFrom-Json -AsHashtable))
    }
    return $objects
}

function Save-JsonFile {
    param(
        [string]$Path,
        [object]$Value
    )

    $json = $Value | ConvertTo-Json -Depth 30
    Set-Content -Path $Path -Value $json -Encoding utf8
}

function Null-IfBlank {
    param([object]$Value)

    if ($null -eq $Value) {
        return $null
    }
    $text = [string]$Value
    if ([string]::IsNullOrWhiteSpace($text)) {
        return $null
    }
    return $text.Trim()
}

function To-Int {
    param([object]$Value, [int]$Default = 0)

    $text = Null-IfBlank $Value
    if ($null -eq $text) {
        return $Default
    }
    return [int]$text
}

function To-Long {
    param([object]$Value)

    $text = Null-IfBlank $Value
    if ($null -eq $text) {
        return $null
    }
    return [long]$text
}

function To-DateTimeString {
    param([object]$Value)

    $text = Null-IfBlank $Value
    if ($null -eq $text) {
        return $null
    }
    return ([datetime]$text).ToString("yyyy-MM-dd HH:mm:ss")
}

function To-Flag {
    param(
        [object]$Value,
        [int]$Default = 0
    )

    $text = Null-IfBlank $Value
    if ($null -eq $text) {
        return $Default
    }
    if ($text -eq "1") { return 1 } else { return 0 }
}

function Convert-Gender {
    param([object]$Value)

    $result = switch (Null-IfBlank $Value) {
        "1" { "male" }
        "2" { "female" }
        default { "other" }
    }
    return $result
}

function Convert-MemberStatus {
    param([object]$Value)

    if ((Null-IfBlank $Value) -eq "0") { return 1 } else { return 0 }
}

function Convert-ArticleStatus {
    param([object]$Value)

    if ((Null-IfBlank $Value) -eq "0") { return 1 } else { return 0 }
}

function Convert-HtmlToPlainText {
    param([string]$Html)

    $text = Null-IfBlank $Html
    if ($null -eq $text) {
        return $null
    }

    $normalized = $text `
        -replace '(?i)<br\s*/?>', "`n" `
        -replace '(?i)</p\s*>', "`n" `
        -replace '(?i)</div\s*>', "`n" `
        -replace '(?i)</li\s*>', "`n" `
        -replace '(?i)<li[^>]*>', '• ' `
        -replace '<[^>]+>', ' '

    $decoded = [System.Net.WebUtility]::HtmlDecode($normalized)
    $collapsed = ($decoded -replace '\s+', ' ').Trim()
    if ([string]::IsNullOrWhiteSpace($collapsed)) {
        return [System.Net.WebUtility]::HtmlDecode($text).Trim()
    }
    return $collapsed
}

function Convert-ToSqlNumber {
    param([object]$Value)

    return [string]::Format(
        [System.Globalization.CultureInfo]::InvariantCulture,
        "{0}",
        $Value
    )
}

function Convert-ToSqlValue {
    param([object]$Value)

    if ($null -eq $Value) {
        return "NULL"
    }

    if ($Value -is [bool]) {
        if ($Value) { return "TRUE" } else { return "FALSE" }
    }

    if ($Value -is [byte] -or $Value -is [int16] -or $Value -is [int32] -or $Value -is [int64] -or
        $Value -is [decimal] -or $Value -is [double] -or $Value -is [single]) {
        return (Convert-ToSqlNumber $Value)
    }

    if ($Value -is [System.Collections.IDictionary]) {
        $json = ($Value | ConvertTo-Json -Compress -Depth 30).Replace("'", "''")
        return "'$json'::jsonb"
    }

    if ($Value -is [object[]] -or ($Value -is [System.Collections.IEnumerable] -and -not ($Value -is [string]))) {
        $elements = @($Value | ForEach-Object {
            $escaped = ([string]$_).Replace("'", "''")
            "'$escaped'"
        })
        return "ARRAY[" + ($elements -join ", ") + "]::varchar[]"
    }

    $text = [string]$Value
    $escaped = $text.Replace("'", "''")
    return "'$escaped'"
}

function Add-InsertSql {
    param(
        [System.Text.StringBuilder]$Builder,
        [string]$TableName,
        [string[]]$Columns,
        [System.Collections.IEnumerable]$Rows,
        [int]$BatchSize = 100
    )

    $buffer = @($Rows)
    if ($buffer.Count -eq 0) {
        return
    }

    for ($offset = 0; $offset -lt $buffer.Count; $offset += $BatchSize) {
        $end = [Math]::Min($offset + $BatchSize - 1, $buffer.Count - 1)
        [void]$Builder.Append("INSERT INTO $TableName (")
        [void]$Builder.Append(($Columns -join ", "))
        [void]$Builder.AppendLine(") VALUES")

        $tuples = for ($index = $offset; $index -le $end; $index++) {
            $row = $buffer[$index]
            $values = foreach ($column in $Columns) {
                Convert-ToSqlValue $row[$column]
            }
            "    (" + ($values -join ", ") + ")"
        }

        [void]$Builder.Append(($tuples -join ",`n"))
        [void]$Builder.AppendLine(";")
        [void]$Builder.AppendLine()
    }
}

function New-LegacyKey {
    param(
        [string]$TableName,
        [hashtable]$Row
    )

    $result = switch ($TableName) {
        "forest_user" { [string]$Row["id"] }
        "forest_user_extend" { [string]$Row["id_user"] }
        "forest_article" { [string]$Row["id"] }
        "forest_article_content" { [string]$Row["id_article"] }
        "forest_comment" { [string]$Row["id"] }
        "forest_portfolio" { [string]$Row["id"] }
        "forest_portfolio_article" { [string]$Row["id"] }
        "forest_tag" { [string]$Row["id"] }
        "forest_tag_article" { [string]$Row["id"] }
        "forest_topic" { [string]$Row["id"] }
        "forest_topic_tag" { [string]$Row["id"] }
        "forest_follow" { [string]$Row["id"] }
        "forest_product" { [string]$Row["id"] }
        "forest_product_content" { [string]$Row["id_product"] }
        default { throw "未定义 legacy key: $TableName" }
    }
    return $result
}

function New-DeterministicIdSequence {
    param([long]$Seed)

    $current = $Seed
    return {
        $script:current += 1
        return $script:current
    }.GetNewClosure()
}

function Get-Value {
    param(
        [hashtable]$Map,
        [object]$Key
    )

    if ($null -eq $Key) {
        return $null
    }
    if ($Map.ContainsKey($Key)) {
        return $Map[$Key]
    }
    return $null
}

Ensure-Directory $workRoot
Ensure-Directory $extractDir
Ensure-Directory $sqlDir
Ensure-Directory $reportDir

Write-Stage "校验 Phase 0 结果"
$phase0CheckSql = @"
SELECT 'flyway_schema_history', EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = '$TargetSchema'
      AND table_name = 'flyway_schema_history'
)::text
UNION ALL
SELECT 'mortise_member', EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = '$TargetSchema'
      AND table_name = 'mortise_member'
)::text
UNION ALL
SELECT 'mortise_article', EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = '$TargetSchema'
      AND table_name = 'mortise_article'
)::text
UNION ALL
SELECT 'mortise_comment', EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = '$TargetSchema'
      AND table_name = 'mortise_comment'
)::text
UNION ALL
SELECT 'mortise_collection', EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = '$TargetSchema'
      AND table_name = 'mortise_collection'
)::text;
"@
$phase0Checks = Invoke-TargetPostgres -Sql $phase0CheckSql -AsQuery
$missingPhase0 = @($phase0Checks | Where-Object { $_ -like "*|f" })
if ($missingPhase0.Count -gt 0) {
    throw "目标库尚未完成 Phase 0，请先确保 Flyway 已成功执行。"
}

$flywayVersionSql = "SELECT version FROM $TargetSchema.flyway_schema_history ORDER BY installed_rank DESC LIMIT 1;"
$latestFlywayVersion = (Invoke-TargetPostgres -Sql $flywayVersionSql -AsQuery | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }) | Select-Object -First 1
Write-Host "  Flyway 最新版本: V$latestFlywayVersion" -ForegroundColor DarkGray

$totalTablesSql = "SELECT COUNT(*)::text FROM information_schema.tables WHERE table_schema = '$TargetSchema';"
$totalTables = (Invoke-TargetPostgres -Sql $totalTablesSql -AsQuery | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }) | Select-Object -First 1
Write-Host "  mortise schema 表数量: $totalTables" -ForegroundColor DarkGray

$extractDefinitions = [ordered]@{
    forest_user = @"
SELECT JSON_OBJECT(
    'id', id,
    'account', account,
    'password', password,
    'nickname', nickname,
    'real_name', real_name,
    'sex', sex,
    'avatar_type', avatar_type,
    'avatar_url', avatar_url,
    'email', email,
    'phone', phone,
    'status', status,
    'created_time', created_time,
    'updated_time', updated_time,
    'last_login_time', last_login_time,
    'signature', signature,
    'last_online_time', last_online_time,
    'bg_img_url', bg_img_url
) FROM forest_user
ORDER BY id;
"@
    forest_user_extend = @"
SELECT JSON_OBJECT(
    'id_user', id_user,
    'github', github,
    'weibo', weibo,
    'weixin', weixin,
    'qq', qq,
    'blog', blog
) FROM forest_user_extend
ORDER BY id_user;
"@
    forest_article = @"
SELECT JSON_OBJECT(
    'id', id,
    'article_title', article_title,
    'article_thumbnail_url', article_thumbnail_url,
    'article_author_id', article_author_id,
    'article_type', article_type,
    'article_tags', article_tags,
    'article_view_count', article_view_count,
    'article_preview_content', article_preview_content,
    'article_comment_count', article_comment_count,
    'article_permalink', article_permalink,
    'article_link', article_link,
    'created_time', created_time,
    'updated_time', updated_time,
    'article_perfect', article_perfect,
    'article_status', article_status,
    'article_thumbs_up_count', article_thumbs_up_count,
    'article_sponsor_count', article_sponsor_count
) FROM forest_article
ORDER BY id;
"@
    forest_article_content = @"
SELECT JSON_OBJECT(
    'id_article', id_article,
    'article_content', article_content,
    'article_content_html', article_content_html,
    'created_time', created_time,
    'updated_time', updated_time
) FROM forest_article_content
ORDER BY id_article;
"@
    forest_comment = @"
SELECT JSON_OBJECT(
    'id', id,
    'comment_content', comment_content,
    'comment_author_id', comment_author_id,
    'comment_article_id', comment_article_id,
    'comment_sharp_url', comment_sharp_url,
    'comment_original_comment_id', comment_original_comment_id,
    'comment_status', comment_status,
    'comment_ip', comment_ip,
    'comment_ua', comment_ua,
    'comment_anonymous', comment_anonymous,
    'comment_reply_count', comment_reply_count,
    'comment_visible', comment_visible,
    'created_time', created_time
) FROM forest_comment
ORDER BY id;
"@
    forest_portfolio = @"
SELECT JSON_OBJECT(
    'id', id,
    'portfolio_head_img_url', portfolio_head_img_url,
    'portfolio_title', portfolio_title,
    'portfolio_author_id', portfolio_author_id,
    'portfolio_description', portfolio_description,
    'created_time', created_time,
    'updated_time', updated_time,
    'portfolio_description_html', portfolio_description_html
) FROM forest_portfolio
ORDER BY id;
"@
    forest_portfolio_article = @"
SELECT JSON_OBJECT(
    'id', id,
    'id_portfolio', id_portfolio,
    'id_article', id_article,
    'sort_no', sort_no
) FROM forest_portfolio_article
ORDER BY id;
"@
    forest_tag = @"
SELECT JSON_OBJECT(
    'id', id,
    'tag_title', tag_title,
    'tag_icon_path', tag_icon_path,
    'tag_uri', tag_uri,
    'tag_description', tag_description,
    'tag_view_count', tag_view_count,
    'tag_article_count', tag_article_count,
    'tag_ad', tag_ad,
    'tag_show_side_ad', tag_show_side_ad,
    'created_time', created_time,
    'updated_time', updated_time,
    'tag_status', tag_status,
    'tag_reservation', tag_reservation,
    'tag_description_html', tag_description_html
) FROM forest_tag
ORDER BY id;
"@
    forest_tag_article = @"
SELECT JSON_OBJECT(
    'id', id,
    'id_tag', id_tag,
    'id_article', id_article,
    'article_comment_count', article_comment_count,
    'article_perfect', article_perfect,
    'created_time', created_time,
    'updated_time', updated_time
) FROM forest_tag_article
ORDER BY id;
"@
    forest_topic = @"
SELECT JSON_OBJECT(
    'id', id,
    'topic_title', topic_title,
    'topic_uri', topic_uri,
    'topic_description', topic_description,
    'topic_type', topic_type,
    'topic_sort', topic_sort,
    'topic_icon_path', topic_icon_path,
    'topic_nva', topic_nva,
    'topic_tag_count', topic_tag_count,
    'topic_status', topic_status,
    'created_time', created_time,
    'updated_time', updated_time,
    'topic_description_html', topic_description_html
) FROM forest_topic
ORDER BY id;
"@
    forest_topic_tag = @"
SELECT JSON_OBJECT(
    'id', id,
    'id_topic', id_topic,
    'id_tag', id_tag,
    'created_time', created_time,
    'updated_time', updated_time
) FROM forest_topic_tag
ORDER BY id;
"@
    forest_follow = @"
SELECT JSON_OBJECT(
    'id', id,
    'follower_id', follower_id,
    'following_id', following_id,
    'following_type', following_type
) FROM forest_follow
ORDER BY id;
"@
    forest_product = @"
SELECT JSON_OBJECT(
    'id', id,
    'product_title', product_title,
    'product_price', product_price,
    'product_img_url', product_img_url,
    'product_description', product_description,
    'weights', weights,
    'tags', tags,
    'status', status,
    'created_time', created_time,
    'updated_time', updated_time
) FROM forest_product
ORDER BY id;
"@
    forest_product_content = @"
SELECT JSON_OBJECT(
    'id_product', id_product,
    'product_content', product_content,
    'product_content_html', product_content_html,
    'created_time', created_time,
    'updated_time', updated_time
) FROM forest_product_content
ORDER BY id_product;
"@
}

$sourceData = @{}
Write-Stage "抽取旧库数据"
foreach ($tableName in $extractDefinitions.Keys) {
    $rows = Convert-JsonLinesToObjects (Invoke-SourceMySql -Sql $extractDefinitions[$tableName])
    $sourceData[$tableName] = $rows
    Save-JsonFile -Path (Join-Path $extractDir "$tableName.json") -Value $rows
    Write-Host "  $tableName -> $($rows.Count) 行" -ForegroundColor DarkGray
}

$users = @($sourceData["forest_user"])
$userExtends = @($sourceData["forest_user_extend"])
$articles = @($sourceData["forest_article"])
$articleContents = @($sourceData["forest_article_content"])
$comments = @($sourceData["forest_comment"])
$portfolios = @($sourceData["forest_portfolio"])
$portfolioArticles = @($sourceData["forest_portfolio_article"])
$tags = @($sourceData["forest_tag"])
$tagArticles = @($sourceData["forest_tag_article"])
$topics = @($sourceData["forest_topic"])
$topicTags = @($sourceData["forest_topic_tag"])
$follows = @($sourceData["forest_follow"])
$products = @($sourceData["forest_product"])
$productContents = @($sourceData["forest_product_content"])

$anomalies = [ordered]@{
    phase0 = [ordered]@{
        workaround = "已通过预建 mortise.mortise_product 兼容 V80/V100 顺序冲突，再由 Flyway 继续执行到 v414。"
    }
    email_conflicts = [System.Collections.Generic.List[hashtable]]::new()
    invalid_articles = [System.Collections.Generic.List[hashtable]]::new()
    invalid_comments = [System.Collections.Generic.List[hashtable]]::new()
    invalid_collection_articles = [System.Collections.Generic.List[hashtable]]::new()
    filtered_tag_article_relations = [System.Collections.Generic.List[hashtable]]::new()
    filtered_topic_tag_relations = [System.Collections.Generic.List[hashtable]]::new()
    tag_fallbacks = [System.Collections.Generic.List[hashtable]]::new()
    filtered_self_follows = [System.Collections.Generic.List[hashtable]]::new()
    filtered_null_follows = [System.Collections.Generic.List[hashtable]]::new()
    filtered_non_user_follows = [System.Collections.Generic.List[hashtable]]::new()
}

$extendByUserId = @{}
foreach ($row in $userExtends) {
    $extendByUserId[[string]$row["id_user"]] = $row
}

$articleContentById = @{}
foreach ($row in $articleContents) {
    $articleContentById[[string]$row["id_article"]] = $row
}

$emailGroups = @{}
foreach ($row in $users) {
    $email = Null-IfBlank $row["email"]
    if ($null -eq $email) {
        continue
    }
    if (-not $emailGroups.ContainsKey($email)) {
        $emailGroups[$email] = [System.Collections.Generic.List[long]]::new()
    }
    $emailGroups[$email].Add([long]$row["id"])
}

$emailKeepIdByValue = @{}
foreach ($email in $emailGroups.Keys) {
    $ids = @($emailGroups[$email] | Sort-Object)
    $emailKeepIdByValue[$email] = $ids[0]
    if ($ids.Count -gt 1) {
        for ($i = 1; $i -lt $ids.Count; $i++) {
            $anomalies.email_conflicts.Add([ordered]@{
                email = $email
                kept_member_id = $ids[0]
                cleared_member_id = $ids[$i]
            })
        }
    }
}

$members = [System.Collections.Generic.List[hashtable]]::new()
$communityProfiles = [System.Collections.Generic.List[hashtable]]::new()
$communityStats = [System.Collections.Generic.List[hashtable]]::new()
$memberIds = @{}

Write-Stage "转换会员、社区档案与统计"
foreach ($user in ($users | Sort-Object { [long]$_["id"] })) {
    $memberId = [long]$user["id"]
    $email = Null-IfBlank $user["email"]
    if ($null -ne $email -and $emailKeepIdByValue[$email] -ne $memberId) {
        $email = $null
    }

    $memberProfile = [ordered]@{
        signature = Null-IfBlank $user["signature"]
        avatarType = Null-IfBlank $user["avatar_type"]
        legacyPasswordResetRequired = $true
    }

    $member = [ordered]@{
        id = $memberId
        username = Null-IfBlank $user["account"]
        email = $email
        phone = $null
        password_hash = $null
        name = Null-IfBlank $user["real_name"]
        nickname = Null-IfBlank $user["nickname"]
        avatar_url = Null-IfBlank $user["avatar_url"]
        gender = Convert-Gender $user["sex"]
        status = Convert-MemberStatus $user["status"]
        member_level = "normal"
        points = 0
        balance = 0.00
        register_source = "forest"
        referrer_id = $null
        last_login_time = To-DateTimeString $user["last_login_time"]
        email_verified_time = $null
        phone_verified_time = $null
        profile = $memberProfile
        preferences = $null
        created_time = To-DateTimeString $user["created_time"]
        updated_time = To-DateTimeString $user["updated_time"]
        current_family_id = $null
        del_flag = 0
    }
    $members.Add($member)
    $memberIds[[string]$memberId] = $true

    $extend = Get-Value -Map $extendByUserId -Key ([string]$memberId)
    $socialLinks = [ordered]@{}
    if ($null -ne $extend) {
        foreach ($name in @("github", "weibo", "weixin", "qq", "blog")) {
            $value = Null-IfBlank $extend[$name]
            if ($null -ne $value) {
                $socialLinks[$name] = $value
            }
        }
    }

    $communityProfileExt = if ($socialLinks.Count -gt 0) { [ordered]@{ socialLinks = $socialLinks } } else { $null }
    $communityProfiles.Add([ordered]@{
        id = $memberId
        user_id = $memberId
        banner_url = Null-IfBlank $user["bg_img_url"]
        ext_data = $communityProfileExt
        created_time = To-DateTimeString $user["created_time"]
        updated_time = To-DateTimeString $user["updated_time"]
        del_flag = 0
    })
}

$tagRows = [System.Collections.Generic.List[hashtable]]::new()
$tagIds = @{}
Write-Stage "转换标签与专题"
foreach ($tag in ($tags | Sort-Object { [long]$_["id"] })) {
    $tagId = [long]$tag["id"]
    $title = Null-IfBlank $tag["tag_title"]
    $slug = Null-IfBlank $tag["tag_uri"]
    if ($null -eq $title) {
        $title = "未命名标签-$tagId"
    }
    if ($null -eq $slug) {
        $slug = "tag-$tagId"
    }
    if ((Null-IfBlank $tag["tag_title"]) -ne $title -or (Null-IfBlank $tag["tag_uri"]) -ne $slug) {
        $anomalies.tag_fallbacks.Add([ordered]@{
            tag_id = $tagId
            title = $title
            slug = $slug
        })
    }

    $tagRows.Add([ordered]@{
        id = $tagId
        title = $title
        slug = $slug
        icon_path = Null-IfBlank $tag["tag_icon_path"]
        description = Null-IfBlank $tag["tag_description"]
        description_html = Null-IfBlank $tag["tag_description_html"]
        sort_no = 0
        status = 1
        view_count = To-Long $tag["tag_view_count"]
        article_count = 0
        ext_data = [ordered]@{
            legacy = [ordered]@{
                tagAd = Null-IfBlank $tag["tag_ad"]
                tagShowSideAd = Null-IfBlank $tag["tag_show_side_ad"]
                tagReservation = Null-IfBlank $tag["tag_reservation"]
            }
        }
        created_time = To-DateTimeString $tag["created_time"]
        updated_time = To-DateTimeString $tag["updated_time"]
        del_flag = 0
    })
    $tagIds[[string]$tagId] = $true
}

$topicRows = [System.Collections.Generic.List[hashtable]]::new()
$topicIds = @{}
foreach ($topic in ($topics | Sort-Object { [long]$_["id"] })) {
    $topicId = [long]$topic["id"]
    $topicRows.Add([ordered]@{
        id = $topicId
        title = (Null-IfBlank $topic["topic_title"])
        slug = (Null-IfBlank $topic["topic_uri"])
        icon_path = (Null-IfBlank $topic["topic_icon_path"])
        description = (Null-IfBlank $topic["topic_description"])
        description_html = (Null-IfBlank $topic["topic_description_html"])
        sort_no = To-Int $topic["topic_sort"] 0
        is_nav = To-Flag $topic["topic_nva"] 0
        status = 1
        tag_count = 0
        ext_data = [ordered]@{
            legacy = [ordered]@{
                topicType = Null-IfBlank $topic["topic_type"]
            }
        }
        created_time = To-DateTimeString $topic["created_time"]
        updated_time = To-DateTimeString $topic["updated_time"]
        del_flag = 0
    })
    $topicIds[[string]$topicId] = $true
}

$articleRows = [System.Collections.Generic.List[hashtable]]::new()
$articleIds = @{}
Write-Stage "转换文章"
foreach ($article in ($articles | Sort-Object { [long]$_["id"] })) {
    $articleId = [long]$article["id"]
    $authorId = To-Long $article["article_author_id"]
    if ($null -eq $authorId -or -not $memberIds.ContainsKey([string]$authorId)) {
        $anomalies.invalid_articles.Add([ordered]@{
            article_id = $articleId
            reason = "作者不存在"
            author_id = $authorId
        })
        continue
    }

    $contentRow = Get-Value -Map $articleContentById -Key ([string]$articleId)
    if ($null -eq $contentRow) {
        $anomalies.invalid_articles.Add([ordered]@{
            article_id = $articleId
            reason = "正文缺失"
            author_id = $authorId
        })
        continue
    }

    $status = Convert-ArticleStatus $article["article_status"]
    $createdTime = To-DateTimeString $article["created_time"]
    $updatedTime = To-DateTimeString $article["updated_time"]
    $articleRows.Add([ordered]@{
        id = $articleId
        title = Null-IfBlank $article["article_title"]
        slug = [string]$articleId
        summary = Null-IfBlank $article["article_preview_content"]
        cover_image_url = Null-IfBlank $article["article_thumbnail_url"]
        content_markdown = (Null-IfBlank $contentRow["article_content"]) ?? ''
        content_html = Null-IfBlank $contentRow["article_content_html"]
        author_id = $authorId
        status = $status
        visibility = 0
        allow_comment = 1
        is_pinned = 0
        is_featured = To-Flag $article["article_perfect"] 0
        view_count = To-Long $article["article_view_count"]
        like_count = To-Long $article["article_thumbs_up_count"]
        comment_count = 0
        ext_data = [ordered]@{
            legacy = [ordered]@{
                articleLink = Null-IfBlank $article["article_link"]
                articlePermalink = Null-IfBlank $article["article_permalink"]
                articleType = Null-IfBlank $article["article_type"]
                articleTags = Null-IfBlank $article["article_tags"]
                articleSponsorCount = To-Int $article["article_sponsor_count"] 0
            }
        }
        published_time = if ($status -eq 1) { $createdTime } else { $null }
        created_time = $createdTime
        updated_time = $updatedTime
        del_flag = 0
    })
    $articleIds[[string]$articleId] = $true
}

$articleTagRows = [System.Collections.Generic.List[hashtable]]::new()
$articleTagSeen = @{}
foreach ($relation in ($tagArticles | Sort-Object { [long]$_["id"] })) {
    $articleId = To-Long $relation["id_article"]
    $tagId = To-Long $relation["id_tag"]
    $reason = $null
    if ($null -eq $tagId -or -not $tagIds.ContainsKey([string]$tagId)) {
        $reason = "标签不存在"
    } elseif ($null -eq $articleId -or -not $articleIds.ContainsKey([string]$articleId)) {
        $reason = "文章不存在"
    }

    if ($null -ne $reason) {
        $anomalies.filtered_tag_article_relations.Add([ordered]@{
            relation_id = To-Long $relation["id"]
            tag_id = $tagId
            article_id = $articleId
            reason = $reason
        })
        continue
    }

    $uniqueKey = "$articleId::$tagId"
    if ($articleTagSeen.ContainsKey($uniqueKey)) {
        continue
    }
    $articleTagSeen[$uniqueKey] = $true

    $articleTagRows.Add([ordered]@{
        id = To-Long $relation["id"]
        article_id = $articleId
        tag_id = $tagId
        created_time = To-DateTimeString $relation["created_time"]
    })
}

$topicTagRows = [System.Collections.Generic.List[hashtable]]::new()
$topicTagSeen = @{}
foreach ($relation in ($topicTags | Sort-Object { [long]$_["id"] })) {
    $topicId = To-Long $relation["id_topic"]
    $tagId = To-Long $relation["id_tag"]
    $reason = $null
    if ($null -eq $topicId -or -not $topicIds.ContainsKey([string]$topicId)) {
        $reason = "专题不存在"
    } elseif ($null -eq $tagId -or -not $tagIds.ContainsKey([string]$tagId)) {
        $reason = "标签不存在"
    }

    if ($null -ne $reason) {
        $anomalies.filtered_topic_tag_relations.Add([ordered]@{
            relation_id = To-Long $relation["id"]
            topic_id = $topicId
            tag_id = $tagId
            reason = $reason
        })
        continue
    }

    $uniqueKey = "$topicId::$tagId"
    if ($topicTagSeen.ContainsKey($uniqueKey)) {
        continue
    }
    $topicTagSeen[$uniqueKey] = $true

    $topicTagRows.Add([ordered]@{
        id = To-Long $relation["id"]
        topic_id = $topicId
        tag_id = $tagId
        created_time = To-DateTimeString $relation["created_time"]
    })
}

$topicIdsByTagId = @{}
foreach ($row in $topicTagRows) {
    $tagId = [string]$row["tag_id"]
    if (-not $topicIdsByTagId.ContainsKey($tagId)) {
        $topicIdsByTagId[$tagId] = [System.Collections.Generic.List[long]]::new()
    }
    $topicIdsByTagId[$tagId].Add([long]$row["topic_id"])
}

$articleTopicRows = [System.Collections.Generic.List[hashtable]]::new()
$nextArticleTopicId = New-DeterministicIdSequence -Seed 810000000000000000
$articleTopicSeen = @{}
foreach ($relation in ($articleTagRows | Sort-Object article_id, tag_id)) {
    $topicList = Get-Value -Map $topicIdsByTagId -Key ([string]$relation["tag_id"])
    if ($null -eq $topicList) {
        continue
    }
    foreach ($topicId in ($topicList | Sort-Object -Unique)) {
        $uniqueKey = "$($relation["article_id"])::$topicId"
        if ($articleTopicSeen.ContainsKey($uniqueKey)) {
            continue
        }
        $articleTopicSeen[$uniqueKey] = $true
        $articleTopicRows.Add([ordered]@{
            id = (& $nextArticleTopicId)
            article_id = $relation["article_id"]
            topic_id = $topicId
            created_time = $relation["created_time"]
        })
    }
}

$commentSourceById = @{}
foreach ($comment in $comments) {
    $commentSourceById[[string]$comment["id"]] = $comment
}

$resolvedCommentById = @{}
$resolvingCommentIds = @{}
function Resolve-CommentRow {
    param([long]$CommentId)

    $key = [string]$CommentId
    if ($resolvedCommentById.ContainsKey($key)) {
        return $resolvedCommentById[$key]
    }
    if ($resolvingCommentIds.ContainsKey($key)) {
        return $null
    }

    $source = Get-Value -Map $commentSourceById -Key $key
    if ($null -eq $source) {
        return $null
    }

    $articleId = To-Long $source["comment_article_id"]
    $authorId = To-Long $source["comment_author_id"]
    if ($null -eq $articleId -or -not $articleIds.ContainsKey([string]$articleId)) {
        $anomalies.invalid_comments.Add([ordered]@{
            comment_id = $CommentId
            reason = "文章不存在"
            article_id = $articleId
        })
        return $null
    }
    if ($null -eq $authorId -or -not $memberIds.ContainsKey([string]$authorId)) {
        $anomalies.invalid_comments.Add([ordered]@{
            comment_id = $CommentId
            reason = "作者不存在"
            author_id = $authorId
        })
        return $null
    }

    $resolvingCommentIds[$key] = $true
    $parentId = To-Long $source["comment_original_comment_id"]
    if ($parentId -eq 0) {
        $parentId = $null
    }

    $rootId = $CommentId
    $path = [string]$CommentId
    $depth = 0
    $replyToUserId = $null

    if ($null -ne $parentId) {
        $parent = Resolve-CommentRow -CommentId $parentId
        if ($null -eq $parent) {
            $resolvingCommentIds.Remove($key) | Out-Null
            $anomalies.invalid_comments.Add([ordered]@{
                comment_id = $CommentId
                reason = "父评论无效"
                parent_id = $parentId
            })
            return $null
        }
        if ($parent["article_id"] -ne $articleId) {
            $resolvingCommentIds.Remove($key) | Out-Null
            $anomalies.invalid_comments.Add([ordered]@{
                comment_id = $CommentId
                reason = "父评论跨文章"
                parent_id = $parentId
                article_id = $articleId
            })
            return $null
        }

        $rootId = $parent["root_id"]
        $path = "$($parent["path"]).$CommentId"
        $depth = [int]$parent["depth"] + 1
        $replyToUserId = $parent["author_id"]
    }

    $resolved = [ordered]@{
        id = $CommentId
        article_id = $articleId
        author_id = $authorId
        parent_id = $parentId
        root_id = $rootId
        path = $path
        depth = $depth
        reply_to_user_id = $replyToUserId
        content = Convert-HtmlToPlainText (Null-IfBlank $source["comment_content"])
        ip = Null-IfBlank $source["comment_ip"]
        user_agent = Null-IfBlank $source["comment_ua"]
        status = 1
        is_anonymous = To-Flag $source["comment_anonymous"] 0
        visibility = 0
        like_count = 0
        ext_data = [ordered]@{
            legacyHtml = Null-IfBlank $source["comment_content"]
            legacySharpUrl = Null-IfBlank $source["comment_sharp_url"]
            legacyReplyCount = To-Int $source["comment_reply_count"] 0
            legacyVisible = Null-IfBlank $source["comment_visible"]
        }
        created_time = To-DateTimeString $source["created_time"]
        updated_time = $null
        del_flag = 0
    }

    $resolvedCommentById[$key] = $resolved
    $resolvingCommentIds.Remove($key) | Out-Null
    return $resolved
}

$commentRows = [System.Collections.Generic.List[hashtable]]::new()
Write-Stage "转换评论树"
foreach ($comment in ($comments | Sort-Object { [long]$_["id"] })) {
    $resolved = Resolve-CommentRow -CommentId ([long]$comment["id"])
    if ($null -ne $resolved) {
        $commentRows.Add($resolved)
    }
}

$articleCommentCountById = @{}
$commentCountByAuthorId = @{}
foreach ($comment in $commentRows) {
    $articleKey = [string]$comment["article_id"]
    if (-not $articleCommentCountById.ContainsKey($articleKey)) {
        $articleCommentCountById[$articleKey] = 0
    }
    $articleCommentCountById[$articleKey]++

    $authorKey = [string]$comment["author_id"]
    if (-not $commentCountByAuthorId.ContainsKey($authorKey)) {
        $commentCountByAuthorId[$authorKey] = 0
    }
    $commentCountByAuthorId[$authorKey]++
}

$articleCountByAuthorId = @{}
foreach ($article in $articleRows) {
    $articleKey = [string]$article["id"]
    $article["comment_count"] = if ($articleCommentCountById.ContainsKey($articleKey)) { $articleCommentCountById[$articleKey] } else { 0 }

    # 仅统计已发布文章 (status=1)
    if ($article["status"] -eq 1) {
        $authorKey = [string]$article["author_id"]
        if (-not $articleCountByAuthorId.ContainsKey($authorKey)) {
            $articleCountByAuthorId[$authorKey] = 0
        }
        $articleCountByAuthorId[$authorKey]++
    }
}

$tagArticleIds = @{}
foreach ($relation in $articleTagRows) {
    $key = [string]$relation["tag_id"]
    if (-not $tagArticleIds.ContainsKey($key)) {
        $tagArticleIds[$key] = @{}
    }
    $tagArticleIds[$key][[string]$relation["article_id"]] = $true
}
foreach ($tagRow in $tagRows) {
    $key = [string]$tagRow["id"]
    $tagRow["article_count"] = if ($tagArticleIds.ContainsKey($key)) { $tagArticleIds[$key].Count } else { 0 }
}

$topicTagIds = @{}
foreach ($relation in $topicTagRows) {
    $key = [string]$relation["topic_id"]
    if (-not $topicTagIds.ContainsKey($key)) {
        $topicTagIds[$key] = @{}
    }
    $topicTagIds[$key][[string]$relation["tag_id"]] = $true
}
foreach ($topicRow in $topicRows) {
    $key = [string]$topicRow["id"]
    $topicRow["tag_count"] = if ($topicTagIds.ContainsKey($key)) { $topicTagIds[$key].Count } else { 0 }
}

foreach ($member in $members) {
    $memberId = [string]$member["id"]
    $communityStats.Add([ordered]@{
        id = $member["id"]
        user_id = $member["id"]
        article_count = if ($articleCountByAuthorId.ContainsKey($memberId)) { $articleCountByAuthorId[$memberId] } else { 0 }
        comment_count = if ($commentCountByAuthorId.ContainsKey($memberId)) { $commentCountByAuthorId[$memberId] } else { 0 }
        follower_count = 0
        following_count = 0
        ext_data = $null
        created_time = $member["created_time"]
        updated_time = $member["updated_time"]
        del_flag = 0
    })
}

$collectionRows = [System.Collections.Generic.List[hashtable]]::new()
$collectionMemberRows = [System.Collections.Generic.List[hashtable]]::new()
$collectionIds = @{}
Write-Stage "转换作品集"
foreach ($portfolio in ($portfolios | Sort-Object { [long]$_["id"] })) {
    $collectionId = [long]$portfolio["id"]
    $ownerId = To-Long $portfolio["portfolio_author_id"]
    if ($null -eq $ownerId -or -not $memberIds.ContainsKey([string]$ownerId)) {
        continue
    }

    $createdTime = To-DateTimeString $portfolio["created_time"]
    $updatedTime = To-DateTimeString $portfolio["updated_time"]
    $collectionRows.Add([ordered]@{
        id = $collectionId
        title = Null-IfBlank $portfolio["portfolio_title"]
        slug = "portfolio-$collectionId"
        summary = Null-IfBlank $portfolio["portfolio_description"]
        cover_image_url = Null-IfBlank $portfolio["portfolio_head_img_url"]
        owner_id = $ownerId
        visibility = 0
        status = 1
        ext_data = [ordered]@{
            legacyDescriptionHtml = Null-IfBlank $portfolio["portfolio_description_html"]
        }
        created_time = $createdTime
        updated_time = $updatedTime
        del_flag = 0
    })
    $collectionMemberRows.Add([ordered]@{
        id = $collectionId
        collection_id = $collectionId
        user_id = $ownerId
        role = 0
        status = 1
        invited_by = $null
        joined_time = $createdTime
        created_time = $createdTime
        updated_time = $updatedTime
    })
    $collectionIds[[string]$collectionId] = $true
}

$collectionById = @{}
foreach ($row in $collectionRows) {
    $collectionById[[string]$row["id"]] = $row
}

$collectionArticleRows = [System.Collections.Generic.List[hashtable]]::new()
$collectionArticleSeen = @{}
foreach ($relation in ($portfolioArticles | Sort-Object { [long]$_["id"] })) {
    $collectionId = To-Long $relation["id_portfolio"]
    $articleId = To-Long $relation["id_article"]
    $reason = $null
    if ($null -eq $collectionId -or -not $collectionIds.ContainsKey([string]$collectionId)) {
        $reason = "作品集不存在"
    } elseif ($null -eq $articleId -or -not $articleIds.ContainsKey([string]$articleId)) {
        $reason = "文章不存在"
    }

    if ($null -ne $reason) {
        $anomalies.invalid_collection_articles.Add([ordered]@{
            relation_id = To-Long $relation["id"]
            collection_id = $collectionId
            article_id = $articleId
            reason = $reason
        })
        continue
    }

    $uniqueKey = "$collectionId::$articleId"
    if ($collectionArticleSeen.ContainsKey($uniqueKey)) {
        continue
    }
    $collectionArticleSeen[$uniqueKey] = $true

    $collection = $collectionById[[string]$collectionId]
    $collectionArticleRows.Add([ordered]@{
        id = To-Long $relation["id"]
        collection_id = $collectionId
        article_id = $articleId
        sort_no = To-Int $relation["sort_no"] 0
        note = $null
        added_by = $collection["owner_id"]
        created_time = $collection["created_time"]
        updated_time = $collection["updated_time"]
    })
}

# ==================== 第二批：关注关系 ====================

$followRelationRows = [System.Collections.Generic.List[hashtable]]::new()
$followSeen = @{}
$migrationTimestamp = [datetime]::UtcNow.ToString("yyyy-MM-dd HH:mm:ss")
Write-Stage "转换关注关系"
foreach ($follow in ($follows | Sort-Object { [long]$_["id"] })) {
    $followId = To-Long $follow["id"]
    $followerId = To-Long $follow["follower_id"]
    $followingId = To-Long $follow["following_id"]
    $followingType = Null-IfBlank $follow["following_type"]

    # 过滤非用户关注
    if ($followingType -ne "0") {
        $anomalies.filtered_non_user_follows.Add([ordered]@{
            id = $followId; follower_id = $followerId; following_id = $followingId; following_type = $followingType
        })
        continue
    }

    # 过滤 NULL following_id
    if ($null -eq $followingId) {
        $anomalies.filtered_null_follows.Add([ordered]@{
            id = $followId; follower_id = $followerId; following_id = $null
        })
        continue
    }

    # 过滤自关注
    if ($followerId -eq $followingId) {
        $anomalies.filtered_self_follows.Add([ordered]@{
            id = $followId; follower_id = $followerId; following_id = $followingId
        })
        continue
    }

    # 过滤不存在的用户
    if (-not $memberIds.ContainsKey([string]$followerId) -or -not $memberIds.ContainsKey([string]$followingId)) {
        continue
    }

    $uniqueKey = "$followerId::$followingId"
    if ($followSeen.ContainsKey($uniqueKey)) {
        continue
    }
    $followSeen[$uniqueKey] = $true

    $followRelationRows.Add([ordered]@{
        id = $followId
        follower_user_id = $followerId
        following_user_id = $followingId
        status = 1
        ext_data = $null
        created_time = $migrationTimestamp
        updated_time = $null
        del_flag = 0
    })
}
Write-Host "  有效关注关系: $($followRelationRows.Count) 行 (过滤自关注 $($anomalies.filtered_self_follows.Count), NULL $($anomalies.filtered_null_follows.Count), 非用户类型 $($anomalies.filtered_non_user_follows.Count))" -ForegroundColor DarkGray

# 回刷 communityStats 的 follower_count / following_count
$followerCountByUser = @{}
$followingCountByUser = @{}
foreach ($rel in $followRelationRows) {
    $followerKey = [string]$rel["follower_user_id"]
    $followingKey = [string]$rel["following_user_id"]
    if (-not $followingCountByUser.ContainsKey($followerKey)) { $followingCountByUser[$followerKey] = 0 }
    $followingCountByUser[$followerKey]++
    if (-not $followerCountByUser.ContainsKey($followingKey)) { $followerCountByUser[$followingKey] = 0 }
    $followerCountByUser[$followingKey]++
}
foreach ($stat in $communityStats) {
    $userId = [string]$stat["user_id"]
    $stat["follower_count"] = if ($followerCountByUser.ContainsKey($userId)) { $followerCountByUser[$userId] } else { 0 }
    $stat["following_count"] = if ($followingCountByUser.ContainsKey($userId)) { $followingCountByUser[$userId] } else { 0 }
}

# ==================== 第二批：产品 ====================

$productContentById = @{}
foreach ($row in $productContents) {
    $productContentById[[string]$row["id_product"]] = $row
}

$productRows = [System.Collections.Generic.List[hashtable]]::new()
Write-Stage "转换产品"
foreach ($product in ($products | Sort-Object { [long]$_["id"] })) {
    $productId = [long]$product["id"]
    $contentRow = Get-Value -Map $productContentById -Key ([string]$productId)
    $createdTime = To-DateTimeString $product["created_time"]
    $updatedTime = To-DateTimeString $product["updated_time"]

    # tags: 逗号分隔 → PostgreSQL 数组
    $tagsRaw = Null-IfBlank $product["tags"]
    $tagsArray = $null
    if ($null -ne $tagsRaw) {
        $tagsArray = @($tagsRaw.Split(",") | ForEach-Object { $_.Trim() } | Where-Object { $_ -ne "" })
    }

    $extData = [ordered]@{
        legacy = [ordered]@{
            priceInCents = To-Int $product["product_price"] 0
        }
    }
    if ($null -ne $contentRow) {
        $html = Null-IfBlank $contentRow["product_content_html"]
        if ($null -ne $html) {
            $extData.legacy["contentHtml"] = $html
        }
    }

    $productRows.Add([ordered]@{
        id = $productId
        title = Null-IfBlank $product["product_title"]
        subtitle = $null
        description = if ($null -ne $contentRow) { (Null-IfBlank $contentRow["product_content"]) ?? '' } else { '' }
        short_description = Null-IfBlank $product["product_description"]
        cover_image_url = Null-IfBlank $product["product_img_url"]
        gallery_images = $null
        product_type = "hardware"
        category_id = $null
        tags = $tagsArray
        features = $null
        specifications = $extData
        seo_title = $null
        seo_description = $null
        seo_keywords = $null
        status = 1
        is_featured = $false
        sort_no = To-Int $product["weights"] 0
        created_by = $null
        created_time = $createdTime
        updated_time = $updatedTime
        published_time = $createdTime
        product_code = "PRD-" + ([string]$productId).PadLeft(6, '0')
        del_flag = 0
    })
}
Write-Host "  产品: $($productRows.Count) 行" -ForegroundColor DarkGray

$mappingReport = @'
# Forest -> Mortise 迁移字段映射

## 执行摘要

- 旧库抽取: `$SourceSsh` / `$SourceDatabase`
- 目标库写入: `$TargetSsh` / `$TargetDatabase`
- 目标 schema: `$TargetSchema`
- legacy schema: `$TargetLegacySchema`
- 已完成 Phase 0: `mortise.flyway_schema_history` 存在，Flyway 已推进到 `v414`

## 会员

- `forest_user.id -> mortise_member.id`
- `account -> username`
- `real_name -> name`
- `nickname -> nickname`
- `avatar_url -> avatar_url`
- `email -> email`
- `status: 0 -> 1, 1 -> 0`
- `sex: 1 -> male, 2 -> female, 其他 -> other`
- `signature/avatar_type/legacyPasswordResetRequired -> profile`
- `password_hash = NULL`

## 社区资料

- `forest_user.bg_img_url -> mortise_community_profile.banner_url`
- `forest_user_extend.github/weibo/weixin/qq/blog -> ext_data.socialLinks`
- `mortise_community_user_stat.article_count/comment_count` 由迁移后的文章/评论聚合回填

## 文章

- `forest_article.id -> mortise_article.id`
- `article_title -> title`
- `article_preview_content -> summary`
- `article_thumbnail_url -> cover_image_url`
- `forest_article_content.article_content -> content_markdown`
- `forest_article_content.article_content_html -> content_html`
- `article_author_id -> author_id`
- `slug = cast(id as text)`
- `article_status: 0 -> PUBLISHED(1), 1 -> DRAFT(0)`
- `article_link/article_permalink/article_type/article_sponsor_count -> ext_data.legacy`

## 评论

- `forest_comment.id -> mortise_comment.id`
- `comment_article_id -> article_id`
- `comment_author_id -> author_id`
- `comment_original_comment_id -> parent_id`
- `root_id/depth/path` 由迁移脚本递归生成
- `comment_content(HTML) -> content(纯文本)`，原 HTML 保留到 `ext_data.legacyHtml`

## 作品集

- `forest_portfolio -> mortise_collection`
- `forest_portfolio_article -> mortise_collection_article`
- 每个合集补一条 `mortise_collection_member`，角色 `OWNER(0)`，状态 `ACTIVE(1)`

## 标签 / 专题

- `forest_tag -> mortise_tag`
- `forest_topic -> mortise_topic`
- `forest_tag_article -> mortise_article_tag`
- `forest_topic_tag -> mortise_topic_tag`
- `mortise_article_topic` 由有效 `article-tag` 与 `topic-tag` 关系推导

## 关注关系

- `forest_follow` (type=0) -> `mortise_community_follow_relation`
- `follower_id -> follower_user_id`
- `following_id -> following_user_id`
- `status = 1`, `del_flag = 0`
- 无 `created_time` → 使用迁移时间戳
- 过滤: 自关注、NULL following_id、非用户类型
- 导入后回刷 `mortise_community_user_stat.follower_count / following_count`

## 产品

- `forest_product` + `forest_product_content` -> `mortise_product`
- `product_title -> title`
- `product_description -> short_description`
- `product_content -> description`
- `product_img_url -> cover_image_url`
- `weights -> sort_no`
- `tags` 逗号分隔 -> PostgreSQL `VARCHAR[]`
- `product_type = 'hardware'`
- `product_code = 'PRD-' + LPAD(id, 6, '0')`
- `product_price / product_content_html -> specifications.legacy`
'@
Set-Content -Path (Join-Path $reportDir "field-mapping.md") -Value $mappingReport -Encoding utf8

Save-JsonFile -Path (Join-Path $reportDir "anomalies.json") -Value $anomalies

Write-Stage "生成 legacy 装载 SQL"
$legacyBuilder = [System.Text.StringBuilder]::new()
[void]$legacyBuilder.AppendLine("BEGIN;")
[void]$legacyBuilder.AppendLine("CREATE SCHEMA IF NOT EXISTS $TargetLegacySchema;")
foreach ($tableName in $extractDefinitions.Keys) {
    [void]$legacyBuilder.AppendLine("CREATE TABLE IF NOT EXISTS $TargetLegacySchema.$tableName (source_key TEXT PRIMARY KEY, payload JSONB NOT NULL, extracted_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);")
    [void]$legacyBuilder.AppendLine("TRUNCATE TABLE $TargetLegacySchema.$tableName;")
    [void]$legacyBuilder.AppendLine()

    $legacyRows = [System.Collections.Generic.List[hashtable]]::new()
    foreach ($row in $sourceData[$tableName]) {
        $legacyRows.Add([ordered]@{
            source_key = (New-LegacyKey -TableName $tableName -Row $row)
            payload = $row
        })
    }
    Add-InsertSql -Builder $legacyBuilder -TableName "$TargetLegacySchema.$tableName" -Columns @("source_key", "payload") -Rows $legacyRows
}
[void]$legacyBuilder.AppendLine("COMMIT;")
$legacySqlPath = Join-Path $sqlDir "load-legacy.sql"
Set-Content -Path $legacySqlPath -Value $legacyBuilder.ToString() -Encoding utf8

Write-Stage "生成正式表导入 SQL"
$loadBuilder = [System.Text.StringBuilder]::new()
[void]$loadBuilder.AppendLine("BEGIN;")
[void]$loadBuilder.AppendLine("TRUNCATE TABLE")
[void]$loadBuilder.AppendLine("    mortise.mortise_product,")
[void]$loadBuilder.AppendLine("    mortise.mortise_collection_article,")
[void]$loadBuilder.AppendLine("    mortise.mortise_collection_member,")
[void]$loadBuilder.AppendLine("    mortise.mortise_collection,")
[void]$loadBuilder.AppendLine("    mortise.mortise_comment,")
[void]$loadBuilder.AppendLine("    mortise.mortise_article_product,")
[void]$loadBuilder.AppendLine("    mortise.mortise_article_topic,")
[void]$loadBuilder.AppendLine("    mortise.mortise_topic_tag,")
[void]$loadBuilder.AppendLine("    mortise.mortise_article_tag,")
[void]$loadBuilder.AppendLine("    mortise.mortise_topic,")
[void]$loadBuilder.AppendLine("    mortise.mortise_tag,")
[void]$loadBuilder.AppendLine("    mortise.mortise_community_follow_relation,")
[void]$loadBuilder.AppendLine("    mortise.mortise_community_user_stat,")
[void]$loadBuilder.AppendLine("    mortise.mortise_community_profile,")
[void]$loadBuilder.AppendLine("    mortise.mortise_article,")
[void]$loadBuilder.AppendLine("    mortise.mortise_member_oauth2_binding,")
[void]$loadBuilder.AppendLine("    mortise.mortise_member_point_history,")
[void]$loadBuilder.AppendLine("    mortise.mortise_member RESTART IDENTITY CASCADE;")
[void]$loadBuilder.AppendLine()

Add-InsertSql -Builder $loadBuilder -TableName "mortise.mortise_member" -Columns @(
    "id", "username", "email", "phone", "password_hash", "name", "nickname", "avatar_url",
    "gender", "status", "member_level", "points", "balance", "register_source", "referrer_id",
    "last_login_time", "email_verified_time", "phone_verified_time", "profile", "preferences",
    "created_time", "updated_time", "current_family_id", "del_flag"
) -Rows $members

Add-InsertSql -Builder $loadBuilder -TableName "mortise.mortise_community_profile" -Columns @(
    "id", "user_id", "banner_url", "ext_data", "created_time", "updated_time", "del_flag"
) -Rows $communityProfiles

Add-InsertSql -Builder $loadBuilder -TableName "mortise.mortise_community_user_stat" -Columns @(
    "id", "user_id", "article_count", "comment_count", "follower_count", "following_count",
    "ext_data", "created_time", "updated_time", "del_flag"
) -Rows $communityStats

Add-InsertSql -Builder $loadBuilder -TableName "mortise.mortise_article" -Columns @(
    "id", "title", "slug", "summary", "cover_image_url", "content_markdown", "content_html",
    "author_id", "status", "visibility", "allow_comment", "is_pinned", "is_featured",
    "view_count", "like_count", "comment_count", "ext_data", "published_time", "created_time",
    "updated_time", "del_flag"
) -Rows $articleRows

Add-InsertSql -Builder $loadBuilder -TableName "mortise.mortise_tag" -Columns @(
    "id", "title", "slug", "icon_path", "description", "description_html", "sort_no",
    "status", "view_count", "article_count", "ext_data", "created_time", "updated_time", "del_flag"
) -Rows $tagRows

Add-InsertSql -Builder $loadBuilder -TableName "mortise.mortise_topic" -Columns @(
    "id", "title", "slug", "icon_path", "description", "description_html", "sort_no", "is_nav",
    "status", "tag_count", "ext_data", "created_time", "updated_time", "del_flag"
) -Rows $topicRows

Add-InsertSql -Builder $loadBuilder -TableName "mortise.mortise_article_tag" -Columns @(
    "id", "article_id", "tag_id", "created_time"
) -Rows $articleTagRows

Add-InsertSql -Builder $loadBuilder -TableName "mortise.mortise_topic_tag" -Columns @(
    "id", "topic_id", "tag_id", "created_time"
) -Rows $topicTagRows

Add-InsertSql -Builder $loadBuilder -TableName "mortise.mortise_article_topic" -Columns @(
    "id", "article_id", "topic_id", "created_time"
) -Rows $articleTopicRows

Add-InsertSql -Builder $loadBuilder -TableName "mortise.mortise_comment" -Columns @(
    "id", "article_id", "author_id", "parent_id", "root_id", "path", "depth", "reply_to_user_id",
    "content", "ip", "user_agent", "status", "is_anonymous", "visibility", "like_count",
    "ext_data", "created_time", "updated_time", "del_flag"
) -Rows $commentRows

Add-InsertSql -Builder $loadBuilder -TableName "mortise.mortise_collection" -Columns @(
    "id", "title", "slug", "summary", "cover_image_url", "owner_id", "visibility", "status",
    "ext_data", "created_time", "updated_time", "del_flag"
) -Rows $collectionRows

Add-InsertSql -Builder $loadBuilder -TableName "mortise.mortise_collection_member" -Columns @(
    "id", "collection_id", "user_id", "role", "status", "invited_by", "joined_time", "created_time", "updated_time"
) -Rows $collectionMemberRows

Add-InsertSql -Builder $loadBuilder -TableName "mortise.mortise_collection_article" -Columns @(
    "id", "collection_id", "article_id", "sort_no", "note", "added_by", "created_time", "updated_time"
) -Rows $collectionArticleRows

Add-InsertSql -Builder $loadBuilder -TableName "mortise.mortise_community_follow_relation" -Columns @(
    "id", "follower_user_id", "following_user_id", "status", "ext_data", "created_time", "updated_time", "del_flag"
) -Rows $followRelationRows

Add-InsertSql -Builder $loadBuilder -TableName "mortise.mortise_product" -Columns @(
    "id", "title", "subtitle", "description", "short_description", "cover_image_url", "gallery_images",
    "product_type", "category_id", "tags", "features", "specifications",
    "seo_title", "seo_description", "seo_keywords",
    "status", "is_featured", "sort_no", "created_by", "created_time", "updated_time", "published_time",
    "product_code", "del_flag"
) -Rows $productRows

[void]$loadBuilder.AppendLine("SELECT setval('mortise.seq_topic_tag_id', COALESCE((SELECT MAX(id) FROM mortise.mortise_topic_tag), 1), true);")
[void]$loadBuilder.AppendLine("COMMIT;")

$loadSqlPath = Join-Path $sqlDir "load-mortise.sql"
Set-Content -Path $loadSqlPath -Value $loadBuilder.ToString() -Encoding utf8

if (-not $SkipExecution) {
    Write-Stage "装载 legacy schema"
    Invoke-TargetPostgres -Sql (Get-Content -Path $legacySqlPath -Raw) | Out-Null

    Write-Stage "写入正式表"
    Invoke-TargetPostgres -Sql (Get-Content -Path $loadSqlPath -Raw) | Out-Null
}

Write-Stage "执行校验"
$expectedCounts = [ordered]@{
    mortise_member = $members.Count
    mortise_community_profile = $communityProfiles.Count
    mortise_community_user_stat = $communityStats.Count
    mortise_article = $articleRows.Count
    mortise_tag = $tagRows.Count
    mortise_topic = $topicRows.Count
    mortise_article_tag = $articleTagRows.Count
    mortise_topic_tag = $topicTagRows.Count
    mortise_article_topic = $articleTopicRows.Count
    mortise_comment = $commentRows.Count
    mortise_collection = $collectionRows.Count
    mortise_collection_member = $collectionMemberRows.Count
    mortise_collection_article = $collectionArticleRows.Count
    mortise_community_follow_relation = $followRelationRows.Count
    mortise_product = $productRows.Count
}
$expectedSourceCounts = [ordered]@{
    forest_user = $users.Count
    forest_user_extend = $userExtends.Count
    forest_article = $articles.Count
    forest_article_content = $articleContents.Count
    forest_comment = $comments.Count
    forest_portfolio = $portfolios.Count
    forest_portfolio_article = $portfolioArticles.Count
    forest_tag = $tags.Count
    forest_tag_article = $tagArticles.Count
    forest_topic = $topics.Count
    forest_topic_tag = $topicTags.Count
    forest_follow = $follows.Count
    forest_product = $products.Count
    forest_product_content = $productContents.Count
}

$overallPass = $true
$countResults = [ordered]@{}
$legacyCountResults = [ordered]@{}
$orphanResults = [ordered]@{}
$fieldCheckResults = [ordered]@{}
$sampleData = [ordered]@{}

if (-not $SkipExecution) {
    # --- 1. 目标表行数 ---
    Write-Host "  校验目标表行数..." -ForegroundColor DarkGray
    $countSql = @"
SELECT 'mortise_member', COUNT(*)::text FROM $TargetSchema.mortise_member
UNION ALL SELECT 'mortise_community_profile', COUNT(*)::text FROM $TargetSchema.mortise_community_profile
UNION ALL SELECT 'mortise_community_user_stat', COUNT(*)::text FROM $TargetSchema.mortise_community_user_stat
UNION ALL SELECT 'mortise_article', COUNT(*)::text FROM $TargetSchema.mortise_article
UNION ALL SELECT 'mortise_tag', COUNT(*)::text FROM $TargetSchema.mortise_tag
UNION ALL SELECT 'mortise_topic', COUNT(*)::text FROM $TargetSchema.mortise_topic
UNION ALL SELECT 'mortise_article_tag', COUNT(*)::text FROM $TargetSchema.mortise_article_tag
UNION ALL SELECT 'mortise_topic_tag', COUNT(*)::text FROM $TargetSchema.mortise_topic_tag
UNION ALL SELECT 'mortise_article_topic', COUNT(*)::text FROM $TargetSchema.mortise_article_topic
UNION ALL SELECT 'mortise_comment', COUNT(*)::text FROM $TargetSchema.mortise_comment
UNION ALL SELECT 'mortise_collection', COUNT(*)::text FROM $TargetSchema.mortise_collection
UNION ALL SELECT 'mortise_collection_member', COUNT(*)::text FROM $TargetSchema.mortise_collection_member
UNION ALL SELECT 'mortise_collection_article', COUNT(*)::text FROM $TargetSchema.mortise_collection_article
UNION ALL SELECT 'mortise_community_follow_relation', COUNT(*)::text FROM $TargetSchema.mortise_community_follow_relation
UNION ALL SELECT 'mortise_product', COUNT(*)::text FROM $TargetSchema.mortise_product;
"@
    $countRows = Invoke-TargetPostgres -Sql $countSql -AsQuery
    $actualCounts = @{}
    foreach ($line in $countRows) {
        if ([string]::IsNullOrWhiteSpace($line)) { continue }
        $parts = $line.Split("|", 2)
        $actualCounts[$parts[0].Trim()] = [int]$parts[1].Trim()
    }
    foreach ($table in $expectedCounts.Keys) {
        $expected = $expectedCounts[$table]
        $actual = if ($actualCounts.ContainsKey($table)) { $actualCounts[$table] } else { -1 }
        $ok = $expected -eq $actual
        $countResults[$table] = [ordered]@{ expected = $expected; actual = $actual; pass = $ok }
        if (-not $ok) { $overallPass = $false }
    }

    # --- 2. Legacy 装载验证 ---
    Write-Host "  校验 Legacy 装载..." -ForegroundColor DarkGray
    $legacyCountSql = @"
SELECT 'forest_user', COUNT(*)::text FROM $TargetLegacySchema.forest_user
UNION ALL SELECT 'forest_user_extend', COUNT(*)::text FROM $TargetLegacySchema.forest_user_extend
UNION ALL SELECT 'forest_article', COUNT(*)::text FROM $TargetLegacySchema.forest_article
UNION ALL SELECT 'forest_article_content', COUNT(*)::text FROM $TargetLegacySchema.forest_article_content
UNION ALL SELECT 'forest_comment', COUNT(*)::text FROM $TargetLegacySchema.forest_comment
UNION ALL SELECT 'forest_portfolio', COUNT(*)::text FROM $TargetLegacySchema.forest_portfolio
UNION ALL SELECT 'forest_portfolio_article', COUNT(*)::text FROM $TargetLegacySchema.forest_portfolio_article
UNION ALL SELECT 'forest_tag', COUNT(*)::text FROM $TargetLegacySchema.forest_tag
UNION ALL SELECT 'forest_tag_article', COUNT(*)::text FROM $TargetLegacySchema.forest_tag_article
UNION ALL SELECT 'forest_topic', COUNT(*)::text FROM $TargetLegacySchema.forest_topic
UNION ALL SELECT 'forest_topic_tag', COUNT(*)::text FROM $TargetLegacySchema.forest_topic_tag
UNION ALL SELECT 'forest_follow', COUNT(*)::text FROM $TargetLegacySchema.forest_follow
UNION ALL SELECT 'forest_product', COUNT(*)::text FROM $TargetLegacySchema.forest_product
UNION ALL SELECT 'forest_product_content', COUNT(*)::text FROM $TargetLegacySchema.forest_product_content;
"@
    $legacyCountRows = Invoke-TargetPostgres -Sql $legacyCountSql -AsQuery
    $legacyCounts = @{}
    foreach ($line in $legacyCountRows) {
        if ([string]::IsNullOrWhiteSpace($line)) { continue }
        $parts = $line.Split("|", 2)
        $legacyCounts[$parts[0].Trim()] = [int]$parts[1].Trim()
    }
    foreach ($table in $expectedSourceCounts.Keys) {
        $expected = $expectedSourceCounts[$table]
        $actual = if ($legacyCounts.ContainsKey($table)) { $legacyCounts[$table] } else { -1 }
        $ok = $expected -eq $actual
        $legacyCountResults[$table] = [ordered]@{ expected = $expected; actual = $actual; pass = $ok }
        if (-not $ok) { $overallPass = $false }
    }

    # --- 3. 约束完整性 ---
    Write-Host "  校验约束完整性..." -ForegroundColor DarkGray
    $orphanSql = @"
SELECT 'orphan_article_tag', COUNT(*)::text
FROM $TargetSchema.mortise_article_tag rel
LEFT JOIN $TargetSchema.mortise_article a ON a.id = rel.article_id
LEFT JOIN $TargetSchema.mortise_tag t ON t.id = rel.tag_id
WHERE a.id IS NULL OR t.id IS NULL
UNION ALL
SELECT 'orphan_topic_tag', COUNT(*)::text
FROM $TargetSchema.mortise_topic_tag rel
LEFT JOIN $TargetSchema.mortise_topic tp ON tp.id = rel.topic_id
LEFT JOIN $TargetSchema.mortise_tag t ON t.id = rel.tag_id
WHERE tp.id IS NULL OR t.id IS NULL
UNION ALL
SELECT 'orphan_collection_article', COUNT(*)::text
FROM $TargetSchema.mortise_collection_article rel
LEFT JOIN $TargetSchema.mortise_collection c ON c.id = rel.collection_id
LEFT JOIN $TargetSchema.mortise_article a ON a.id = rel.article_id
WHERE c.id IS NULL OR a.id IS NULL
UNION ALL
SELECT 'orphan_comment_parent', COUNT(*)::text
FROM $TargetSchema.mortise_comment c
LEFT JOIN $TargetSchema.mortise_comment p ON p.id = c.parent_id
WHERE c.parent_id IS NOT NULL AND p.id IS NULL
UNION ALL
SELECT 'orphan_comment_article', COUNT(*)::text
FROM $TargetSchema.mortise_comment c
LEFT JOIN $TargetSchema.mortise_article a ON a.id = c.article_id
WHERE a.id IS NULL
UNION ALL
SELECT 'orphan_comment_author', COUNT(*)::text
FROM $TargetSchema.mortise_comment c
LEFT JOIN $TargetSchema.mortise_member m ON m.id = c.author_id
WHERE m.id IS NULL
UNION ALL
SELECT 'orphan_article_author', COUNT(*)::text
FROM $TargetSchema.mortise_article a
LEFT JOIN $TargetSchema.mortise_member m ON m.id = a.author_id
WHERE m.id IS NULL
UNION ALL
SELECT 'orphan_collection_owner', COUNT(*)::text
FROM $TargetSchema.mortise_collection c
LEFT JOIN $TargetSchema.mortise_member m ON m.id = c.owner_id
WHERE m.id IS NULL
UNION ALL
SELECT 'comment_path_mismatch', COUNT(*)::text
FROM $TargetSchema.mortise_comment c
LEFT JOIN $TargetSchema.mortise_comment p ON p.id = c.parent_id
WHERE (c.parent_id IS NULL AND (c.root_id <> c.id OR c.depth <> 0 OR c.path <> c.id::text))
   OR (c.parent_id IS NOT NULL AND (c.root_id <> p.root_id OR c.depth <> p.depth + 1 OR c.path <> p.path || '.' || c.id::text))
UNION ALL
SELECT 'dup_member_username', (COUNT(*) - COUNT(DISTINCT username))::text FROM $TargetSchema.mortise_member WHERE username IS NOT NULL
UNION ALL
SELECT 'dup_member_email', (COUNT(*) - COUNT(DISTINCT email))::text FROM $TargetSchema.mortise_member WHERE email IS NOT NULL
UNION ALL
SELECT 'orphan_follow_follower', COUNT(*)::text
FROM $TargetSchema.mortise_community_follow_relation f
LEFT JOIN $TargetSchema.mortise_member m ON m.id = f.follower_user_id
WHERE m.id IS NULL
UNION ALL
SELECT 'orphan_follow_following', COUNT(*)::text
FROM $TargetSchema.mortise_community_follow_relation f
LEFT JOIN $TargetSchema.mortise_member m ON m.id = f.following_user_id
WHERE m.id IS NULL
UNION ALL
SELECT 'dup_follow_relation', (COUNT(*) - COUNT(DISTINCT (follower_user_id::text || '::' || following_user_id::text)))::text
FROM $TargetSchema.mortise_community_follow_relation
UNION ALL
SELECT 'self_follow', COUNT(*)::text
FROM $TargetSchema.mortise_community_follow_relation
WHERE follower_user_id = following_user_id
UNION ALL
SELECT 'dup_product_code', (COUNT(*) - COUNT(DISTINCT product_code))::text FROM $TargetSchema.mortise_product
UNION ALL
SELECT 'follow_stat_mismatch', COUNT(*)::text
FROM $TargetSchema.mortise_community_user_stat s
WHERE s.follower_count <> (SELECT COUNT(*) FROM $TargetSchema.mortise_community_follow_relation WHERE following_user_id = s.user_id AND del_flag = 0)
   OR s.following_count <> (SELECT COUNT(*) FROM $TargetSchema.mortise_community_follow_relation WHERE follower_user_id = s.user_id AND del_flag = 0);
"@
    $orphanRows = Invoke-TargetPostgres -Sql $orphanSql -AsQuery
    foreach ($line in $orphanRows) {
        if ([string]::IsNullOrWhiteSpace($line)) { continue }
        $parts = $line.Split("|", 2)
        $name = $parts[0].Trim()
        $count = [int]$parts[1].Trim()
        $ok = $count -eq 0
        $orphanResults[$name] = [ordered]@{ count = $count; pass = $ok }
        if (-not $ok) { $overallPass = $false }
    }

    # --- 4. 字段一致性（聚合比较 legacy vs mortise） ---
    Write-Host "  校验字段一致性..." -ForegroundColor DarkGray
    $fieldCheckSql = @"
SELECT 'member_username', COUNT(*)::text
FROM $TargetSchema.mortise_member m
JOIN $TargetLegacySchema.forest_user l ON l.source_key = m.id::text
WHERE m.username IS DISTINCT FROM TRIM(l.payload->>'account')
UNION ALL
SELECT 'member_nickname', COUNT(*)::text
FROM $TargetSchema.mortise_member m
JOIN $TargetLegacySchema.forest_user l ON l.source_key = m.id::text
WHERE m.nickname IS DISTINCT FROM TRIM(l.payload->>'nickname')
UNION ALL
SELECT 'member_avatar_url', COUNT(*)::text
FROM $TargetSchema.mortise_member m
JOIN $TargetLegacySchema.forest_user l ON l.source_key = m.id::text
WHERE m.avatar_url IS DISTINCT FROM NULLIF(TRIM(l.payload->>'avatar_url'), '')
UNION ALL
SELECT 'article_title', COUNT(*)::text
FROM $TargetSchema.mortise_article a
JOIN $TargetLegacySchema.forest_article l ON l.source_key = a.id::text
WHERE a.title IS DISTINCT FROM TRIM(l.payload->>'article_title')
UNION ALL
SELECT 'article_author', COUNT(*)::text
FROM $TargetSchema.mortise_article a
JOIN $TargetLegacySchema.forest_article l ON l.source_key = a.id::text
WHERE a.author_id::text IS DISTINCT FROM l.payload->>'article_author_id'
UNION ALL
SELECT 'collection_title', COUNT(*)::text
FROM $TargetSchema.mortise_collection c
JOIN $TargetLegacySchema.forest_portfolio l ON l.source_key = c.id::text
WHERE c.title IS DISTINCT FROM TRIM(l.payload->>'portfolio_title')
UNION ALL
SELECT 'collection_owner', COUNT(*)::text
FROM $TargetSchema.mortise_collection c
JOIN $TargetLegacySchema.forest_portfolio l ON l.source_key = c.id::text
WHERE c.owner_id::text IS DISTINCT FROM l.payload->>'portfolio_author_id'
UNION ALL
SELECT 'tag_title', COUNT(*)::text
FROM $TargetSchema.mortise_tag t
JOIN $TargetLegacySchema.forest_tag l ON l.source_key = t.id::text
WHERE t.title IS DISTINCT FROM COALESCE(NULLIF(TRIM(l.payload->>'tag_title'), ''), '未命名标签-' || t.id::text)
UNION ALL
SELECT 'comment_article', COUNT(*)::text
FROM $TargetSchema.mortise_comment c
JOIN $TargetLegacySchema.forest_comment l ON l.source_key = c.id::text
WHERE c.article_id::text IS DISTINCT FROM l.payload->>'comment_article_id'
UNION ALL
SELECT 'comment_author', COUNT(*)::text
FROM $TargetSchema.mortise_comment c
JOIN $TargetLegacySchema.forest_comment l ON l.source_key = c.id::text
WHERE c.author_id::text IS DISTINCT FROM l.payload->>'comment_author_id'
UNION ALL
SELECT 'follow_follower', COUNT(*)::text
FROM $TargetSchema.mortise_community_follow_relation f
JOIN $TargetLegacySchema.forest_follow l ON l.source_key = f.id::text
WHERE f.follower_user_id::text IS DISTINCT FROM l.payload->>'follower_id'
UNION ALL
SELECT 'follow_following', COUNT(*)::text
FROM $TargetSchema.mortise_community_follow_relation f
JOIN $TargetLegacySchema.forest_follow l ON l.source_key = f.id::text
WHERE f.following_user_id::text IS DISTINCT FROM l.payload->>'following_id'
UNION ALL
SELECT 'product_title', COUNT(*)::text
FROM $TargetSchema.mortise_product p
JOIN $TargetLegacySchema.forest_product l ON l.source_key = p.id::text
WHERE p.title IS DISTINCT FROM TRIM(l.payload->>'product_title');
"@
    $fieldCheckRows = Invoke-TargetPostgres -Sql $fieldCheckSql -AsQuery
    foreach ($line in $fieldCheckRows) {
        if ([string]::IsNullOrWhiteSpace($line)) { continue }
        $parts = $line.Split("|", 2)
        $name = $parts[0].Trim()
        $count = [int]$parts[1].Trim()
        $ok = $count -eq 0
        $fieldCheckResults[$name] = [ordered]@{ mismatch_count = $count; pass = $ok }
        if (-not $ok) { $overallPass = $false }
    }

    # --- 5. 抽样验收（保存到 JSON 供人工复核） ---
    Write-Host "  生成抽样数据..." -ForegroundColor DarkGray
    try {
        $sampleMemberSql = @"
SELECT jsonb_build_object(
    'id', m.id, 'username', m.username, 'nickname', m.nickname,
    'avatar_url', m.avatar_url, 'gender', m.gender, 'status', m.status, 'email', m.email,
    'profile_signature', m.profile->>'signature',
    'src_account', l.payload->>'account', 'src_nickname', l.payload->>'nickname',
    'src_avatar_url', l.payload->>'avatar_url', 'src_email', l.payload->>'email',
    'src_signature', l.payload->>'signature',
    'banner_url', cp.banner_url, 'src_bg_img_url', l.payload->>'bg_img_url',
    'social_links', cp.ext_data->'socialLinks'
)::text
FROM $TargetSchema.mortise_member m
JOIN $TargetLegacySchema.forest_user l ON l.source_key = m.id::text
LEFT JOIN $TargetSchema.mortise_community_profile cp ON cp.user_id = m.id
ORDER BY RANDOM() LIMIT 20;
"@
        $sampleMembers = Invoke-TargetPostgres -Sql $sampleMemberSql -AsQuery | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
        $sampleData["members"] = @($sampleMembers | ForEach-Object { $_ | ConvertFrom-Json -AsHashtable })

        $sampleArticleSql = @"
SELECT jsonb_build_object(
    'id', a.id, 'title', a.title, 'status', a.status, 'author_id', a.author_id,
    'slug', a.slug, 'is_featured', a.is_featured, 'view_count', a.view_count,
    'comment_count', a.comment_count,
    'tag_count', (SELECT COUNT(*) FROM $TargetSchema.mortise_article_tag WHERE article_id = a.id),
    'src_title', l.payload->>'article_title', 'src_author_id', l.payload->>'article_author_id',
    'src_status', l.payload->>'article_status', 'src_perfect', l.payload->>'article_perfect',
    'src_view_count', l.payload->>'article_view_count'
)::text
FROM $TargetSchema.mortise_article a
JOIN $TargetLegacySchema.forest_article l ON l.source_key = a.id::text
ORDER BY RANDOM() LIMIT 20;
"@
        $sampleArticles = Invoke-TargetPostgres -Sql $sampleArticleSql -AsQuery | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
        $sampleData["articles"] = @($sampleArticles | ForEach-Object { $_ | ConvertFrom-Json -AsHashtable })

        $sampleCommentSql = @"
SELECT jsonb_build_object(
    'id', c.id, 'article_id', c.article_id, 'author_id', c.author_id,
    'parent_id', c.parent_id, 'root_id', c.root_id, 'depth', c.depth, 'path', c.path,
    'content_preview', LEFT(c.content, 80),
    'src_article_id', l.payload->>'comment_article_id',
    'src_author_id', l.payload->>'comment_author_id',
    'src_parent_id', l.payload->>'comment_original_comment_id',
    'src_content_preview', LEFT(l.payload->>'comment_content', 80)
)::text
FROM $TargetSchema.mortise_comment c
JOIN $TargetLegacySchema.forest_comment l ON l.source_key = c.id::text
ORDER BY RANDOM() LIMIT 20;
"@
        $sampleComments = Invoke-TargetPostgres -Sql $sampleCommentSql -AsQuery | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
        $sampleData["comments"] = @($sampleComments | ForEach-Object { $_ | ConvertFrom-Json -AsHashtable })

        $sampleCollectionSql = @"
SELECT jsonb_build_object(
    'id', c.id, 'title', c.title, 'slug', c.slug, 'owner_id', c.owner_id,
    'cover_image_url', c.cover_image_url,
    'article_count', (SELECT COUNT(*) FROM $TargetSchema.mortise_collection_article WHERE collection_id = c.id),
    'src_title', l.payload->>'portfolio_title',
    'src_author_id', l.payload->>'portfolio_author_id',
    'src_head_img_url', l.payload->>'portfolio_head_img_url'
)::text
FROM $TargetSchema.mortise_collection c
JOIN $TargetLegacySchema.forest_portfolio l ON l.source_key = c.id::text
ORDER BY RANDOM() LIMIT 10;
"@
        $sampleCollections = Invoke-TargetPostgres -Sql $sampleCollectionSql -AsQuery | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
        $sampleData["collections"] = @($sampleCollections | ForEach-Object { $_ | ConvertFrom-Json -AsHashtable })

        $sampleFollowSql = @"
SELECT jsonb_build_object(
    'id', f.id, 'follower_user_id', f.follower_user_id, 'following_user_id', f.following_user_id,
    'status', f.status,
    'follower_nickname', m1.nickname, 'following_nickname', m2.nickname,
    'src_follower_id', l.payload->>'follower_id',
    'src_following_id', l.payload->>'following_id'
)::text
FROM $TargetSchema.mortise_community_follow_relation f
JOIN $TargetSchema.mortise_member m1 ON m1.id = f.follower_user_id
JOIN $TargetSchema.mortise_member m2 ON m2.id = f.following_user_id
JOIN $TargetLegacySchema.forest_follow l ON l.source_key = f.id::text
ORDER BY RANDOM() LIMIT 20;
"@
        $sampleFollows = Invoke-TargetPostgres -Sql $sampleFollowSql -AsQuery | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
        $sampleData["follows"] = @($sampleFollows | ForEach-Object { $_ | ConvertFrom-Json -AsHashtable })

        $sampleProductSql = @"
SELECT jsonb_build_object(
    'id', p.id, 'title', p.title, 'product_code', p.product_code, 'product_type', p.product_type,
    'short_description', p.short_description, 'cover_image_url', p.cover_image_url,
    'tags', p.tags, 'sort_no', p.sort_no, 'status', p.status,
    'src_title', l.payload->>'product_title',
    'src_price', l.payload->>'product_price',
    'src_img_url', l.payload->>'product_img_url'
)::text
FROM $TargetSchema.mortise_product p
JOIN $TargetLegacySchema.forest_product l ON l.source_key = p.id::text
ORDER BY p.id;
"@
        $sampleProducts = Invoke-TargetPostgres -Sql $sampleProductSql -AsQuery | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
        $sampleData["products"] = @($sampleProducts | ForEach-Object { $_ | ConvertFrom-Json -AsHashtable })
    } catch {
        Write-Host "  ⚠️ 抽样查询出错，跳过: $_" -ForegroundColor Yellow
    }
} else {
    Write-Host "  跳过数据库校验（SkipExecution 模式）" -ForegroundColor Yellow
}

$validationReport = [ordered]@{
    overall_pass = $overallPass
    counts = $countResults
    legacy_counts = $legacyCountResults
    orphan_checks = $orphanResults
    field_checks = $fieldCheckResults
    samples = $sampleData
    anomalies = [ordered]@{
        filtered_invalid_articles = $anomalies.invalid_articles.Count
        filtered_invalid_comments = $anomalies.invalid_comments.Count
        filtered_invalid_collection_articles = $anomalies.invalid_collection_articles.Count
        filtered_invalid_tag_article_relations = $anomalies.filtered_tag_article_relations.Count
        filtered_invalid_topic_tag_relations = $anomalies.filtered_topic_tag_relations.Count
        email_conflicts = $anomalies.email_conflicts.Count
        tag_fallbacks = $anomalies.tag_fallbacks.Count
        filtered_self_follows = $anomalies.filtered_self_follows.Count
        filtered_null_follows = $anomalies.filtered_null_follows.Count
        filtered_non_user_follows = $anomalies.filtered_non_user_follows.Count
    }
}
Save-JsonFile -Path (Join-Path $reportDir "validation.json") -Value $validationReport

# --- 生成 Markdown 校验报告 ---
$sb = [System.Text.StringBuilder]::new()
[void]$sb.AppendLine("# Forest → Mortise 迁移校验报告")
[void]$sb.AppendLine()
$overallIcon = if ($overallPass) { "✅ PASS" } else { "❌ FAIL" }
[void]$sb.AppendLine("**整体结果: $overallIcon**")
[void]$sb.AppendLine()

[void]$sb.AppendLine("## 目标表行数")
[void]$sb.AppendLine()
[void]$sb.AppendLine("| 表 | 期望 | 实际 | 结果 |")
[void]$sb.AppendLine("|---|---|---|---|")
foreach ($table in $expectedCounts.Keys) {
    if ($countResults.Contains($table)) {
        $entry = $countResults[$table]
        $icon = if ($entry.pass) { "✅" } else { "❌" }
        [void]$sb.AppendLine("| $table | $($entry.expected) | $($entry.actual) | $icon |")
    } else {
        [void]$sb.AppendLine("| $table | $($expectedCounts[$table]) | - | ⏭️ |")
    }
}
[void]$sb.AppendLine()

if ($legacyCountResults.Count -gt 0) {
    [void]$sb.AppendLine("## Legacy 装载验证")
    [void]$sb.AppendLine()
    [void]$sb.AppendLine("| 源表 | 源库行数 | Legacy 行数 | 结果 |")
    [void]$sb.AppendLine("|---|---|---|---|")
    foreach ($table in $expectedSourceCounts.Keys) {
        if ($legacyCountResults.Contains($table)) {
            $entry = $legacyCountResults[$table]
            $icon = if ($entry.pass) { "✅" } else { "❌" }
            [void]$sb.AppendLine("| $table | $($entry.expected) | $($entry.actual) | $icon |")
        }
    }
    [void]$sb.AppendLine()
}

[void]$sb.AppendLine("## 约束完整性")
[void]$sb.AppendLine()
if ($orphanResults.Count -gt 0) {
    [void]$sb.AppendLine("| 检查项 | 数量 | 结果 |")
    [void]$sb.AppendLine("|---|---|---|")
    foreach ($check in $orphanResults.Keys) {
        $entry = $orphanResults[$check]
        $icon = if ($entry.pass) { "✅" } else { "❌" }
        [void]$sb.AppendLine("| $check | $($entry.count) | $icon |")
    }
} else {
    [void]$sb.AppendLine("（未校验）")
}
[void]$sb.AppendLine()

[void]$sb.AppendLine("## 字段一致性")
[void]$sb.AppendLine()
if ($fieldCheckResults.Count -gt 0) {
    [void]$sb.AppendLine("| 字段 | 不匹配数 | 结果 |")
    [void]$sb.AppendLine("|---|---|---|")
    foreach ($check in $fieldCheckResults.Keys) {
        $entry = $fieldCheckResults[$check]
        $icon = if ($entry.pass) { "✅" } else { "❌" }
        [void]$sb.AppendLine("| $check | $($entry.mismatch_count) | $icon |")
    }
} else {
    [void]$sb.AppendLine("（未校验）")
}
[void]$sb.AppendLine()

[void]$sb.AppendLine("## 异常摘要")
[void]$sb.AppendLine()
[void]$sb.AppendLine("| 类型 | 数量 |")
[void]$sb.AppendLine("|---|---|")
[void]$sb.AppendLine("| 过滤无效文章 | $($anomalies.invalid_articles.Count) |")
[void]$sb.AppendLine("| 过滤无效评论 | $($anomalies.invalid_comments.Count) |")
[void]$sb.AppendLine("| 过滤坏作品集关系 | $($anomalies.invalid_collection_articles.Count) |")
[void]$sb.AppendLine("| 过滤坏标签文章关系 | $($anomalies.filtered_tag_article_relations.Count) |")
[void]$sb.AppendLine("| 过滤坏专题标签关系 | $($anomalies.filtered_topic_tag_relations.Count) |")
[void]$sb.AppendLine("| 邮箱冲突处理 | $($anomalies.email_conflicts.Count) |")
[void]$sb.AppendLine("| 空标签回填 | $($anomalies.tag_fallbacks.Count) |")
[void]$sb.AppendLine("| 自关注过滤 | $($anomalies.filtered_self_follows.Count) |")
[void]$sb.AppendLine("| NULL following_id 过滤 | $($anomalies.filtered_null_follows.Count) |")
[void]$sb.AppendLine("| 非用户关注排除 | $($anomalies.filtered_non_user_follows.Count) |")
[void]$sb.AppendLine()

if ($sampleData.Count -gt 0) {
    [void]$sb.AppendLine("## 抽样验收")
    [void]$sb.AppendLine()
    [void]$sb.AppendLine("抽样数据已保存到 ``validation.json``，包含:")
    [void]$sb.AppendLine("- 20 个会员（含昵称、头像、签名、背景图、社交链接）")
    [void]$sb.AppendLine("- 20 篇文章（含标题、状态、作者、标签数）")
    [void]$sb.AppendLine("- 20 条评论（含层级结构、正文预览）")
    [void]$sb.AppendLine("- 10 个作品集（含标题、作者、文章数）")
    [void]$sb.AppendLine("- 20 条关注关系（含关注者/被关注者昵称）")
    [void]$sb.AppendLine("- 全部产品（含标题、编码、标签、价格）")
}

Set-Content -Path (Join-Path $reportDir "summary.md") -Value $sb.ToString() -Encoding utf8

Write-Stage "迁移完成"
Write-Host "产物目录: $workRoot" -ForegroundColor Green
Write-Host "校验摘要: $(Join-Path $reportDir 'summary.md')" -ForegroundColor Green
if (-not $overallPass) {
    Write-Host "⚠️ 校验未全部通过，请检查 validation.json" -ForegroundColor Yellow
}
