# mortise-system 模块迁移脚本（从 GitHub）
# 从 GitHub 仓库克隆原始代码并迁移到 mortise-system 模块

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  mortise-system 模块迁移脚本 (从GitHub)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 定义路径
$tempDir = "mortise-temp"
$githubRepo = "https://github.com/rymcu/mortise.git"
$sourceBase = "$tempDir/src/main/java/com/rymcu/mortise"
$targetBase = "mortise-system/src/main/java/com/rymcu/mortise/system"
$resourceSource = "$tempDir/src/main/resources"
$resourceTarget = "mortise-system/src/main/resources"

Write-Host "📦 配置信息:" -ForegroundColor Cyan
Write-Host "   - GitHub 仓库: $githubRepo" -ForegroundColor White
Write-Host "   - 临时目录: $tempDir" -ForegroundColor White
Write-Host "   - 目标模块: mortise-system" -ForegroundColor White
Write-Host ""

# 检查是否已有临时目录
if (Test-Path $tempDir) {
    $overwrite = Read-Host "临时目录 '$tempDir' 已存在，是否删除并重新克隆? (y/n)"
    if ($overwrite -eq "y") {
        Write-Host "🗑️  删除旧的临时目录..." -ForegroundColor Yellow
        Remove-Item -Path $tempDir -Recurse -Force
    } else {
        Write-Host "✅ 使用现有的临时目录" -ForegroundColor Green
    }
}

# 克隆仓库
if (-not (Test-Path $tempDir)) {
    Write-Host ""
    Write-Host "================================================" -ForegroundColor Cyan
    Write-Host "  第 1 步: 克隆 GitHub 仓库" -ForegroundColor Cyan
    Write-Host "================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "📥 正在克隆仓库..." -ForegroundColor Yellow
    Write-Host "   这可能需要几分钟，请耐心等待..." -ForegroundColor Gray
    
    try {
        git clone $githubRepo $tempDir --depth 1 2>&1 | Out-Null
        Write-Host "✅ 克隆完成!" -ForegroundColor Green
    } catch {
        Write-Host "❌ 克隆失败: $_" -ForegroundColor Red
        Write-Host ""
        Write-Host "请检查:" -ForegroundColor Yellow
        Write-Host "  1. 网络连接是否正常" -ForegroundColor White
        Write-Host "  2. Git 是否已安装 (运行 'git --version' 验证)" -ForegroundColor White
        Write-Host "  3. GitHub 仓库是否可访问" -ForegroundColor White
        exit 1
    }
}

# 验证源代码存在
if (-not (Test-Path $sourceBase)) {
    Write-Host "❌ 错误: 未找到源代码目录: $sourceBase" -ForegroundColor Red
    Write-Host "   请检查 GitHub 仓库结构是否发生变化" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  第 2 步: 创建目录结构" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# 函数: 创建目录
function New-TargetDirectory {
    param($path)
    if (-not (Test-Path $path)) {
        New-Item -ItemType Directory -Force -Path $path | Out-Null
        Write-Host "✅ 创建: $path" -ForegroundColor Green
    }
}

# 创建目录结构
New-TargetDirectory "$targetBase/entity"
New-TargetDirectory "$targetBase/mapper"
New-TargetDirectory "$targetBase/model"
New-TargetDirectory "$targetBase/service"
New-TargetDirectory "$targetBase/service/impl"
New-TargetDirectory "$targetBase/controller"
New-TargetDirectory "$targetBase/handler"
New-TargetDirectory "$targetBase/handler/event"
New-TargetDirectory "$targetBase/serializer"
New-TargetDirectory "$targetBase/annotation"
New-TargetDirectory "$resourceTarget/mapper"

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  第 3 步: 复制文件" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# 函数: 复制文件
function Copy-SourceFiles {
    param(
        [string]$source,
        [string]$target,
        [string]$pattern = "*.java",
        [string]$description
    )
    
    if (Test-Path $source) {
        $files = Get-ChildItem -Path $source -Filter $pattern -File -ErrorAction SilentlyContinue
        if ($files) {
            $count = 0
            foreach ($file in $files) {
                Copy-Item $file.FullName -Destination $target -Force
                $count++
            }
            Write-Host "✅ 复制 $description : $count 个文件" -ForegroundColor Green
            return $count
        } else {
            Write-Host "⚠️  跳过 $description : 未找到 $pattern 文件" -ForegroundColor Yellow
            return 0
        }
    } else {
        Write-Host "⚠️  跳过 $description : 目录不存在" -ForegroundColor Yellow
        return 0
    }
}

$totalFiles = 0

# 复制 Entity
$totalFiles += Copy-SourceFiles "$sourceBase/entity" "$targetBase/entity" "*.java" "Entity 实体"

# 复制 Mapper
$totalFiles += Copy-SourceFiles "$sourceBase/mapper" "$targetBase/mapper" "*.java" "Mapper 接口"

# 复制 Model
$totalFiles += Copy-SourceFiles "$sourceBase/model" "$targetBase/model" "*.java" "Model DTO/VO"

# 复制 Service 接口
$totalFiles += Copy-SourceFiles "$sourceBase/service" "$targetBase/service" "*.java" "Service 接口"

# 复制 Service 实现
$totalFiles += Copy-SourceFiles "$sourceBase/service/impl" "$targetBase/service/impl" "*.java" "Service 实现"

# 复制 Controller (注意：在 web/admin 目录)
if (Test-Path "$tempDir/src/main/java/com/rymcu/mortise/web/admin") {
    $totalFiles += Copy-SourceFiles "$tempDir/src/main/java/com/rymcu/mortise/web/admin" "$targetBase/controller" "*.java" "Controller 控制器"
}

# 复制 Handler
$totalFiles += Copy-SourceFiles "$sourceBase/handler" "$targetBase/handler" "*.java" "Handler 处理器"

# 复制 Event
if (Test-Path "$sourceBase/event") {
    $totalFiles += Copy-SourceFiles "$sourceBase/event" "$targetBase/handler/event" "*.java" "Event 事件"
} elseif (Test-Path "$sourceBase/handler/event") {
    $totalFiles += Copy-SourceFiles "$sourceBase/handler/event" "$targetBase/handler/event" "*.java" "Event 事件"
}

# 复制 Serializer
$totalFiles += Copy-SourceFiles "$sourceBase/serializer" "$targetBase/serializer" "*.java" "Serializer 序列化器"

# 复制 Annotation
$totalFiles += Copy-SourceFiles "$sourceBase/annotation" "$targetBase/annotation" "*.java" "Annotation 注解"

# 复制 Mapper XML
$totalFiles += Copy-SourceFiles "$resourceSource/mapper" "$resourceTarget/mapper" "*.xml" "Mapper XML"

Write-Host ""
Write-Host "📊 复制完成，共复制 $totalFiles 个文件" -ForegroundColor Cyan

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  第 4 步: 批量替换包名" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# 获取所有 Java 文件
$javaFiles = Get-ChildItem -Path $targetBase -Filter "*.java" -Recurse -File

Write-Host "📝 准备替换 $($javaFiles.Count) 个文件的包名..." -ForegroundColor Yellow

$replacements = @{
    "package com.rymcu.mortise.entity;" = "package com.rymcu.mortise.system.entity;"
    "package com.rymcu.mortise.mapper;" = "package com.rymcu.mortise.system.mapper;"
    "package com.rymcu.mortise.model;" = "package com.rymcu.mortise.system.model;"
    "package com.rymcu.mortise.service;" = "package com.rymcu.mortise.system.service;"
    "package com.rymcu.mortise.service.impl;" = "package com.rymcu.mortise.system.service.impl;"
    "package com.rymcu.mortise.web.admin;" = "package com.rymcu.mortise.system.controller;"
    "package com.rymcu.mortise.handler;" = "package com.rymcu.mortise.system.handler;"
    "package com.rymcu.mortise.event;" = "package com.rymcu.mortise.system.handler.event;"
    "package com.rymcu.mortise.handler.event;" = "package com.rymcu.mortise.system.handler.event;"
    "package com.rymcu.mortise.serializer;" = "package com.rymcu.mortise.system.serializer;"
    "package com.rymcu.mortise.annotation;" = "package com.rymcu.mortise.system.annotation;"
}

$replacedCount = 0
foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    $modified = $false
    
    foreach ($old in $replacements.Keys) {
        $new = $replacements[$old]
        if ($content -match [regex]::Escape($old)) {
            $content = $content -replace [regex]::Escape($old), $new
            $modified = $true
        }
    }
    
    if ($modified) {
        Set-Content $file.FullName -Value $content -Encoding UTF8 -NoNewline
        $replacedCount++
    }
}

Write-Host "✅ 已替换 $replacedCount 个文件的包名" -ForegroundColor Green

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  第 5 步: 导入语句替换说明" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "⚠️  重要: 还需要手动替换导入语句!" -ForegroundColor Yellow
Write-Host ""
Write-Host "请按照以下步骤操作:" -ForegroundColor Cyan
Write-Host ""
Write-Host "1️⃣  在 VS Code 中打开项目" -ForegroundColor White
Write-Host "2️⃣  按 Ctrl+Shift+H 打开全局搜索替换" -ForegroundColor White
Write-Host "3️⃣  启用正则表达式模式 (.*)" -ForegroundColor White
Write-Host "4️⃣  在 'files to include' 中输入: mortise-system/**/*.java" -ForegroundColor White
Write-Host "5️⃣  按照下表逐条替换:" -ForegroundColor White
Write-Host ""

$importReplacements = @(
    @{Find="import com\.rymcu\.mortise\.entity\."; Replace="import com.rymcu.mortise.system.entity."; Desc="实体类导入"}
    @{Find="import com\.rymcu\.mortise\.mapper\."; Replace="import com.rymcu.mortise.system.mapper."; Desc="Mapper 导入"}
    @{Find="import com\.rymcu\.mortise\.model\."; Replace="import com.rymcu.mortise.system.model."; Desc="Model 导入"}
    @{Find="import com\.rymcu\.mortise\.service\."; Replace="import com.rymcu.mortise.system.service."; Desc="Service 导入"}
    @{Find="import com\.rymcu\.mortise\.web\.admin\."; Replace="import com.rymcu.mortise.system.controller."; Desc="Controller 导入"}
    @{Find="import com\.rymcu\.mortise\.util\."; Replace="import com.rymcu.mortise.common.util."; Desc="工具类导入"}
    @{Find="import com\.rymcu\.mortise\.result\."; Replace="import com.rymcu.mortise.core.result."; Desc="结果类导入"}
    @{Find="import com\.rymcu\.mortise\.exception\."; Replace="import com.rymcu.mortise.common.exception."; Desc="异常类导入"}
    @{Find="import com\.rymcu\.mortise\.enumerate\."; Replace="import com.rymcu.mortise.common.enumerate."; Desc="枚举导入"}
)

$counter = 1
foreach ($replacement in $importReplacements) {
    Write-Host "  $counter. " -NoNewline -ForegroundColor Cyan
    Write-Host "$($replacement.Desc)" -ForegroundColor White
    Write-Host "     查找: " -NoNewline -ForegroundColor Gray
    Write-Host "$($replacement.Find)" -ForegroundColor Yellow
    Write-Host "     替换: " -NoNewline -ForegroundColor Gray
    Write-Host "$($replacement.Replace)" -ForegroundColor Green
    Write-Host ""
    $counter++
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  迁移完成!" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📊 统计:" -ForegroundColor Green
Write-Host "   - 复制文件: $totalFiles" -ForegroundColor White
Write-Host "   - 替换包名: $replacedCount 个文件" -ForegroundColor White
Write-Host ""
Write-Host "📝 下一步:" -ForegroundColor Yellow
Write-Host "   1. 在 VS Code 中手动替换导入语句 (见上方说明)" -ForegroundColor White
Write-Host "   2. 运行验证脚本: .\verify-system.ps1" -ForegroundColor White
Write-Host "   3. 编译验证: mvn clean compile -pl mortise-system -am" -ForegroundColor White
Write-Host "   4. 修复编译错误 (如果有)" -ForegroundColor White
Write-Host ""
Write-Host "💡 提示:" -ForegroundColor Cyan
Write-Host "   - 详细文档: docs/QUICK_START.md" -ForegroundColor White
Write-Host "   - 检查清单: docs/mortise-system-migration-checklist.md" -ForegroundColor White
Write-Host "   - 替换配置: docs/vscode-replace-config.json" -ForegroundColor White
Write-Host ""
Write-Host "🎉 文件复制和包名替换完成，请继续手动替换导入语句!" -ForegroundColor Green
Write-Host ""
