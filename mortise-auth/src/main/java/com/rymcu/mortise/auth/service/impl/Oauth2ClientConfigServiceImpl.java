package com.rymcu.mortise.auth.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.auth.entity.Oauth2ClientConfig;
import com.rymcu.mortise.auth.mapper.Oauth2ClientConfigMapper;
import com.rymcu.mortise.auth.model.OAuth2ClientConfigSearch;
import com.rymcu.mortise.auth.service.Oauth2ClientConfigService;
import com.rymcu.mortise.common.enumerate.EnabledFlag;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.rymcu.mortise.auth.entity.table.Oauth2ClientConfigTableDef.OAUTH2_CLIENT_CONFIG;

/**
 * OAuth2 客户端配置服务实现
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Service
public class Oauth2ClientConfigServiceImpl extends ServiceImpl<Oauth2ClientConfigMapper, Oauth2ClientConfig> implements Oauth2ClientConfigService {

    private final StringEncryptor stringEncryptor;

    public Oauth2ClientConfigServiceImpl(@Qualifier("jasyptStringEncryptor") StringEncryptor stringEncryptor) {
        this.stringEncryptor = stringEncryptor;
    }

    @Override
    public Oauth2ClientConfig loadOauth2ClientConfigByRegistrationId(String registrationId) {
        log.debug("从数据库加载 OAuth2 客户端配置: registrationId={}", registrationId);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(OAUTH2_CLIENT_CONFIG.REGISTRATION_ID.eq(registrationId))
                .and(OAUTH2_CLIENT_CONFIG.IS_ENABLED.eq(EnabledFlag.YES.ordinal()));

        Oauth2ClientConfig config = mapper.selectOneByQuery(queryWrapper);
        if (config != null) {
            config.setClientSecret(decryptValue(config.getClientSecret()));
            return config;
        }
        return null;
    }

    @Override
    public List<Oauth2ClientConfig> loadOauth2ClientConfigAllEnabled() {
        log.debug("加载所有启用的 OAuth2 客户端配置");

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(OAUTH2_CLIENT_CONFIG.IS_ENABLED.eq(EnabledFlag.YES.ordinal()))
                .orderBy(OAUTH2_CLIENT_CONFIG.CREATED_TIME.desc());
        List<Oauth2ClientConfig> list = mapper.selectListByQuery(queryWrapper);
        list.forEach(config -> config.setClientSecret(decryptValue(config.getClientSecret())));
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        log.info("删除客户端配置: id={}", id);
        return mapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByRegistrationId(String registrationId) {
        log.info("删除客户端配置: registrationId={}", registrationId);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(OAUTH2_CLIENT_CONFIG.REGISTRATION_ID.eq(registrationId));

        return mapper.deleteByQuery(queryWrapper) > 0;
    }

    @Override
    public Page<Oauth2ClientConfig> findOauth2ClientConfigs(Page<Oauth2ClientConfig> page, OAuth2ClientConfigSearch search) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .where(OAUTH2_CLIENT_CONFIG.REGISTRATION_ID.eq(search.getRegistrationId(), String::isEmpty))
                .and(OAUTH2_CLIENT_CONFIG.CLIENT_ID.eq(search.getClientId(), String::isEmpty));
        return mapper.paginate(page, queryWrapper);
    }

    @Override
    public Boolean batchDeleteOAuth2ClientConfig(List<Long> idOAuth2ClientConfigs) {
        if (idOAuth2ClientConfigs == null || idOAuth2ClientConfigs.isEmpty()) {
            return false;
        }
        return mapper.deleteBatchByIds(idOAuth2ClientConfigs) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOauth2ClientConfig(Oauth2ClientConfig config) {
        // 加密敏感信息
        if (StringUtils.hasText(config.getClientSecret())) {
            config.setClientSecret(encryptValue(config.getClientSecret()));
        }

        // 设置默认值
        if (config.getIsEnabled() == null) {
            config.setIsEnabled(EnabledFlag.YES.ordinal());
        }

        config.setCreatedTime(LocalDateTime.now());
        mapper.insertSelective(config);
        log.info("创建 OAuth2 配置成功，id: {}, registrationId: {}, name: {}",
                config.getId(), config.getRegistrationId(), config.getClientName());
        return config.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateOauth2ClientConfig(Oauth2ClientConfig config) {
        Oauth2ClientConfig oldConfig = mapper.selectOneById(config.getId());
        if (oldConfig == null) {
            throw new IllegalArgumentException("配置不存在：" + config.getId());
        }

        // 如果更新了 AppSecret，需要加密
        if (StringUtils.hasText(config.getClientSecret())
                && !config.getClientSecret().equals(oldConfig.getClientSecret())) {
            config.setClientSecret(encryptValue(config.getClientSecret()));
        }
        config.setUpdatedTime(LocalDateTime.now());
        int rows = mapper.insertOrUpdateSelective(config);
        log.info("更新微信账号成功，id: {}", config.getId());
        return rows > 0;
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
}
