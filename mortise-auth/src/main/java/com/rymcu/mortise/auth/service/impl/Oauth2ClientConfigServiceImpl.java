package com.rymcu.mortise.auth.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.auth.entity.Oauth2ClientConfig;
import com.rymcu.mortise.auth.mapper.Oauth2ClientConfigMapper;
import com.rymcu.mortise.auth.service.Oauth2ClientConfigService;
import com.rymcu.mortise.common.enumerate.EnabledFlag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.rymcu.mortise.auth.entity.table.Oauth2ClientConfigTableDef.OAUTH2_CLIENT_CONFIG;

/**
 * OAuth2 客户端配置服务实现
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Oauth2ClientConfigServiceImpl implements Oauth2ClientConfigService {

    private final Oauth2ClientConfigMapper oauth2ClientConfigMapper;

    @Override
    public Optional<Oauth2ClientConfig> findByRegistrationId(String registrationId) {
        log.debug("查找客户端配置: registrationId={}", registrationId);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(OAUTH2_CLIENT_CONFIG.REGISTRATION_ID.eq(registrationId))
                .and(OAUTH2_CLIENT_CONFIG.IS_ENABLED.eq(EnabledFlag.YES.ordinal()));

        Oauth2ClientConfig config = oauth2ClientConfigMapper.selectOneByQuery(queryWrapper);
        return Optional.ofNullable(config);
    }

    @Override
    public List<Oauth2ClientConfig> findAllEnabled() {
        log.debug("查找所有启用的客户端配置");

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(OAUTH2_CLIENT_CONFIG.IS_ENABLED.eq(EnabledFlag.YES.ordinal()))
                .orderBy(OAUTH2_CLIENT_CONFIG.CREATED_TIME.desc());

        return oauth2ClientConfigMapper.selectListByQuery(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Oauth2ClientConfig save(Oauth2ClientConfig config) {
        log.info("保存客户端配置: registrationId={}", config.getRegistrationId());

        if (config.getId() == null) {
            // 新建
            config.setCreatedTime(LocalDateTime.now());
            config.setUpdatedTime(LocalDateTime.now());
            oauth2ClientConfigMapper.insertSelective(config);
        } else {
            // 更新
            config.setUpdatedTime(LocalDateTime.now());
            oauth2ClientConfigMapper.insertOrUpdateSelective(config);
        }

        return config;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        log.info("删除客户端配置: id={}", id);
        oauth2ClientConfigMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByRegistrationId(String registrationId) {
        log.info("删除客户端配置: registrationId={}", registrationId);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(OAUTH2_CLIENT_CONFIG.REGISTRATION_ID.eq(registrationId));

        oauth2ClientConfigMapper.deleteByQuery(queryWrapper);
    }
}
