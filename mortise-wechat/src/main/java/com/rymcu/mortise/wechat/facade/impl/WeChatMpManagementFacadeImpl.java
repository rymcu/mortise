package com.rymcu.mortise.wechat.facade.impl;

import com.rymcu.mortise.wechat.exception.WeChatAccountNotFoundException;
import com.rymcu.mortise.wechat.facade.WeChatMpManagementFacade;
import com.rymcu.mortise.wechat.service.DynamicWeChatServiceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信公众号动态管理门面实现
 *
 * @author ronger
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeChatMpManagementFacadeImpl implements WeChatMpManagementFacade {

    private final DynamicWeChatServiceManager dynamicWeChatServiceManager;

    @Override
    public Map<String, Object> reloadAll() {
        dynamicWeChatServiceManager.reloadAll();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "所有微信公众号配置已重新加载");
        result.put("configuredAccounts", dynamicWeChatServiceManager.getAllConfiguredAccountIds().size());
        result.put("configuredAppIds", dynamicWeChatServiceManager.getAllConfiguredAppIds().size());

        log.info("Admin triggered reload all WeChat MP configurations");
        return result;
    }

    @Override
    public Map<String, Object> reloadAccount(Long accountId) {
        dynamicWeChatServiceManager.addOrUpdateAccount(accountId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "账号配置已更新");
        result.put("accountId", accountId);
        result.put("configured", dynamicWeChatServiceManager.isAccountConfigured(accountId));

        log.info("Admin triggered reload WeChat MP configuration for accountId: {}", accountId);
        return result;
    }

    @Override
    public Map<String, Object> removeAccount(Long accountId) {
        boolean wasConfigured = dynamicWeChatServiceManager.isAccountConfigured(accountId);
        dynamicWeChatServiceManager.removeAccount(accountId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", wasConfigured ? "账号配置已移除" : "账号配置不存在");
        result.put("accountId", accountId);
        result.put("wasConfigured", wasConfigured);

        log.info("Admin triggered remove WeChat MP configuration for accountId: {}", accountId);
        return result;
    }

    @Override
    public Map<String, Object> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("configuredAccountIds", dynamicWeChatServiceManager.getAllConfiguredAccountIds());
        result.put("configuredAppIds", dynamicWeChatServiceManager.getAllConfiguredAppIds());
        result.put("accountIdToAppIdMap", dynamicWeChatServiceManager.getAccountIdToAppIdMap());
        result.put("totalAccounts", dynamicWeChatServiceManager.getAllConfiguredAccountIds().size());
        return result;
    }

    @Override
    public Map<String, Object> testAccount(Long accountId) {
        Map<String, Object> result = new HashMap<>();

        try {
            WxMpService service = dynamicWeChatServiceManager.getServiceByAccountId(accountId);

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

        return result;
    }
}
