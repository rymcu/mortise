package com.rymcu.mortise.wechat.controller;

import com.rymcu.mortise.web.annotation.AdminController;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.wechat.entity.WeChatAccount;
import com.rymcu.mortise.wechat.entity.WeChatConfig;
import com.rymcu.mortise.wechat.facade.WeChatAccountFacade;
import com.rymcu.mortise.wechat.model.WeChatAccountSearch;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 微信账号配置管理控制器
 *
 * @author ronger
 * @since 1.0.0
 */
@Tag(name = "微信账号管理", description = "微信账号配置管理相关接口")
@AdminController
@RequestMapping("/wechat/accounts")
@RequiredArgsConstructor
public class WeChatAccountController {

    private final WeChatAccountFacade weChatAccountFacade;

    // ==================== 账号管理 ====================

    @Operation(summary = "获取 WeChat 账号列表", description = "分页查询 WeChat 账号数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('wechat:account:list')")
    public GlobalResult<PageResult<WeChatAccount>> pageAccounts(@Parameter(description = "账号查询条件") @Valid WeChatAccountSearch search) {
        return GlobalResult.success(weChatAccountFacade.pageAccounts(search));
    }

    /**
     * 获取账号详情
     *
     * @param id 账号ID
     * @return 账号信息
     */
    @Operation(summary = "获取账号详情", description = "根据ID获取账号详细信息")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('wechat:account:query')")
    public GlobalResult<WeChatAccount> getAccount(@PathVariable @Parameter(description = "账号ID") Long id) {
        return GlobalResult.success(weChatAccountFacade.getAccount(id));
    }

    /**
     * 创建账号
     *
     * @param weChatAccount 账号信息
     * @return 创建的账号ID
     */
    @Operation(summary = "创建账号", description = "新增微信账号")
    @PostMapping
    @PreAuthorize("hasAuthority('wechat:account:add')")
    public GlobalResult<Long> createAccount(@RequestBody @Valid WeChatAccount weChatAccount) {
        return GlobalResult.success(weChatAccountFacade.createAccount(weChatAccount));
    }

    /**
     * 更新账号
     *
     * @param id      账号ID
     * @param weChatAccount 账号信息
     */
    @Operation(summary = "更新账号", description = "修改微信账号信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('wechat:account:edit')")
    public GlobalResult<Boolean> updateAccount(@PathVariable @Parameter(description = "账号ID") Long id,
                                                @RequestBody @Valid WeChatAccount weChatAccount) {
        return GlobalResult.success(weChatAccountFacade.updateAccount(id, weChatAccount));
    }

    /**
     * 删除账号
     *
     * @param id 账号ID
     */
    @Operation(summary = "删除账号", description = "删除微信账号及其所有配置")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('wechat:account:delete')")
    public GlobalResult<Boolean> deleteAccount(@PathVariable @Parameter(description = "账号ID") Long id) {
        return GlobalResult.success(weChatAccountFacade.deleteAccount(id));
    }

    /**
     * 设置默认账号
     *
     * @param id 账号ID
     */
    @Operation(summary = "设置默认账号", description = "将指定账号设置为默认账号")
    @PatchMapping("/{id}/default")
    @PreAuthorize("hasAuthority('wechat:account:edit')")
    public GlobalResult<Boolean> setDefaultAccount(@PathVariable @Parameter(description = "账号ID") Long id) {
        return GlobalResult.success(weChatAccountFacade.setDefaultAccount(id));
    }

    /**
     * 启用/禁用账号
     *
     * @param id      账号ID
     * @param enabled 是否启用
     */
    @Operation(summary = "启用/禁用账号", description = "切换账号的启用状态")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('wechat:account:edit')")
    public GlobalResult<Boolean> toggleAccount(@PathVariable @Parameter(description = "账号ID") Long id,
                                                @RequestParam @Parameter(description = "是否启用") boolean enabled) {
        return GlobalResult.success(weChatAccountFacade.toggleAccount(id, enabled));
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
    @PreAuthorize("hasAuthority('wechat:account:query')")
    public GlobalResult<List<WeChatConfig>> listConfigs(@PathVariable @Parameter(description = "账号ID") Long accountId) {
        return GlobalResult.success(weChatAccountFacade.listConfigs(accountId));
    }

    /**
     * 保存或更新配置
     *
     * @param accountId 账号ID
     * @param request   配置信息
     */
    @Operation(summary = "保存配置", description = "保存或更新账号配置")
    @PostMapping("/{accountId}/configs")
    @PreAuthorize("hasAuthority('wechat:account:edit')")
    public GlobalResult<Boolean> saveConfig(@PathVariable @Parameter(description = "账号ID") Long accountId,
                                             @RequestBody @Valid WeChatConfig request) {
        return GlobalResult.success(weChatAccountFacade.saveConfig(accountId, request));
    }

    /**
     * 删除配置
     *
     * @param accountId 账号ID
     * @param configKey 配置键
     */
    @Operation(summary = "删除配置", description = "删除账号的指定配置项")
    @DeleteMapping("/{accountId}/configs/{configKey}")
    @PreAuthorize("hasAuthority('wechat:account:delete')")
    public GlobalResult<Boolean> deleteConfig(@PathVariable @Parameter(description = "账号ID") Long accountId,
                                               @PathVariable @Parameter(description = "配置键") String configKey) {
        return GlobalResult.success(weChatAccountFacade.deleteConfig(accountId, configKey));
    }

    /**
     * 刷新配置缓存
     */
    @Operation(summary = "刷新缓存", description = "刷新微信配置缓存")
    @PostMapping("/cache/refresh")
    @PreAuthorize("hasAuthority('wechat:account:edit')")
    public GlobalResult<Void> refreshCache() {
        weChatAccountFacade.refreshCache();
        return GlobalResult.success();
    }
}

