package com.rymcu.mortise.wechat.service;

import com.rymcu.mortise.wechat.config.WeChatMpProperties;
import com.rymcu.mortise.wechat.entity.WeChatAccount;
import com.rymcu.mortise.wechat.enumerate.WeChatAccountType;
import com.rymcu.mortise.wechat.exception.WeChatAccountNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信公众号动态管理器
 * <p>负责微信公众号配置的动态加载、更新、移除和查询</p>
 * <p>支持运行时热更新配置，无需重启应用</p>
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicWeChatServiceManager {

    // 注入空的、可变的 WxMpService Bean
    private final WxMpService wxMpService;
    private final WeChatAccountService accountService;
    private final WeChatConfigService configService;

    // 内部维护一个 accountId -> appId 的映射缓存
    private final Map<Long, String> accountIdToAppIdMap = new ConcurrentHashMap<>();

    // 内部维护 appId -> config 的映射，用于重新设置多配置
    private final Map<String, WxMpConfigStorage> configStorageMap = new ConcurrentHashMap<>();

    /**
     * 在 Bean 初始化后，执行一次全量加载
     */
    @PostConstruct
    public void init() {
        log.info("Initializing WeChat MP accounts from database...");
        try {
            reloadAll();
        } catch (Exception e) {
            log.warn("Failed to initialize WeChat MP accounts from database (application will continue): {}", e.getMessage());
        }
    }

    /**
     * 重新从数据库加载所有公众号配置
     * <p>可以暴露一个接口，用于手动触发全量刷新</p>
     */
    public synchronized void reloadAll() {
        try {
            // 先清空现有配置
            accountIdToAppIdMap.clear();
            configStorageMap.clear();

            List<WeChatAccount> accounts = accountService.listAccounts(WeChatAccountType.MP.getCode());
            log.info("Found {} enabled WeChat MP accounts in database.", accounts.size());

            for (WeChatAccount account : accounts) {
                addAccountInternal(account);
            }

            // 重新设置多配置存储
            if (!configStorageMap.isEmpty()) {
                wxMpService.setMultiConfigStorages(configStorageMap);
            }

            log.info("Finished reloading all WeChat MP accounts. Total loaded: {}", accountIdToAppIdMap.size());
        } catch (Exception e) {
            log.error("Failed to reload WeChat MP accounts from database", e);
            throw e;
        }
    }

    /**
     * 添加或更新单个账号配置
     * @param accountId 账号ID
     */
    public synchronized void addOrUpdateAccount(Long accountId) {
        WeChatAccount account = accountService.getAccountById(accountId);
        if (account != null) {
            addAccountInternal(account);

            // 重新设置多配置存储
            if (!configStorageMap.isEmpty()) {
                wxMpService.setMultiConfigStorages(new ConcurrentHashMap<>(configStorageMap));
            }

            log.info("Added/Updated WeChat MP account: accountId={}, accountName={}",
                    accountId, account.getAccountName());
        } else {
            log.warn("Account not found for accountId: {}", accountId);
        }
    }

    /**
     * 内部添加逻辑
     */
    private void addAccountInternal(WeChatAccount account) {
        try {
            WeChatMpProperties properties = configService.loadMpConfigByAccountId(account.getId());
            if (properties != null && properties.isEnabled()) {
                WxMpDefaultConfigImpl config = new WxMpDefaultConfigImpl();
                config.setAppId(properties.getAppId());
                config.setSecret(properties.getAppSecret());
                config.setToken(properties.getToken());
                config.setAesKey(properties.getAesKey());

                // 如果已存在，先移除
                if (accountIdToAppIdMap.containsKey(account.getId())) {
                    String oldAppId = accountIdToAppIdMap.get(account.getId());
                    configStorageMap.remove(oldAppId);
                }

                // 添加新配置
                configStorageMap.put(properties.getAppId(), config);
                accountIdToAppIdMap.put(account.getId(), properties.getAppId());

                log.info("  ✓ Loaded config for accountId: {}, accountName: {}, appId: {}",
                        account.getId(), account.getAccountName(), maskString(properties.getAppId()));
            } else {
                log.warn("Config not enabled or not found for accountId: {}", account.getId());
            }
        } catch (Exception e) {
            log.warn("Failed to load config for accountId: {}. Error: {}", account.getId(), e.getMessage());
        }
    }

    /**
     * 移除一个账号
     * @param accountId 账号ID
     */
    public synchronized void removeAccount(Long accountId) {
        if (accountIdToAppIdMap.containsKey(accountId)) {
            String appId = accountIdToAppIdMap.get(accountId);
            configStorageMap.remove(appId);
            accountIdToAppIdMap.remove(accountId);

            // 重新设置多配置存储
            wxMpService.setMultiConfigStorages(new ConcurrentHashMap<>(configStorageMap));

            log.info("Removed config for accountId: {}, appId: {}", accountId, maskString(appId));
        } else {
            log.warn("No config found to remove for accountId: {}", accountId);
        }
    }

    /**
     * 根据业务 accountId 获取对应的 WxMpService
     * <p>这是业务代码应该调用的主要方法</p>
     *
     * @param accountId 账号ID
     * @return WxMpService 实例（已切换到指定账号上下文）
     * @throws WeChatAccountNotFoundException 当找不到对应配置时
     */
    public WxMpService getServiceByAccountId(Long accountId) {
        String appId = accountIdToAppIdMap.get(accountId);
        if (appId == null) {
            throw new WeChatAccountNotFoundException(accountId);
        }

        try {
            // 切换线程上下文到指定的 appId，这是一个线程安全的操作
            wxMpService.switchover(appId);
            return wxMpService;
        } catch (Exception e) {
            log.error("Failed to switch WeChat MP service to accountId: {}, appId: {}", accountId, appId, e);
            throw new WeChatAccountNotFoundException(accountId, e);
        }
    }

    /**
     * 根据 appId 获取对应的 WxMpService
     * <p>主要用于消息接收时的路由</p>
     *
     * @param appId 公众号 AppID
     * @return WxMpService 实例（已切换到指定账号上下文）
     * @throws WeChatAccountNotFoundException 当找不到对应配置时
     */
    public WxMpService getServiceByAppId(String appId) {
        if (!configStorageMap.containsKey(appId)) {
            throw new WeChatAccountNotFoundException("No WeChat MP configuration found for appId: " + appId);
        }

        try {
            wxMpService.switchover(appId);
            return wxMpService;
        } catch (Exception e) {
            log.error("Failed to switch WeChat MP service to appId: {}", appId, e);
            throw new WeChatAccountNotFoundException("Failed to switch to appId: " + appId, e);
        }
    }

    /**
     * 检查指定账号是否已配置
     */
    public boolean isAccountConfigured(Long accountId) {
        return accountIdToAppIdMap.containsKey(accountId);
    }

    /**
     * 检查指定 appId 是否已配置
     */
    public boolean isAppIdConfigured(String appId) {
        return configStorageMap.containsKey(appId);
    }

    /**
     * 获取所有已配置的账号ID
     */
    public java.util.Set<Long> getAllConfiguredAccountIds() {
        return accountIdToAppIdMap.keySet();
    }

    /**
     * 获取所有已配置的 appId
     */
    public java.util.Set<String> getAllConfiguredAppIds() {
        return configStorageMap.keySet();
    }

    /**
     * 获取账号ID到AppID的映射
     */
    public Map<Long, String> getAccountIdToAppIdMap() {
        return new ConcurrentHashMap<>(accountIdToAppIdMap);
    }

    /**
     * 掩码敏感信息
     */
    private String maskString(String str) {
        if (str == null || str.length() <= 6) {
            return "***";
        }
        return str.substring(0, 3) + "***" + str.substring(str.length() - 3);
    }

    public WxMpService getDefaultService() {
        WeChatAccount weChatAccount = accountService.getAccountById(null);
        return getServiceByAccountId(weChatAccount.getId());
    }

    public String getAppIdByAccountId(Long accountId) {
        if (accountIdToAppIdMap.containsKey(accountId)) {
            return accountIdToAppIdMap.get(accountId);
        }
        throw new WeChatAccountNotFoundException(accountId);
    }
}
