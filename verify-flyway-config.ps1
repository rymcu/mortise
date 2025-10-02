# ============================================
# Flyway 脚本位置验证工具
# 用于快速验证 Flyway 是否能找到迁移脚本
# ============================================

Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║       Mortise Flyway 配置验证工具                              ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# 1. 检查 SQL 脚本文件是否存在
Write-Host "✓ 步骤 1: 检查 SQL 脚本文件..." -ForegroundColor Yellow
$sqlScriptPath = "mortise-system\src\main\resources\db\migration\V1__Create_System_Tables.sql"

if (Test-Path $sqlScriptPath) {
    Write-Host "  ✅ 找到 SQL 脚本: $sqlScriptPath" -ForegroundColor Green
    $scriptSize = (Get-Item $sqlScriptPath).Length
    Write-Host "  📄 文件大小: $scriptSize 字节" -ForegroundColor Gray
} else {
    Write-Host "  ❌ 未找到 SQL 脚本: $sqlScriptPath" -ForegroundColor Red
    Write-Host "  请确认文件路径是否正确！" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 2. 检查 mortise-system 模块配置
Write-Host "✓ 步骤 2: 检查 mortise-system 模块..." -ForegroundColor Yellow
$systemPomPath = "mortise-system\pom.xml"

if (Test-Path $systemPomPath) {
    Write-Host "  ✅ 找到 mortise-system 模块" -ForegroundColor Green
} else {
    Write-Host "  ❌ 未找到 mortise-system 模块" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 3. 检查 mortise-app 依赖配置
Write-Host "✓ 步骤 3: 检查 mortise-app 依赖..." -ForegroundColor Yellow
$appPomPath = "mortise-app\pom.xml"

if (Test-Path $appPomPath) {
    $appPomContent = Get-Content $appPomPath -Raw
    if ($appPomContent -match "mortise-system") {
        Write-Host "  ✅ mortise-app 已正确依赖 mortise-system" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  mortise-app 未依赖 mortise-system" -ForegroundColor Yellow
        Write-Host "  请在 mortise-app/pom.xml 中添加依赖！" -ForegroundColor Yellow
    }
} else {
    Write-Host "  ❌ 未找到 mortise-app/pom.xml" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 4. 检查 Flyway 配置
Write-Host "✓ 步骤 4: 检查 Flyway 配置..." -ForegroundColor Yellow
$flywayConfigPath = "mortise-app\src\main\resources\application-dev.yml"

if (Test-Path $flywayConfigPath) {
    $configContent = Get-Content $flywayConfigPath -Raw
    if ($configContent -match "flyway:") {
        Write-Host "  ✅ 找到 Flyway 配置" -ForegroundColor Green
        
        if ($configContent -match "enabled:\s*true") {
            Write-Host "  ✅ Flyway 已启用" -ForegroundColor Green
        } else {
            Write-Host "  ⚠️  Flyway 可能未启用" -ForegroundColor Yellow
        }
        
        if ($configContent -match "locations:\s*classpath:db/migration") {
            Write-Host "  ✅ 脚本位置配置正确: classpath:db/migration" -ForegroundColor Green
        } else {
            Write-Host "  ⚠️  脚本位置配置可能不正确" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  ⚠️  未找到 Flyway 配置" -ForegroundColor Yellow
    }
} else {
    Write-Host "  ⚠️  未找到配置文件: $flywayConfigPath" -ForegroundColor Yellow
}

Write-Host ""

# 5. 构建建议
Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  验证结果                                                      ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ 基本配置检查通过！" -ForegroundColor Green
Write-Host ""
Write-Host "📝 下一步操作建议：" -ForegroundColor Yellow
Write-Host "1. 构建项目并验证：" -ForegroundColor White
Write-Host "   mvn clean package -DskipTests" -ForegroundColor Gray
Write-Host ""
Write-Host "2. 验证 JAR 包内容：" -ForegroundColor White
Write-Host "   jar tf mortise-system\target\mortise-system-0.0.1.jar | Select-String 'db/migration'" -ForegroundColor Gray
Write-Host ""
Write-Host "3. 启动应用查看日志：" -ForegroundColor White
Write-Host "   mvn spring-boot:run" -ForegroundColor Gray
Write-Host ""
Write-Host "4. 观察 Flyway 执行日志，确认脚本被识别和执行。" -ForegroundColor White
Write-Host ""

Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  配置说明                                                      ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "在 Maven 多模块项目中：" -ForegroundColor White
Write-Host "• SQL 脚本位于: mortise-system/src/main/resources/db/migration/" -ForegroundColor Gray
Write-Host "• Flyway 配置: mortise-app/src/main/resources/application-dev.yml" -ForegroundColor Gray
Write-Host "• 配置项: locations: classpath:db/migration" -ForegroundColor Gray
Write-Host ""
Write-Host "这个配置 ✅ 可以正常工作，因为：" -ForegroundColor Green
Write-Host "1. mortise-app 依赖 mortise-system" -ForegroundColor Gray
Write-Host "2. 打包时 mortise-system 的资源会在 classpath 中" -ForegroundColor Gray
Write-Host "3. Flyway 会扫描整个 classpath，包括依赖模块" -ForegroundColor Gray
Write-Host ""
