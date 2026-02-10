# mortise-system 模块迁移脚本
# 用于从原始 src 目录迁移代码到 mortise-system 模块

# 设置错误时停止
$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  mortise-system 模块迁移脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 定义路径
$sourceBase = "src/main/java/com/rymcu/mortise"
$targetBase = "mortise-system/src/main/java/com/rymcu/mortise/system"
$resourceSource = "src/main/resources"
$resourceTarget = "mortise-system/src/main/resources"

# 检查源目录是否存在
if (-not (Test-Path $sourceBase)) {
    Write-Host "❌ 错误: 源目录不存在: $sourceBase" -ForegroundColor Red
    Write-Host "   提示: 请确保您在项目根目录下运行此脚本" -ForegroundColor Yellow
    exit 1
}

Write-Host "📁 源目录: $sourceBase" -ForegroundColor Green
Write-Host "📁 目标目录: $targetBase" -ForegroundColor Green
Write-Host ""

# 询问是否继续
$confirm = Read-Host "是否开始迁移? (y/n)"
if ($confirm -ne "y") {
    Write-Host "❌ 迁移已取消" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "开始迁移..." -ForegroundColor Cyan
Write-Host ""

# 函数: 创建目录
function Create-Directory {
    param($path)
    if (-not (Test-Path $path)) {
        New-Item -ItemType Directory -Force -Path $path | Out-Null
        Write-Host "✅ 创建目录: $path" -ForegroundColor Green
    }
}

# 函数: 复制并统计文件
function Copy-Files {
    param(
        [string]$source,
        [string]$target,
        [string]$pattern = "*.java",
        [string]$description
    )
    
    if (Test-Path $source) {
        $files = Get-ChildItem -Path $source -Filter $pattern -ErrorAction SilentlyContinue
        if ($files) {
            Create-Directory $target
            $count = 0
            foreach ($file in $files) {
                Copy-Item $file.FullName -Destination $target -Force
                $count++
            }
            Write-Host "✅ 迁移 $description : $count 个文件" -ForegroundColor Green
            return $count
        } else {
            Write-Host "⚠️  跳过 $description : 未找到文件" -ForegroundColor Yellow
            return 0
        }
    } else {
        Write-Host "⚠️  跳过 $description : 目录不存在 ($source)" -ForegroundColor Yellow
        return 0
    }
}

# 统计变量
$totalFiles = 0

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  第 1 步: 创建目录结构" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

# 创建 Java 源码目录
Create-Directory "$targetBase/entity"
Create-Directory "$targetBase/mapper"
Create-Directory "$targetBase/model"
Create-Directory "$targetBase/service"
Create-Directory "$targetBase/service/impl"
Create-Directory "$targetBase/controller"
Create-Directory "$targetBase/handler"
Create-Directory "$targetBase/handler/event"
Create-Directory "$targetBase/serializer"
Create-Directory "$targetBase/constant"

# 创建资源目录
Create-Directory "$resourceTarget/mapper"

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  第 2 步: 迁移 Java 文件" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

# 迁移 Entity
$totalFiles += Copy-Files "$sourceBase/entity" "$targetBase/entity" "*.java" "Entity 实体"

# 迁移 Mapper
$totalFiles += Copy-Files "$sourceBase/mapper" "$targetBase/mapper" "*.java" "Mapper 接口"

# 迁移 Model
$totalFiles += Copy-Files "$sourceBase/model" "$targetBase/model" "*.java" "Model DTO/VO"

# 迁移 Service 接口
$totalFiles += Copy-Files "$sourceBase/service" "$targetBase/service" "*.java" "Service 接口"

# 迁移 Service 实现
$totalFiles += Copy-Files "$sourceBase/service/impl" "$targetBase/service/impl" "*.java" "Service 实现"

# 迁移 Controller
$totalFiles += Copy-Files "$sourceBase/controller" "$targetBase/controller" "*.java" "Controller 控制器"

# 迁移 Handler
$totalFiles += Copy-Files "$sourceBase/handler" "$targetBase/handler" "*.java" "Handler 处理器"

# 迁移 Event
if (Test-Path "$sourceBase/event") {
    $totalFiles += Copy-Files "$sourceBase/event" "$targetBase/handler/event" "*.java" "Event 事件"
} elseif (Test-Path "$sourceBase/handler/event") {
    $totalFiles += Copy-Files "$sourceBase/handler/event" "$targetBase/handler/event" "*.java" "Event 事件"
}

# 迁移 Serializer
$totalFiles += Copy-Files "$sourceBase/serializer" "$targetBase/serializer" "*.java" "Serializer 序列化器"

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  第 3 步: 迁移资源文件" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

# 迁移 Mapper XML
$totalFiles += Copy-Files "$resourceSource/mapper" "$resourceTarget/mapper" "*.xml" "Mapper XML"

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  第 4 步: 包名替换" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

# 获取所有迁移的 Java 文件
$javaFiles = Get-ChildItem -Path $targetBase -Filter "*.java" -Recurse

Write-Host "📝 准备替换 $($javaFiles.Count) 个 Java 文件的包名..." -ForegroundColor Yellow

$replacements = @{
    "package com.rymcu.mortise.entity;" = "package com.rymcu.mortise.system.entity;"
    "package com.rymcu.mortise.mapper;" = "package com.rymcu.mortise.system.mapper;"
    "package com.rymcu.mortise.model;" = "package com.rymcu.mortise.system.model;"
    "package com.rymcu.mortise.service;" = "package com.rymcu.mortise.system.service;"
    "package com.rymcu.mortise.service.impl;" = "package com.rymcu.mortise.system.service.impl;"
    "package com.rymcu.mortise.controller;" = "package com.rymcu.mortise.system.controller;"
    "package com.rymcu.mortise.handler;" = "package com.rymcu.mortise.system.handler;"
    "package com.rymcu.mortise.event;" = "package com.rymcu.mortise.system.handler.event;"
    "package com.rymcu.mortise.handler.event;" = "package com.rymcu.mortise.system.handler.event;"
    "package com.rymcu.mortise.serializer;" = "package com.rymcu.mortise.system.serializer;"
    "package com.rymcu.mortise.constant;" = "package com.rymcu.mortise.system.constant;"
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
Write-Host "  第 5 步: 导入语句替换提示" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

Write-Host ""
Write-Host "⚠️  注意: 还需要手动替换导入语句!" -ForegroundColor Yellow
Write-Host ""
Write-Host "请使用 VS Code 的全局搜索替换功能 (Ctrl+Shift+H):" -ForegroundColor Cyan
Write-Host ""
Write-Host "1️⃣  替换实体导入:" -ForegroundColor White
Write-Host "   查找: import com.rymcu.mortise.entity." -ForegroundColor Gray
Write-Host "   替换: import com.rymcu.mortise.system.entity." -ForegroundColor Gray
Write-Host ""
Write-Host "2️⃣  替换公共类导入:" -ForegroundColor White
Write-Host "   查找: import com.rymcu.mortise.util." -ForegroundColor Gray
Write-Host "   替换: import com.rymcu.mortise.common.util." -ForegroundColor Gray
Write-Host ""
Write-Host "3️⃣  替换结果类导入:" -ForegroundColor White
Write-Host "   查找: import com.rymcu.mortise.result." -ForegroundColor Gray
Write-Host "   替换: import com.rymcu.mortise.core.result." -ForegroundColor Gray
Write-Host ""
Write-Host "4️⃣  替换异常类导入:" -ForegroundColor White
Write-Host "   查找: import com.rymcu.mortise.exception." -ForegroundColor Gray
Write-Host "   替换: import com.rymcu.mortise.common.exception." -ForegroundColor Gray
Write-Host ""
Write-Host "5️⃣  替换枚举导入:" -ForegroundColor White
Write-Host "   查找: import com.rymcu.mortise.enumerate." -ForegroundColor Gray
Write-Host "   替换: import com.rymcu.mortise.common.enumerate." -ForegroundColor Gray
Write-Host ""

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  迁移完成!" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📊 统计:" -ForegroundColor Green
Write-Host "   - 迁移文件总数: $totalFiles" -ForegroundColor White
Write-Host "   - 替换包名: $replacedCount 个文件" -ForegroundColor White
Write-Host ""
Write-Host "📝 下一步:" -ForegroundColor Yellow
Write-Host "   1. 使用 VS Code 全局替换导入语句 (见上方提示)" -ForegroundColor White
Write-Host "   2. 运行: mvn clean compile -pl mortise-system -am" -ForegroundColor White
Write-Host "   3. 根据编译错误调整代码" -ForegroundColor White
Write-Host ""
Write-Host "📚 详细文档: docs/mortise-system-migration-guide.md" -ForegroundColor Cyan
Write-Host ""
