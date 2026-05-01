package com.rymcu.mortise.wechat.controller;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.web.annotation.AdminController;
import com.rymcu.mortise.wechat.facade.WeChatMpManagementFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 微信公众号动态管理控制器
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@AdminController
@RequestMapping("/wechat/mp")
@RequiredArgsConstructor
public class WeChatMpManagementController {

    private final WeChatMpManagementFacade weChatMpManagementFacade;

    /**
     * 重新加载所有微信公众号配置
     * <p>从数据库重新加载所有配置，适用于批量变更后的刷新</p>
     */
    @PostMapping("/reload-all")
    @PreAuthorize("hasAuthority('wechat:account:edit')")
    public GlobalResult<Map<String, Object>> reloadAll() {
        try {
            return GlobalResult.success(weChatMpManagementFacade.reloadAll());
        } catch (Exception e) {
            log.error("Failed to reload all WeChat MP configurations", e);
            return GlobalResult.error("重新加载失败: " + e.getMessage());
        }
    }

    /**
     * 添加或更新指定账号的配置
     * <p>适用于单个账号配置变更后的热更新</p>
     */
    @PostMapping("/accounts/{accountId}/reload")
    @PreAuthorize("hasAuthority('wechat:account:edit')")
    public GlobalResult<Map<String, Object>> reloadAccount(@PathVariable Long accountId) {
        try {
            return GlobalResult.success(weChatMpManagementFacade.reloadAccount(accountId));
        } catch (Exception e) {
            log.error("Failed to reload WeChat MP configuration for accountId: {}", accountId, e);
            return GlobalResult.error("账号配置更新失败: " + e.getMessage());
        }
    }

    /**
     * 移除指定账号的配置
     * <p>适用于账号禁用或删除后的热移除</p>
     */
    @DeleteMapping("/accounts/{accountId}")
    @PreAuthorize("hasAuthority('wechat:account:delete')")
    public GlobalResult<Map<String, Object>> removeAccount(@PathVariable Long accountId) {
        try {
            return GlobalResult.success(weChatMpManagementFacade.removeAccount(accountId));
        } catch (Exception e) {
            log.error("Failed to remove WeChat MP configuration for accountId: {}", accountId, e);
            return GlobalResult.error("账号配置移除失败: " + e.getMessage());
        }
    }

    /**
     * 查询当前已配置的账号状态
     * <p>用于管理界面显示当前配置状态</p>
     */
    @GetMapping("/status")
    @PreAuthorize("hasAuthority('wechat:account:query')")
    public GlobalResult<Map<String, Object>> getStatus() {
        return GlobalResult.success(weChatMpManagementFacade.getStatus());
    }

    /**
     * 测试指定账号的微信服务是否可用
     * <p>用于验证配置是否正确</p>
     */
    @GetMapping("/accounts/{accountId}/test")
    @PreAuthorize("hasAuthority('wechat:account:query')")
    public GlobalResult<Map<String, Object>> testAccount(@PathVariable Long accountId) {
        return GlobalResult.success(weChatMpManagementFacade.testAccount(accountId));
    }
}

