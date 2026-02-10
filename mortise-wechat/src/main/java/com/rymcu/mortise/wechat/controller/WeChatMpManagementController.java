package com.rymcu.mortise.wechat.controller;

import com.rymcu.mortise.web.annotation.AdminController;
import com.rymcu.mortise.wechat.exception.WeChatAccountNotFoundException;
import com.rymcu.mortise.wechat.service.DynamicWeChatServiceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信公众号动态管理控制器
 * <p>提供微信公众号配置的动态管理接口，支持运行时热更新</p>
 * <p>注意：这些接口应该只对管理员开放，并添加适当的权限控制</p>
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@AdminController
@RequestMapping("/api/admin/wechat/mp")
@RequiredArgsConstructor
public class WeChatMpManagementController {

    private final DynamicWeChatServiceManager dynamicWeChatServiceManager;

    /**
     * 重新加载所有微信公众号配置
     * <p>从数据库重新加载所有配置，适用于批量变更后的刷新</p>
     */
    @PostMapping("/reload-all")
    public ResponseEntity<Map<String, Object>> reloadAll() {
        try {
            dynamicWeChatServiceManager.reloadAll();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "所有微信公众号配置已重新加载");
            result.put("configuredAccounts", dynamicWeChatServiceManager.getAllConfiguredAccountIds().size());
            result.put("configuredAppIds", dynamicWeChatServiceManager.getAllConfiguredAppIds().size());

            log.info("Admin triggered reload all WeChat MP configurations");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to reload all WeChat MP configurations", e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "重新加载失败: " + e.getMessage());

            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 添加或更新指定账号的配置
     * <p>适用于单个账号配置变更后的热更新</p>
     */
    @PostMapping("/accounts/{accountId}/reload")
    public ResponseEntity<Map<String, Object>> reloadAccount(@PathVariable Long accountId) {
        try {
            dynamicWeChatServiceManager.addOrUpdateAccount(accountId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "账号配置已更新");
            result.put("accountId", accountId);
            result.put("configured", dynamicWeChatServiceManager.isAccountConfigured(accountId));

            log.info("Admin triggered reload WeChat MP configuration for accountId: {}", accountId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to reload WeChat MP configuration for accountId: {}", accountId, e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "账号配置更新失败: " + e.getMessage());
            result.put("accountId", accountId);

            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 移除指定账号的配置
     * <p>适用于账号禁用或删除后的热移除</p>
     */
    @DeleteMapping("/accounts/{accountId}")
    public ResponseEntity<Map<String, Object>> removeAccount(@PathVariable Long accountId) {
        try {
            boolean wasConfigured = dynamicWeChatServiceManager.isAccountConfigured(accountId);
            dynamicWeChatServiceManager.removeAccount(accountId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", wasConfigured ? "账号配置已移除" : "账号配置不存在");
            result.put("accountId", accountId);
            result.put("wasConfigured", wasConfigured);

            log.info("Admin triggered remove WeChat MP configuration for accountId: {}", accountId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to remove WeChat MP configuration for accountId: {}", accountId, e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "账号配置移除失败: " + e.getMessage());
            result.put("accountId", accountId);

            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 查询当前已配置的账号状态
     * <p>用于管理界面显示当前配置状态</p>
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("configuredAccountIds", dynamicWeChatServiceManager.getAllConfiguredAccountIds());
        result.put("configuredAppIds", dynamicWeChatServiceManager.getAllConfiguredAppIds());
        result.put("accountIdToAppIdMap", dynamicWeChatServiceManager.getAccountIdToAppIdMap());
        result.put("totalAccounts", dynamicWeChatServiceManager.getAllConfiguredAccountIds().size());

        return ResponseEntity.ok(result);
    }

    /**
     * 测试指定账号的微信服务是否可用
     * <p>用于验证配置是否正确</p>
     */
    @GetMapping("/accounts/{accountId}/test")
    public ResponseEntity<Map<String, Object>> testAccount(@PathVariable Long accountId) {
        Map<String, Object> result = new HashMap<>();

        try {
            WxMpService service = dynamicWeChatServiceManager.getServiceByAccountId(accountId);

            // 简单测试：获取当前的 token（这会触发实际的 API 调用）
            String accessToken = service.getAccessToken();
            boolean hasToken = accessToken != null && !accessToken.isEmpty();

            result.put("success", true);
            result.put("accountId", accountId);
            result.put("configured", true);
            result.put("hasAccessToken", hasToken);
            result.put("message", hasToken ? "配置正常，可以获取访问令牌" : "配置存在但无法获取访问令牌");

            log.info("WeChat MP service test for accountId: {} - success: {}", accountId, hasToken);

        } catch (WeChatAccountNotFoundException e) {
            result.put("success", false);
            result.put("accountId", accountId);
            result.put("configured", false);
            result.put("message", "账号配置不存在");

        } catch (Exception e) {
            result.put("success", false);
            result.put("accountId", accountId);
            result.put("configured", true);
            result.put("message", "配置存在但测试失败: " + e.getMessage());

            log.warn("WeChat MP service test failed for accountId: {}", accountId, e);
        }

        return ResponseEntity.ok(result);
    }
}

