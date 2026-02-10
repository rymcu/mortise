package com.rymcu.mortise.wechat.controller;

import com.rymcu.mortise.web.annotation.AdminController;
import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.wechat.entity.WeChatAccount;
import com.rymcu.mortise.wechat.entity.WeChatConfig;
import com.rymcu.mortise.wechat.model.WeChatAccountSearch;
import com.rymcu.mortise.wechat.service.WeChatAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 微信账号配置管理控制器
 * <p>提供微信账号和配置的管理接口（仅管理员可用）</p>
 *
 * @author ronger
 * @since 1.0.0
 */
@Tag(name = "微信账号管理", description = "微信账号配置管理相关接口")
@Slf4j
@AdminController
@RequestMapping("/wechat/accounts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class WeChatAccountController {

    private final WeChatAccountService accountService;

    // ==================== 账号管理 ====================

    @Operation(summary = "获取 WeChat 账号列表", description = "分页查询 WeChat 账号数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping
    public GlobalResult<Page<WeChatAccount>> pageAccounts(@Parameter(description = "账号查询条件") @Valid WeChatAccountSearch search) {
        Page<WeChatAccount> page = new Page<>(search.getPageNum(), search.getPageSize());
        Page<WeChatAccount> result = accountService.pageAccounts(page, search);
        return GlobalResult.success(result);
    }

    /**
     * 获取账号详情
     *
     * @param id 账号ID
     * @return 账号信息
     */
    @Operation(summary = "获取账号详情", description = "根据ID获取账号详细信息")
    @GetMapping("/{id}")
    public GlobalResult<WeChatAccount> getAccount(@PathVariable @Parameter(description = "账号ID") Long id) {
        log.info("获取微信账号详情，id: {}", id);
        WeChatAccount account = accountService.getAccountById(id);
        return GlobalResult.success(account);
    }

    /**
     * 创建账号
     *
     * @param weChatAccount 账号信息
     * @return 创建的账号ID
     */
    @Operation(summary = "创建账号", description = "新增微信账号")
    @PostMapping
    public GlobalResult<Long> createAccount(@RequestBody @Valid WeChatAccount weChatAccount) {
        log.info("创建微信账号，type: {}, name: {}", weChatAccount.getAccountType(), weChatAccount.getAccountName());

        WeChatAccount account = new WeChatAccount();
        account.setAccountType(weChatAccount.getAccountType());
        account.setAccountName(weChatAccount.getAccountName());
        account.setAppId(weChatAccount.getAppId());
        account.setAppSecret(weChatAccount.getAppSecret());
        account.setIsDefault(weChatAccount.getIsDefault());
        account.setIsEnabled(weChatAccount.getIsEnabled());

        Long id = accountService.createAccount(account);
        return GlobalResult.success(id);
    }

    /**
     * 更新账号
     *
     * @param id      账号ID
     * @param weChatAccount 账号信息
     */
    @Operation(summary = "更新账号", description = "修改微信账号信息")
    @PutMapping("/{id}")
    public GlobalResult<Boolean> updateAccount(@PathVariable @Parameter(description = "账号ID") Long id,
                                                @RequestBody @Valid WeChatAccount weChatAccount) {
        log.info("更新微信账号，id: {}", id);

        WeChatAccount account = new WeChatAccount();
        account.setId(id);
        if (weChatAccount.getAccountName() != null) {
            account.setAccountName(weChatAccount.getAccountName());
        }
        if (weChatAccount.getAppId() != null) {
            account.setAppId(weChatAccount.getAppId());
        }
        if (weChatAccount.getAppSecret() != null) {
            account.setAppSecret(weChatAccount.getAppSecret());
        }
        if (weChatAccount.getIsDefault() != null) {
            account.setIsDefault(weChatAccount.getIsDefault());
        }
        if (weChatAccount.getIsEnabled() != null) {
            account.setIsEnabled(weChatAccount.getIsEnabled());
        }

        boolean result = accountService.updateAccount(account);
        return GlobalResult.success(result);
    }

    /**
     * 删除账号
     *
     * @param id 账号ID
     */
    @Operation(summary = "删除账号", description = "删除微信账号及其所有配置")
    @DeleteMapping("/{id}")
    public GlobalResult<Boolean> deleteAccount(@PathVariable @Parameter(description = "账号ID") Long id) {
        log.info("删除微信账号，id: {}", id);
        boolean result = accountService.deleteAccount(id);
        return GlobalResult.success(result);
    }

    /**
     * 设置默认账号
     *
     * @param id 账号ID
     */
    @Operation(summary = "设置默认账号", description = "将指定账号设置为默认账号")
    @PatchMapping("/{id}/default")
    public GlobalResult<Boolean> setDefaultAccount(@PathVariable @Parameter(description = "账号ID") Long id) {
        log.info("设置默认账号，id: {}", id);
        boolean result = accountService.setDefaultAccount(id);
        return GlobalResult.success(result);
    }

    /**
     * 启用/禁用账号
     *
     * @param id      账号ID
     * @param enabled 是否启用
     */
    @Operation(summary = "启用/禁用账号", description = "切换账号的启用状态")
    @PatchMapping("/{id}/status")
    public GlobalResult<Boolean> toggleAccount(@PathVariable @Parameter(description = "账号ID") Long id,
                                                @RequestParam @Parameter(description = "是否启用") boolean enabled) {
        log.info("{}账号，id: {}", enabled ? "启用" : "禁用", id);
        boolean result = accountService.toggleAccount(id, enabled);
        return GlobalResult.success(result);
    }

    // ==================== 配置管理 ====================

    /**
     * 获取账号的所有配置
     *
     * @param accountId 账号ID
     * @return 配置列表
     */
    @Operation(summary = "获取配置列表", description = "获取账号的所有配置项")
    @GetMapping("/{accountId}/configs")
    public GlobalResult<List<WeChatConfig>> listConfigs(@PathVariable @Parameter(description = "账号ID") Long accountId) {
        log.info("获取账号配置列表，accountId: {}", accountId);
        List<WeChatConfig> list = accountService.listConfigs(accountId);
        return GlobalResult.success(list);
    }

    /**
     * 保存或更新配置
     *
     * @param accountId 账号ID
     * @param request   配置信息
     */
    @Operation(summary = "保存配置", description = "保存或更新账号配置")
    @PostMapping("/{accountId}/configs")
    public GlobalResult<Boolean> saveConfig(@PathVariable @Parameter(description = "账号ID") Long accountId,
                                             @RequestBody @Valid WeChatConfig request) {
        log.info("保存配置，accountId: {}, key: {}", accountId, request.getConfigKey());
        boolean result = accountService.saveConfig(
                accountId,
                request.getConfigKey(),
                request.getConfigValue(),
                request.getIsEncrypted()
        );
        return GlobalResult.success(result);
    }

    /**
     * 删除配置
     *
     * @param accountId 账号ID
     * @param configKey 配置键
     */
    @Operation(summary = "删除配置", description = "删除账号的指定配置项")
    @DeleteMapping("/{accountId}/configs/{configKey}")
    public GlobalResult<Boolean> deleteConfig(@PathVariable @Parameter(description = "账号ID") Long accountId,
                                               @PathVariable @Parameter(description = "配置键") String configKey) {
        log.info("删除配置，accountId: {}, key: {}", accountId, configKey);
        boolean result = accountService.deleteConfig(accountId, configKey);
        return GlobalResult.success(result);
    }

    /**
     * 刷新配置缓存
     */
    @Operation(summary = "刷新缓存", description = "刷新微信配置缓存")
    @PostMapping("/cache/refresh")
    public GlobalResult<Void> refreshCache() {
        log.info("刷新微信配置缓存");
        accountService.refreshCache();
        return GlobalResult.success();
    }
}

