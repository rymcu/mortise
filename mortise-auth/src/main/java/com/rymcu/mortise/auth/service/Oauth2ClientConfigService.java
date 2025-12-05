package com.rymcu.mortise.auth.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.auth.entity.Oauth2ClientConfig;
import com.rymcu.mortise.auth.model.OAuth2ClientConfigSearch;

import java.util.List;

/**
 * OAuth2 客户端配置服务接口
 *
 * @author ronger
 * @since 1.0.0
 */
public interface Oauth2ClientConfigService extends IService<Oauth2ClientConfig> {

    /**
     * 根据 registrationId 查找客户端配置
     *
     * @param registrationId 客户端注册ID
     * @return 客户端配置（如果存在）
     */
    Oauth2ClientConfig loadOauth2ClientConfigByRegistrationId(String registrationId);

    /**
     * 获取所有启用的客户端配置
     *
     * @return 启用的客户端配置列表
     */
    List<Oauth2ClientConfig> loadOauth2ClientConfigAllEnabled();

    /**
     * 删除客户端配置
     *
     * @param id 配置ID
     */
    Boolean deleteById(Long id);

    /**
     * 根据 registrationId 删除客户端配置
     *
     * @param registrationId 客户端注册ID
     */
    Boolean deleteByRegistrationId(String registrationId);

    Page<Oauth2ClientConfig> findOauth2ClientConfigs(Page<Oauth2ClientConfig> page, OAuth2ClientConfigSearch search);

    Boolean batchDeleteOAuth2ClientConfig(List<Long> idOAuth2ClientConfigs);

    Long createOauth2ClientConfig(Oauth2ClientConfig config);

    Boolean updateOauth2ClientConfig(Oauth2ClientConfig config);
}
