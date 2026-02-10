# mortise-system 模块迁移验证脚本
# 用于检查迁移后的代码是否符合规范

$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  mortise-system 迁移验证脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseDir = "mortise-system/src/main/java/com/rymcu/mortise/system"
$resourceDir = "mortise-system/src/main/resources"

# 统计变量
$totalIssues = 0
$warnings = @()
$errors = @()

# 函数: 检查文件中的模式
function Check-Pattern {
    param(
        [string]$directory,
        [string]$pattern,
        [string]$description,
        [string]$severity = "warning"
    )
    
    if (Test-Path $directory) {
        $files = Get-ChildItem -Path $directory -Filter "*.java" -Recurse -ErrorAction SilentlyContinue
        $foundCount = 0
        
        foreach ($file in $files) {
            $content = Get-Content $file.FullName -Raw -Encoding UTF8
            $matches = [regex]::Matches($content, $pattern)
            
            if ($matches.Count -gt 0) {
                $foundCount++
                $relPath = $file.FullName.Replace((Get-Location).Path, "").TrimStart('\')
                
                if ($severity -eq "error") {
                    $script:errors += "❌ $relPath : $description (找到 $($matches.Count) 处)"
                } else {
                    $script:warnings += "⚠️  $relPath : $description (找到 $($matches.Count) 处)"
                }
            }
        }
        
        return $foundCount
    }
    return 0
}

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  检查 1: 包名验证" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

# 检查是否还有旧的包名
$oldPackageCount = 0
$oldPackageCount += Check-Pattern $baseDir "package com\.rymcu\.mortise\.entity;" "发现旧的包名声明 (entity)" "error"
$oldPackageCount += Check-Pattern $baseDir "package com\.rymcu\.mortise\.mapper;" "发现旧的包名声明 (mapper)" "error"
$oldPackageCount += Check-Pattern $baseDir "package com\.rymcu\.mortise\.service;" "发现旧的包名声明 (service，应该是 system.service)" "error"
$oldPackageCount += Check-Pattern $baseDir "package com\.rymcu\.mortise\.controller;" "发现旧的包名声明 (controller)" "error"

if ($oldPackageCount -eq 0) {
    Write-Host "✅ 包名声明检查通过" -ForegroundColor Green
} else {
    Write-Host "❌ 发现 $oldPackageCount 个文件使用了旧的包名" -ForegroundColor Red
    $script:totalIssues += $oldPackageCount
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  检查 2: 导入语句验证" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

# 检查是否还有旧的导入语句
$oldImportCount = 0
$oldImportCount += Check-Pattern $baseDir "import com\.rymcu\.mortise\.entity\." "使用了旧的 entity 导入" "error"
$oldImportCount += Check-Pattern $baseDir "import com\.rymcu\.mortise\.mapper\." "使用了旧的 mapper 导入" "error"
$oldImportCount += Check-Pattern $baseDir "import com\.rymcu\.mortise\.util\." "使用了旧的 util 导入 (应该用 common.util)" "error"
$oldImportCount += Check-Pattern $baseDir "import com\.rymcu\.mortise\.result\." "使用了旧的 result 导入 (应该用 core.result)" "error"
$oldImportCount += Check-Pattern $baseDir "import com\.rymcu\.mortise\.exception\." "使用了旧的 exception 导入 (应该用 common.exception)" "error"

if ($oldImportCount -eq 0) {
    Write-Host "✅ 导入语句检查通过" -ForegroundColor Green
} else {
    Write-Host "❌ 发现 $oldImportCount 个文件使用了旧的导入语句" -ForegroundColor Red
    $script:totalIssues += $oldImportCount
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  检查 3: 业务封装层使用" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

# 检查 Service 实现是否直接使用基础设施服务
$directUsageCount = 0

# 检查是否直接注入 CacheService (应该用 SystemCacheService)
$cacheServiceFiles = Check-Pattern "$baseDir/service/impl" "@Autowired\s+private\s+CacheService" "直接使用 CacheService (建议用 SystemCacheService)" "warning"
if ($cacheServiceFiles -gt 0) {
    $directUsageCount += $cacheServiceFiles
}

# 检查是否直接注入 NotificationService (应该用 SystemNotificationService)
$notificationServiceFiles = Check-Pattern "$baseDir/service/impl" "@Autowired\s+private\s+NotificationService\s" "直接使用 NotificationService (建议用 SystemNotificationService)" "warning"
if ($notificationServiceFiles -gt 0) {
    $directUsageCount += $notificationServiceFiles
}

if ($directUsageCount -eq 0) {
    Write-Host "✅ 业务封装层使用检查通过" -ForegroundColor Green
} else {
    Write-Host "⚠️  发现 $directUsageCount 个文件直接使用基础设施服务 (建议使用业务封装层)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  检查 4: Controller 最佳实践" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

$controllerDir = "$baseDir/controller"
if (Test-Path $controllerDir) {
    $controllers = Get-ChildItem -Path $controllerDir -Filter "*.java" -ErrorAction SilentlyContinue
    $controllerCount = $controllers.Count
    $withTagCount = 0
    $withOperationLogCount = 0
    $withRateLimitCount = 0
    
    foreach ($controller in $controllers) {
        $content = Get-Content $controller.FullName -Raw -Encoding UTF8
        
        if ($content -match "@Tag\(") { $withTagCount++ }
        if ($content -match "@OperationLog\(") { $withOperationLogCount++ }
        if ($content -match "@RateLimit\(") { $withRateLimitCount++ }
    }
    
    Write-Host "📊 Controller 统计:" -ForegroundColor White
    Write-Host "   - Controller 总数: $controllerCount" -ForegroundColor White
    Write-Host "   - 使用 @Tag 注解: $withTagCount / $controllerCount" -ForegroundColor $(if ($withTagCount -eq $controllerCount) { "Green" } else { "Yellow" })
    Write-Host "   - 使用 @OperationLog 注解: $withOperationLogCount / $controllerCount" -ForegroundColor $(if ($withOperationLogCount -gt 0) { "Green" } else { "Yellow" })
    Write-Host "   - 使用 @RateLimit 注解: $withRateLimitCount / $controllerCount" -ForegroundColor $(if ($withRateLimitCount -gt 0) { "Green" } else { "Yellow" })
    
    if ($withTagCount -lt $controllerCount) {
        $script:warnings += "⚠️  建议为所有 Controller 添加 @Tag 注解以完善 API 文档"
    }
    if ($withOperationLogCount -eq 0) {
        $script:warnings += "⚠️  建议为关键操作添加 @OperationLog 注解以记录操作日志"
    }
    if ($withRateLimitCount -eq 0) {
        $script:warnings += "⚠️  建议为高频接口添加 @RateLimit 注解以防止滥用"
    }
} else {
    Write-Host "⚠️  Controller 目录不存在" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  检查 5: Mapper XML 验证" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

$mapperXmlDir = "$resourceDir/mapper"
if (Test-Path $mapperXmlDir) {
    $xmlFiles = Get-ChildItem -Path $mapperXmlDir -Filter "*.xml" -ErrorAction SilentlyContinue
    $xmlCount = $xmlFiles.Count
    $oldNamespaceCount = 0
    $oldResultTypeCount = 0
    
    foreach ($xml in $xmlFiles) {
        $content = Get-Content $xml.FullName -Raw -Encoding UTF8
        
        if ($content -match 'namespace="com\.rymcu\.mortise\.mapper\.') {
            $oldNamespaceCount++
            $relPath = $xml.FullName.Replace((Get-Location).Path, "").TrimStart('\')
            $script:errors += "❌ $relPath : Mapper XML 使用了旧的 namespace"
        }
        
        if ($content -match 'resultType="com\.rymcu\.mortise\.entity\.') {
            $oldResultTypeCount++
            $relPath = $xml.FullName.Replace((Get-Location).Path, "").TrimStart('\')
            $script:errors += "❌ $relPath : Mapper XML 使用了旧的 resultType (entity)"
        }
    }
    
    Write-Host "📊 Mapper XML 统计:" -ForegroundColor White
    Write-Host "   - XML 文件总数: $xmlCount" -ForegroundColor White
    
    if ($oldNamespaceCount -eq 0 -and $oldResultTypeCount -eq 0) {
        Write-Host "✅ Mapper XML 检查通过" -ForegroundColor Green
    } else {
        Write-Host "❌ 发现 Mapper XML 配置问题" -ForegroundColor Red
        $script:totalIssues += $oldNamespaceCount + $oldResultTypeCount
    }
} else {
    Write-Host "⚠️  Mapper XML 目录不存在 (如果项目使用注解方式，可忽略)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  检查 6: 文件统计" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

function Count-Files {
    param($path, $pattern = "*.java")
    if (Test-Path $path) {
        return (Get-ChildItem -Path $path -Filter $pattern -ErrorAction SilentlyContinue).Count
    }
    return 0
}

$entityCount = Count-Files "$baseDir/entity"
$mapperCount = Count-Files "$baseDir/mapper"
$modelCount = Count-Files "$baseDir/model"
$serviceCount = Count-Files "$baseDir/service" 
$serviceImplCount = Count-Files "$baseDir/service/impl"
$controllerCount = Count-Files "$baseDir/controller"
$handlerCount = Count-Files "$baseDir/handler"
$eventCount = Count-Files "$baseDir/handler/event"
$serializerCount = Count-Files "$baseDir/serializer"
$mapperXmlCount = Count-Files "$resourceDir/mapper" "*.xml"

$totalJavaFiles = $entityCount + $mapperCount + $modelCount + $serviceCount + $serviceImplCount + $controllerCount + $handlerCount + $eventCount + $serializerCount

Write-Host "📊 文件统计:" -ForegroundColor White
Write-Host "   - Entity: $entityCount" -ForegroundColor White
Write-Host "   - Mapper: $mapperCount" -ForegroundColor White
Write-Host "   - Model (DTO/VO): $modelCount" -ForegroundColor White
Write-Host "   - Service 接口: $serviceCount" -ForegroundColor White
Write-Host "   - Service 实现: $serviceImplCount" -ForegroundColor White
Write-Host "   - Controller: $controllerCount" -ForegroundColor White
Write-Host "   - Handler: $handlerCount" -ForegroundColor White
Write-Host "   - Event: $eventCount" -ForegroundColor White
Write-Host "   - Serializer: $serializerCount" -ForegroundColor White
Write-Host "   - Mapper XML: $mapperXmlCount" -ForegroundColor White
Write-Host "   - Java 文件总数: $totalJavaFiles" -ForegroundColor Cyan

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  验证结果汇总" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

if ($errors.Count -gt 0) {
    Write-Host "❌ 发现 $($errors.Count) 个错误:" -ForegroundColor Red
    foreach ($error in $errors) {
        Write-Host "   $error" -ForegroundColor Red
    }
    Write-Host ""
}

if ($warnings.Count -gt 0) {
    Write-Host "⚠️  发现 $($warnings.Count) 个警告:" -ForegroundColor Yellow
    foreach ($warning in $warnings) {
        Write-Host "   $warning" -ForegroundColor Yellow
    }
    Write-Host ""
}

if ($errors.Count -eq 0 -and $warnings.Count -eq 0) {
    Write-Host "✅ 所有检查通过! 代码质量良好!" -ForegroundColor Green
} elseif ($errors.Count -eq 0) {
    Write-Host "✅ 必要检查通过! 建议处理上述警告以提升代码质量" -ForegroundColor Green
} else {
    Write-Host "❌ 发现必须修复的错误，请根据上述提示进行修正" -ForegroundColor Red
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  下一步建议" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

if ($errors.Count -gt 0) {
    Write-Host "1. 修复上述错误 (必须)" -ForegroundColor Red
    Write-Host "2. 运行编译验证: mvn clean compile -pl mortise-system -am" -ForegroundColor White
    Write-Host "3. 修复编译错误" -ForegroundColor White
    Write-Host "4. 重新运行此验证脚本" -ForegroundColor White
} else {
    Write-Host "1. 运行编译验证: mvn clean compile -pl mortise-system -am" -ForegroundColor Green
    Write-Host "2. 运行单元测试: mvn test -pl mortise-system" -ForegroundColor Green
    Write-Host "3. 启动应用测试: mvn spring-boot:run -pl mortise-app" -ForegroundColor Green
    Write-Host "4. 测试 REST API 接口" -ForegroundColor Green
    
    if ($warnings.Count -gt 0) {
        Write-Host ""
        Write-Host "可选优化:" -ForegroundColor Yellow
        Write-Host "- 处理上述警告以提升代码质量" -ForegroundColor Yellow
        Write-Host "- 添加必要的注解 (@Tag, @OperationLog, @RateLimit)" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "📚 相关文档:" -ForegroundColor Cyan
Write-Host "   - 迁移指南: docs/mortise-system-migration-guide.md" -ForegroundColor White
Write-Host "   - 检查清单: docs/mortise-system-migration-checklist.md" -ForegroundColor White
Write-Host "   - 替换配置: docs/vscode-replace-config.json" -ForegroundColor White
Write-Host ""

# 返回错误码
if ($errors.Count -gt 0) {
    exit 1
} else {
    exit 0
}
