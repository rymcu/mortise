package com.rymcu.mortise.wechat.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.common.enumerate.DefaultFlag;
import com.rymcu.mortise.common.enumerate.EnabledFlag;
import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.wechat.entity.WeChatAccount;
import com.rymcu.mortise.wechat.entity.WeChatConfig;
import com.rymcu.mortise.wechat.mapper.WeChatAccountMapper;
import com.rymcu.mortise.wechat.mapper.WeChatConfigMapper;
import com.rymcu.mortise.wechat.model.WeChatAccountSearch;
import com.rymcu.mortise.wechat.service.WeChatAccountService;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.rymcu.mortise.wechat.entity.table.WeChatAccountTableDef.WE_CHAT_ACCOUNT;
import static com.rymcu.mortise.wechat.entity.table.WeChatConfigTableDef.WE_CHAT_CONFIG;

/**
 * 微信账号服务实现
 * <p>提供账号和配置的统一管理</p>
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Service
public class WeChatAccountServiceImpl extends ServiceImpl<WeChatAccountMapper, WeChatAccount> implements WeChatAccountService {

    private final WeChatConfigMapper configMapper;

    private final StringEncryptor stringEncryptor;

    public WeChatAccountServiceImpl(WeChatConfigMapper weChatConfigMapper,
                                    @Qualifier("jasyptStringEncryptor") StringEncryptor stringEncryptor) {
        this.configMapper = weChatConfigMapper;
        this.stringEncryptor = stringEncryptor;
    }

    @Override
    public Page<WeChatAccount> pageAccounts(Page<WeChatAccount> page, WeChatAccountSearch search) {
        QueryWrapper query = QueryWrapper.create()
                .select()
                .orderBy(WE_CHAT_ACCOUNT.CREATED_TIME.desc());

        // 动态条件
        if (StringUtils.hasText(search.getAccountType())) {
            query.and(WE_CHAT_ACCOUNT.ACCOUNT_TYPE.eq(search.getAccountType()));
        }
        if (search.getIsEnabled() != null) {
            query.and(WE_CHAT_ACCOUNT.IS_ENABLED.eq(search.getIsEnabled()));
        }
        if (StringUtils.hasText(search.getAccountName())) {
            query.and(WE_CHAT_ACCOUNT.ACCOUNT_NAME.like("%" + search.getAccountName() + "%"));
        }

        log.info("分页查询微信账号，条件：{}", search);
        return mapper.paginate(page, query);
    }

    @Override
    public List<WeChatAccount> listAccounts(String accountType) {
        QueryWrapper query = QueryWrapper.create()
                .select()
                .where(WE_CHAT_ACCOUNT.IS_ENABLED.eq(EnabledFlag.YES.ordinal()))
                .orderBy(WE_CHAT_ACCOUNT.IS_DEFAULT.asc(), WE_CHAT_ACCOUNT.CREATED_TIME.desc());

        if (StringUtils.hasText(accountType)) {
            query.and(WE_CHAT_ACCOUNT.ACCOUNT_TYPE.eq(accountType));
        }

        return mapper.selectListByQuery(query);
    }

    @Override
    public WeChatAccount getAccountById(Long accountId) {
        return mapper.selectOneById(accountId);
    }

    @Override
    public WeChatAccount getAccountByAppId(String appId) {
        QueryWrapper query = QueryWrapper.create().select()
                .where(WE_CHAT_ACCOUNT.APP_ID.eq(appId))
                .and(WE_CHAT_ACCOUNT.IS_ENABLED.eq(EnabledFlag.YES.ordinal()));
        return mapper.selectOneByQuery(query);
    }

    @Override
    public WeChatAccount getDefaultAccount(String accountType) {
        QueryWrapper query = QueryWrapper.create().select()
                .where(WE_CHAT_ACCOUNT.ACCOUNT_TYPE.eq(accountType))
                .and(WE_CHAT_ACCOUNT.IS_ENABLED.eq(EnabledFlag.YES.ordinal()))
                .and(WE_CHAT_ACCOUNT.IS_DEFAULT.eq(DefaultFlag.YES.ordinal()));
        return mapper.selectOneByQuery(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "wechat:config", allEntries = true)
    public Long createAccount(WeChatAccount account) {
        // 参数校验
        validateAccount(account);

        // 加密敏感信息
        if (StringUtils.hasText(account.getAppSecret())) {
            account.setAppSecret(encryptValue(account.getAppSecret()));
        }

        // 如果设置为默认账号，先取消同类型的其他默认账号
        if (account.getIsDefault() != null && account.getIsDefault() == DefaultFlag.YES.ordinal()) {
            clearDefaultAccount(account.getAccountType());
        }

        // 设置默认值
        if (account.getIsEnabled() == null) {
            account.setIsEnabled(EnabledFlag.YES.ordinal());
        }
        account.setCreatedTime(LocalDateTime.now());

        mapper.insertSelective(account);
        log.info("创建微信账号成功，id: {}, type: {}, name: {}",
                account.getId(), account.getAccountType(), account.getAccountName());
        return account.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "wechat:config", allEntries = true)
    public boolean updateAccount(WeChatAccount account) {
        WeChatAccount existing = mapper.selectOneById(account.getId());
        if (existing == null) {
            throw new IllegalArgumentException("账号不存在：" + account.getId());
        }

        // 如果更新了 AppSecret，需要加密
        if (StringUtils.hasText(account.getAppSecret())
                && !account.getAppSecret().equals(existing.getAppSecret())) {
            account.setAppSecret(encryptValue(account.getAppSecret()));
        }

        // 如果设置为默认账号，先取消同类型的其他默认账号
        if (account.getIsDefault() != null && account.getIsDefault() == 1) {
            clearDefaultAccount(existing.getAccountType());
        }

        account.setUpdatedTime(LocalDateTime.now());
        int rows = mapper.insertOrUpdateSelective(account);
        log.info("更新微信账号成功，id: {}", account.getId());
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "wechat:config", allEntries = true)
    public boolean deleteAccount(Long accountId) {
        // 删除账号的所有配置
        configMapper.deleteByQuery(QueryWrapper.create()
                .from(WeChatConfig.class)
                .where("account_id = {0}", accountId));

        // 删除账号
        int rows = mapper.deleteById(accountId);
        log.info("删除微信账号成功，id: {}", accountId);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "wechat:config", allEntries = true)
    public boolean setDefaultAccount(Long accountId) {
        WeChatAccount account = mapper.selectOneById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("账号不存在：" + accountId);
        }

        // 取消同类型的其他默认账号
        clearDefaultAccount(account.getAccountType());

        // 设置为默认
        account.setIsDefault(DefaultFlag.YES.ordinal());
        account.setUpdatedTime(LocalDateTime.now());
        int rows = mapper.insertOrUpdateSelective(account);
        log.info("设置默认账号成功，id: {}, type: {}", accountId, account.getAccountType());
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "wechat:config", allEntries = true)
    public boolean toggleAccount(Long accountId, boolean enabled) {
        WeChatAccount account = mapper.selectOneById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("账号不存在：" + accountId);
        }

        account.setIsEnabled(enabled ? 1 : 0);
        account.setUpdatedTime(LocalDateTime.now());
        int rows = mapper.insertOrUpdateSelective(account);
        log.info("{}账号成功，id: {}", enabled ? "启用" : "禁用", accountId);
        return rows > 0;
    }

    @Override
    public List<WeChatConfig> listConfigs(Long accountId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select().where(WE_CHAT_CONFIG.ID.eq(accountId))
                .and(WE_CHAT_CONFIG.STATUS.eq(Status.ENABLED.ordinal()));
        return configMapper.selectListByQuery(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "wechat:config", allEntries = true)
    public boolean saveConfig(Long accountId, String configKey, String configValue, Integer isEncrypted) {
        // 如果需要加密
        String finalValue = configValue;
        if (isEncrypted == 1) {
            finalValue = encryptValue(configValue);
        }

        // 查询是否已存在
        QueryWrapper queryWrapper = QueryWrapper.create().select()
                .where(WE_CHAT_CONFIG.ID.eq(accountId))
                .and(WE_CHAT_CONFIG.STATUS.eq(Status.ENABLED.ordinal()))
                .and(WE_CHAT_CONFIG.CONFIG_KEY.eq(configKey));
        WeChatConfig existing = configMapper.selectOneByQuery(queryWrapper);

        if (existing != null) {
            // 更新
            existing.setConfigValue(finalValue);
            existing.setIsEncrypted(isEncrypted);
            existing.setUpdatedTime(LocalDateTime.now());
            int rows = configMapper.insertOrUpdateSelective(existing);
            log.info("更新配置成功，accountId: {}, key: {}", accountId, configKey);
            return rows > 0;
        } else {
            // 新增
            WeChatConfig config = new WeChatConfig();
            config.setAccountId(accountId);
            config.setConfigKey(configKey);
            config.setConfigValue(finalValue);
            config.setIsEncrypted(isEncrypted);
            config.setStatus(0);
            config.setDelFlag(0);
            config.setCreatedTime(LocalDateTime.now());
            int rows = configMapper.insertSelective(config);
            log.info("新增配置成功，accountId: {}, key: {}", accountId, configKey);
            return rows > 0;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "wechat:config", allEntries = true)
    public boolean batchSaveConfigs(Long accountId, List<WeChatConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            return true;
        }

        for (WeChatConfig config : configs) {
            saveConfig(
                    accountId,
                    config.getConfigKey(),
                    config.getConfigValue(),
                    config.getIsEncrypted()
            );
        }

        log.info("批量保存配置成功，accountId: {}, count: {}", accountId, configs.size());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "wechat:config", allEntries = true)
    public boolean deleteConfig(Long accountId, String configKey) {
        int rows = configMapper.deleteByQuery(QueryWrapper.create()
                .from(WeChatConfig.class)
                .where("account_id = {0}", accountId)
                .and("config_key = {0}", configKey));
        log.info("删除配置成功，accountId: {}, key: {}", accountId, configKey);
        return rows > 0;
    }

    @Override
    @CacheEvict(value = "wechat:config", allEntries = true)
    public void refreshCache() {
        log.info("微信配置缓存已刷新");
    }

    // ==================== 私有方法 ====================

    /**
     * 校验账号信息
     */
    private void validateAccount(WeChatAccount account) {
        if (!StringUtils.hasText(account.getAccountType())) {
            throw new IllegalArgumentException("账号类型不能为空");
        }
        if (!StringUtils.hasText(account.getAccountName())) {
            throw new IllegalArgumentException("账号名称不能为空");
        }
        if (!StringUtils.hasText(account.getAppId())) {
            throw new IllegalArgumentException("AppID不能为空");
        }
    }

    /**
     * 加密值
     */
    private String encryptValue(String value) {
        if (value == null) {
            return null;
        }

        try {
            return stringEncryptor.encrypt(value);
        } catch (Exception e) {
            log.error("加密失败，将使用原值: {}", e.getMessage());
            return value;
        }
    }

    /**
     * 取消指定类型的所有默认账号
     */
    private void clearDefaultAccount(String accountType) {
        QueryWrapper query = QueryWrapper.create().select()
                .where(WE_CHAT_ACCOUNT.ACCOUNT_TYPE.eq(accountType))
                .and(WE_CHAT_ACCOUNT.IS_ENABLED.eq(EnabledFlag.YES.ordinal()));

        List<WeChatAccount> accounts = mapper.selectListByQuery(query);
        for (WeChatAccount account : accounts) {
            if (account.getIsDefault() != null && account.getIsDefault() == 1) {
                account.setIsDefault(0);
                account.setUpdatedTime(LocalDateTime.now());
                mapper.insertSelective(account);
            }
        }
    }
}
