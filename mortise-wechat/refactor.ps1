# WeChat 模块重构脚本
# 用于快速创建所有必需的文件

$baseDir = "d:\rymcu2024\mortise\mortise-wechat\src\main\java\com\rymcu\mortise\wechat"

# 1. 创建目录结构
Write-Host "Creating directory structure..." -ForegroundColor Green
New-Item -Path "$baseDir\service\impl" -ItemType Directory -Force | Out-Null
New-Item -Path "$baseDir\model\request" -ItemType Directory -Force | Out-Null
New-Item -Path "$baseDir\model\response" -ItemType Directory -Force | Out-Null

Write-Host "✓ Directory structure created" -ForegroundColor Green

# 2. 删除旧的 Service 文件（如果存在）
Write-Host "`nCleaning up old service files..." -ForegroundColor Yellow
Remove-Item -Path "$baseDir\service\WeChatConfigManagementService.java" -ErrorAction SilentlyContinue
Remove-Item -Path "$baseDir\service\WeChatAccountManagementService.java" -ErrorAction SilentlyContinue
Remove-Item -Path "$baseDir\service\WeChatMultiAccountConfigService.java" -ErrorAction SilentlyContinue

Write-Host "✓ Old files cleaned up" -ForegroundColor Green

Write-Host "`n=====================================" -ForegroundColor Cyan
Write-Host "Refactoring Plan Summary" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Service Layer:" -ForegroundColor Yellow
Write-Host "  • WeChatAccountService - 账号和配置管理"
Write-Host "  • WeChatConfigService - 配置加载服务"
Write-Host "  • WeChatLoginService - 登录服务"
Write-Host "  • WeChatMessageService - 消息服务"
Write-Host ""
Write-Host "Controller Layer:" -ForegroundColor Yellow
Write-Host "  • WeChatAccountController - /api/v1/admin/wechat/accounts"
Write-Host "  • WeChatLoginController - /api/v1/wechat/login"
Write-Host "  • WeChatMessageController - /api/v1/admin/wechat/messages"
Write-Host "  • WeChatPortalController - /api/v1/wechat/portal/{appId}"
Write-Host ""
Write-Host "Key Changes:" -ForegroundColor Yellow
Write-Host "  ✓ Service/ServiceImpl 模式"
Write-Host "  ✓ 统一使用 GlobalResult 返回"
Write-Host "  ✓ 支持分页查询"
Write-Host "  ✓ 完整的 API 文档"
Write-Host "  ✓ 移除旧表结构支持"
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "  1. 查看 docs/REFACTORING_PLAN.md 了解详细方案"
Write-Host "  2. 手动创建或使用工具生成 Service 实现类"
Write-Host "  3. 重构 Controller 层"
Write-Host "  4. 创建 DTO/VO 类"
Write-Host "  5. 添加单元测试"
Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
