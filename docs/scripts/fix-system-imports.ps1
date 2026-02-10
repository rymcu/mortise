# Mortise-System Import 批量替换脚本
# 用于修复所有旧包名的 import 语句

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Mortise-System Import 批量替换工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$systemPath = "mortise-system\src\main\java"

# 定义所有需要替换的模式
$replacements = @(
    @{
        Pattern = 'import com\.rymcu\.mortise\.annotation\.'
        Replacement = 'import com.rymcu.mortise.system.annotation.'
        Description = "注解类 (annotation)"
    },
    @{
        Pattern = 'import com\.rymcu\.mortise\.serializer\.'
        Replacement = 'import com.rymcu.mortise.system.serializer.'
        Description = "序列化类 (serializer)"
    },
    @{
        Pattern = 'import com\.rymcu\.mortise\.handler\.event\.'
        Replacement = 'import com.rymcu.mortise.system.handler.event.'
        Description = "事件类 (handler.event)"
    },
    @{
        Pattern = 'import com\.rymcu\.mortise\.system\.util\.Utils;'
        Replacement = 'import com.rymcu.mortise.common.util.Utils;'
        Description = "工具类 Utils"
    },
    @{
        Pattern = 'import com\.rymcu\.mortise\.system\.util\.BeanCopierUtil;'
        Replacement = 'import com.rymcu.mortise.common.util.BeanCopierUtil;'
        Description = "Bean复制工具"
    },
    @{
        Pattern = 'import com\.rymcu\.mortise\.core\.exception\.'
        Replacement = 'import com.rymcu.mortise.common.exception.'
        Description = "异常类 (exception)"
    },
    @{
        Pattern = 'import com\.rymcu\.mortise\.entity\.table\.'
        Replacement = 'import com.rymcu.mortise.system.entity.table.'
        Description = "实体表类 (entity.table)"
    },
    @{
        Pattern = 'import static com\.rymcu\.mortise\.entity\.table\.'
        Replacement = 'import static com.rymcu.mortise.system.entity.table.'
        Description = "实体表类静态导入"
    },
    @{
        Pattern = 'import com\.rymcu\.mortise\.core\.constant\.CacheConstant;'
        Replacement = 'import com.rymcu.mortise.cache.constant.CacheConstant;'
        Description = "缓存常量"
    },
    @{
        Pattern = 'import com\.rymcu\.mortise\.core\.constant\.ProjectConstant;'
        Replacement = 'import com.rymcu.mortise.common.constant.ProjectConstant;'
        Description = "项目常量"
    },
    @{
        Pattern = 'SystemCacheConstant\.'
        Replacement = 'CacheConstant.'
        Description = "修正缓存常量类名"
    }
)

Write-Host "正在扫描 Java 文件..." -ForegroundColor Yellow
$javaFiles = Get-ChildItem -Path $systemPath -Filter "*.java" -Recurse

Write-Host "找到 $($javaFiles.Count) 个 Java 文件" -ForegroundColor Green
Write-Host ""

$totalReplacements = 0

foreach ($replacement in $replacements) {
    Write-Host "处理: $($replacement.Description)" -ForegroundColor Cyan
    $count = 0
    
    foreach ($file in $javaFiles) {
        $content = Get-Content $file.FullName -Raw -Encoding UTF8
        
        if ($content -match $replacement.Pattern) {
            $newContent = $content -replace $replacement.Pattern, $replacement.Replacement
            Set-Content $file.FullName -Value $newContent -Encoding UTF8 -NoNewline
            $count++
        }
    }
    
    if ($count -gt 0) {
        Write-Host "  ✅ 已替换 $count 个文件" -ForegroundColor Green
        $totalReplacements += $count
    } else {
        Write-Host "  ⚪ 无需替换" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  替换完成!" -ForegroundColor Green
Write-Host "  总计修改: $totalReplacements 个文件" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步: 运行编译验证" -ForegroundColor Yellow
Write-Host "  mvn clean compile -pl mortise-system -am" -ForegroundColor White
