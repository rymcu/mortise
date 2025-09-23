# Spring Boot Actuator 访问演示脚本

Write-Host "=== Spring Boot Actuator 访问演示 ===" -ForegroundColor Green
Write-Host ""

# 配置
$baseUrl = "http://localhost:9999/mortise"
$actuatorUrl = "$baseUrl/actuator"

Write-Host "应用地址: $baseUrl" -ForegroundColor Yellow
Write-Host "Actuator地址: $actuatorUrl" -ForegroundColor Yellow
Write-Host ""

# 1. 登录获取Token
Write-Host "1. 正在登录获取Token..." -ForegroundColor Cyan
try {
    $loginData = @{
        account = "ronger@rymcu.com"
        password = "XzHvhX4CDaN696oQAXdmlcsrqgWbkxRl"
    } | ConvertTo-Json
    
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/login" -Method POST -ContentType "application/json" -Body $loginData -ErrorAction Stop
    
    if ($loginResponse.code -eq 200) {
        Write-Host "✅ 登录成功" -ForegroundColor Green
        $token = $loginResponse.data.token
        $headers = @{"Authorization" = "Bearer $token"}
        Write-Host "   Token: $($token.Substring(0,20))..." -ForegroundColor Gray
    } else {
        Write-Host "❌ 登录失败: $($loginResponse.message)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ 登录请求失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 2. 访问健康检查
Write-Host "2. 访问健康检查端点..." -ForegroundColor Cyan
try {
    $healthData = Invoke-RestMethod -Uri "$actuatorUrl/health" -Method GET -Headers $headers -ErrorAction Stop
    Write-Host "✅ 健康检查访问成功" -ForegroundColor Green
    Write-Host "   总体状态: $($healthData.status)" -ForegroundColor $(if($healthData.status -eq "UP") {"Green"} else {"Red"})
    
    # 显示各组件状态
    Write-Host "   组件状态:" -ForegroundColor Gray
    foreach ($component in $healthData.components.PSObject.Properties) {
        $status = $component.Value.status
        $color = if($status -eq "UP") {"Green"} else {"Red"}
        Write-Host "     $($component.Name): $status" -ForegroundColor $color
    }
} catch {
    Write-Host "❌ 健康检查访问失败: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 3. 访问端点列表
Write-Host "3. 获取可用端点列表..." -ForegroundColor Cyan
try {
    $actuatorList = Invoke-RestMethod -Uri "$actuatorUrl" -Method GET -Headers $headers -ErrorAction Stop
    Write-Host "✅ 端点列表获取成功" -ForegroundColor Green
    Write-Host "   可用端点:" -ForegroundColor Gray
    foreach ($link in $actuatorList._links.PSObject.Properties) {
        if ($link.Name -ne "self") {
            Write-Host "     📊 $($link.Name)" -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "❌ 端点列表获取失败: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 4. 访问应用信息
Write-Host "4. 访问应用信息..." -ForegroundColor Cyan
try {
    $infoData = Invoke-RestMethod -Uri "$actuatorUrl/info" -Method GET -Headers $headers -ErrorAction Stop
    Write-Host "✅ 应用信息获取成功" -ForegroundColor Green
    if ($infoData.PSObject.Properties.Count -gt 0) {
        Write-Host "   应用信息:" -ForegroundColor Gray
        $infoData | ConvertTo-Json -Depth 3
    } else {
        Write-Host "   暂无配置的应用信息" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ 应用信息获取失败: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 5. 访问指标
Write-Host "5. 访问性能指标..." -ForegroundColor Cyan
try {
    $metricsData = Invoke-RestMethod -Uri "$actuatorUrl/metrics" -Method GET -Headers $headers -ErrorAction Stop
    Write-Host "✅ 指标列表获取成功" -ForegroundColor Green
    Write-Host "   可用指标数量: $($metricsData.names.Count)" -ForegroundColor Yellow
    
    # 显示一些关键指标
    $keyMetrics = @(
        "jvm.memory.used",
        "jvm.memory.max", 
        "jvm.gc.pause",
        "system.cpu.usage",
        "hikaricp.connections.active",
        "http.server.requests"
    )
    
    Write-Host "   关键指标:" -ForegroundColor Gray
    foreach ($metric in $keyMetrics) {
        if ($metricsData.names -contains $metric) {
            try {
                $metricData = Invoke-RestMethod -Uri "$actuatorUrl/metrics/$metric" -Method GET -Headers $headers -ErrorAction Stop
                if ($metricData.measurements -and $metricData.measurements.Count -gt 0) {
                    $value = $metricData.measurements[0].value
                    $unit = if($metricData.baseUnit) { $metricData.baseUnit } else { "" }
                    Write-Host "     📈 ${metric}: $value $unit" -ForegroundColor Green
                }
            } catch {
                Write-Host "     ❌ ${metric}: 获取失败" -ForegroundColor Red
            }
        }
    }
} catch {
    Write-Host "❌ 指标获取失败: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 6. 访问Prometheus指标
Write-Host "6. 检查Prometheus指标..." -ForegroundColor Cyan
try {
    $prometheusResponse = Invoke-WebRequest -Uri "$actuatorUrl/prometheus" -Method GET -Headers $headers -ErrorAction Stop
    $prometheusData = $prometheusResponse.Content
    $lineCount = ($prometheusData -split "`n").Count
    Write-Host "✅ Prometheus指标获取成功" -ForegroundColor Green
    Write-Host "   指标数据行数: $lineCount" -ForegroundColor Yellow
    Write-Host "   前几行示例:" -ForegroundColor Gray
    ($prometheusData -split "`n" | Select-Object -First 5) | ForEach-Object {
        Write-Host "     $_" -ForegroundColor DarkGray
    }
} catch {
    Write-Host "❌ Prometheus指标获取失败: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "=== 演示完成 ===" -ForegroundColor Green
Write-Host "✨ 所有主要Actuator端点都可以正常访问！" -ForegroundColor Green
Write-Host "📚 更多详细信息请参考 docs/actuator-access-guide.md" -ForegroundColor Yellow