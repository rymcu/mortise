package com.rymcu.mortise.auth.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.auth.entity.Oauth2ClientConfig;
import com.rymcu.mortise.auth.mapper.Oauth2ClientConfigMapper;
import com.rymcu.mortise.auth.model.OAuth2ClientConfigSearch;
import com.rymcu.mortise.auth.service.Oauth2ClientConfigService;
import com.rymcu.mortise.common.enumerate.EnabledFlag;
import com.rymcu.mortise.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
public class Oauth2ClientConfigServiceImpl extends ServiceImpl<Oauth2ClientConfigMapper, Oauth2ClientConfig> implements Oauth2ClientConfigService {

    @Override
    public Optional<Oauth2ClientConfig> findByRegistrationId(String registrationId) {
        log.debug("查找客户端配置: registrationId={}", registrationId);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(OAUTH2_CLIENT_CONFIG.REGISTRATION_ID.eq(registrationId))
                .and(OAUTH2_CLIENT_CONFIG.IS_ENABLED.eq(EnabledFlag.YES.ordinal()));

        Oauth2ClientConfig config = mapper.selectOneByQuery(queryWrapper);
        return Optional.ofNullable(config);
    }

    @Override
    public List<Oauth2ClientConfig> findAllEnabled() {
        log.debug("查找所有启用的客户端配置");

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(OAUTH2_CLIENT_CONFIG.IS_ENABLED.eq(EnabledFlag.YES.ordinal()))
                .orderBy(OAUTH2_CLIENT_CONFIG.CREATED_TIME.desc());

        return mapper.selectListByQuery(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOauth2ClientConfig(Oauth2ClientConfig config) {
        log.info("保存客户端配置: registrationId={}", config.getRegistrationId());
        boolean isUpdate = config.getId() != null;
        if (isUpdate) {
            // 更新
            Oauth2ClientConfig oldOauth2ClientConfig = mapper.selectOneById(config.getId());
            if (oldOauth2ClientConfig == null) {
                throw new ServiceException("数据不存在");
            }
            BeanUtils.copyProperties(config, oldOauth2ClientConfig);
            oldOauth2ClientConfig.setUpdatedTime(LocalDateTime.now());
            return mapper.insertOrUpdateSelective(oldOauth2ClientConfig) > 0;
        }
        // 新建
        config.setCreatedTime(LocalDateTime.now());
        config.setUpdatedTime(LocalDateTime.now());
        return mapper.insertSelective(config) > 0;
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
}
