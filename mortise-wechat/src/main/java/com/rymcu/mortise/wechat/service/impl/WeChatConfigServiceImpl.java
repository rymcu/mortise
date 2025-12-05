package com.rymcu.mortise.wechat.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.common.enumerate.DefaultFlag;
import com.rymcu.mortise.common.enumerate.EnabledFlag;
import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.wechat.config.WeChatMpProperties;
import com.rymcu.mortise.wechat.config.WeChatOpenProperties;
import com.rymcu.mortise.wechat.entity.WeChatAccount;
import com.rymcu.mortise.wechat.entity.WeChatConfig;
import com.rymcu.mortise.wechat.enumerate.WeChatAccountType;
import com.rymcu.mortise.wechat.mapper.WeChatAccountMapper;
import com.rymcu.mortise.wechat.mapper.WeChatConfigMapper;
import com.rymcu.mortise.wechat.service.WeChatConfigService;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rymcu.mortise.wechat.entity.table.WeChatAccountTableDef.WE_CHAT_ACCOUNT;
import static com.rymcu.mortise.wechat.entity.table.WeChatConfigTableDef.WE_CHAT_CONFIG;

/**
 * 微信配置服务实现
 * <p>提供配置加载和缓存功能</p>
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Service
public class WeChatConfigServiceImpl extends ServiceImpl<WeChatConfigMapper, WeChatConfig> implements WeChatConfigService {

    private final WeChatAccountMapper accountMapper;

    private final StringEncryptor stringEncryptor;

    public WeChatConfigServiceImpl(WeChatAccountMapper weChatAccountMapper,
                                   @Qualifier("jasyptStringEncryptor") StringEncryptor stringEncryptor) {
        this.accountMapper = weChatAccountMapper;
        this.stringEncryptor = stringEncryptor;
    }

    @Override
    @Cacheable(value = "wechat:config", key = "'mp:default'", unless = "#result == null")
    public WeChatMpProperties loadDefaultMpConfig() {
        log.info("从数据库加载默认微信公众号配置");
        return loadMpConfigByAccountId(null);
    }

    @Override
    @Cacheable(value = "wechat:config", key = "'mp:' + (#accountId != null ? #accountId : 'default')",
            unless = "#result == null")
    public WeChatMpProperties loadMpConfigByAccountId(Long accountId) {
        log.info("从数据库加载微信公众号配置，accountId: {}", accountId);

        // 获取账号信息
        WeChatAccount account;
        if (accountId != null) {
            account = accountMapper.selectOneById(accountId);
        } else {
            QueryWrapper query = QueryWrapper.create().select()
                    .where(WE_CHAT_ACCOUNT.ACCOUNT_TYPE.eq(WeChatAccountType.MP.getCode()))
                    .where(WE_CHAT_ACCOUNT.IS_DEFAULT.eq(DefaultFlag.YES.ordinal()))
                    .and(WE_CHAT_ACCOUNT.IS_ENABLED.eq(EnabledFlag.YES.ordinal()));
            account = accountMapper.selectOneByQuery(query);
        }

        if (account == null) {
            log.warn("未找到微信公众号账号, accountId: {}", accountId);
            return null;
        }

        if (account.getIsEnabled() != 1) {
            log.warn("微信公众号账号未启用, accountId: {}", account.getId());
            return null;
        }

        // 获取账号配置
        Map<String, WeChatConfig> configMap = listConfigs(accountId);

        // 构建配置对象
        WeChatMpProperties properties = new WeChatMpProperties();
        properties.setEnabled(true);
        properties.setAppId(account.getAppId());
        properties.setAppSecret(decryptValue(account.getAppSecret()));
        properties.setToken(getConfigValue(configMap, "token", false));
        properties.setAesKey(getConfigValue(configMap, "aesKey", true));

        log.info("微信公众号配置加载完成，account: {}, appId: {}",
                account.getAccountName(), maskString(properties.getAppId()));

        return properties;
    }

    @Override
    public WeChatMpProperties loadMpConfigByAppId(String appId) {
        log.info("根据AppID加载微信公众号配置: {}", appId);
        QueryWrapper query = QueryWrapper.create().select()
                .where(WE_CHAT_ACCOUNT.APP_ID.eq(appId))
                .and(WE_CHAT_ACCOUNT.IS_ENABLED.eq(EnabledFlag.YES.ordinal()));
        WeChatAccount account = accountMapper.selectOneByQuery(query);
        if (account == null) {
            log.warn("未找到AppID对应的公众号账号: {}", appId);
            return null;
        }

        return loadMpConfigByAccountId(account.getId());
    }

    @Override
    @Cacheable(value = "wechat:config", key = "'open:default'", unless = "#result == null")
    public WeChatOpenProperties loadDefaultOpenConfig() {
        log.info("从数据库加载默认微信开放平台配置");
        return loadOpenConfigByAccountId(null);
    }

    @Override
    @Cacheable(value = "wechat:config", key = "'open:' + (#accountId != null ? #accountId : 'default')",
            unless = "#result == null")
    public WeChatOpenProperties loadOpenConfigByAccountId(Long accountId) {
        log.info("从数据库加载微信开放平台配置，accountId: {}", accountId);

        // 获取账号信息
        WeChatAccount account;
        if (accountId != null) {
            account = accountMapper.selectOneById(accountId);
        } else {
            QueryWrapper query = QueryWrapper.create().select()
                    .where(WE_CHAT_ACCOUNT.ACCOUNT_TYPE.eq(WeChatAccountType.OPEN.getCode()))
                    .where(WE_CHAT_ACCOUNT.IS_DEFAULT.eq(DefaultFlag.YES.ordinal()))
                    .and(WE_CHAT_ACCOUNT.IS_ENABLED.eq(EnabledFlag.YES.ordinal()));
            account = accountMapper.selectOneByQuery(query);
        }
        if (account == null) {
            log.warn("未找到微信开放平台账号, accountId: {}", accountId);
            return null;
        }

        if (account.getIsEnabled() != 1) {
            log.warn("微信开放平台账号未启用, accountId: {}", account.getId());
            return null;
        }

        // 获取账号配置
        Map<String, WeChatConfig> configMap = listConfigs(accountId);

        // 构建配置对象
        WeChatOpenProperties properties = new WeChatOpenProperties();
        properties.setEnabled(true);
        properties.setAppId(account.getAppId());
        properties.setAppSecret(decryptValue(account.getAppSecret()));
        properties.setRedirectUri(getConfigValue(configMap, "redirectUri", false));
        properties.setQrCodeExpireSeconds(getIntValue(configMap, "qrCodeExpireSeconds", 300));

        log.info("微信开放平台配置加载完成，account: {}, appId: {}",
                account.getAccountName(), maskString(properties.getAppId()));

        return properties;
    }

    private Map<String, WeChatConfig> listConfigs(Long accountId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select().where(WE_CHAT_CONFIG.ACCOUNT_ID.eq(accountId))
                .and(WE_CHAT_CONFIG.STATUS.eq(Status.ENABLED.ordinal()));
        List<WeChatConfig> configs = mapper.selectListByQuery(queryWrapper);
        return configs.stream()
                .collect(Collectors.toMap(WeChatConfig::getConfigKey, Function.identity()));
    }

    @Override
    @CacheEvict(value = "wechat:config", allEntries = true)
    public void refreshCache() {
        log.info("微信配置缓存已刷新");
    }

    // ==================== 私有方法 ====================

    /**
     * 解密值
     */
    private String decryptValue(String value) {
        if (value == null) {
            return null;
        }

        try {
            return stringEncryptor.decrypt(value);
        } catch (Exception e) {
            log.error("解密失败，将使用原值: {}", e.getMessage());
            return value;
        }
    }

    /**
     * 获取配置值（支持解密）
     */
    private String getConfigValue(Map<String, WeChatConfig> configMap, String key, boolean needDecrypt) {
        WeChatConfig config = configMap.get(key);
        if (config == null || config.getConfigValue() == null) {
            return null;
        }

        String value = config.getConfigValue();

        // 如果配置标记为加密且需要解密
        if (needDecrypt && config.getIsEncrypted() != null && config.getIsEncrypted() == 1) {
            value = decryptValue(value);
        }

        return value;
    }

    /**
     * 获取整数值配置
     */
    private int getIntValue(Map<String, WeChatConfig> configMap, String key, int defaultValue) {
        String value = getConfigValue(configMap, key, false);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("配置 {} 值 {} 不是有效的整数，使用默认值 {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 掩码敏感信息（用于日志输出）
     */
    private String maskString(String str) {
        if (str == null || str.length() <= 6) {
            return "***";
        }
        return str.substring(0, 3) + "***" + str.substring(str.length() - 3);
    }
}
