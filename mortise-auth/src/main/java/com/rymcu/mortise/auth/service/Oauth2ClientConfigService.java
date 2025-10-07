package com.rymcu.mortise.auth.service;

import com.rymcu.mortise.auth.entity.Oauth2ClientConfig;

import java.util.List;
import java.util.Optional;

/**
 * OAuth2 客户端配置服务接口
 *
 * @author ronger
 * @since 1.0.0
 */
public interface Oauth2ClientConfigService {

    /**
     * 根据 registrationId 查找客户端配置
     *
     * @param registrationId 客户端注册ID
     * @return 客户端配置（如果存在）
     */
    Optional<Oauth2ClientConfig> findByRegistrationId(String registrationId);

    /**
     * 获取所有启用的客户端配置
     *
     * @return 启用的客户端配置列表
     */
    List<Oauth2ClientConfig> findAllEnabled();

    /**
     * 保存或更新客户端配置
     *
     * @param config 客户端配置
     * @return 保存后的配置
     */
    Oauth2ClientConfig save(Oauth2ClientConfig config);

    /**
     * 删除客户端配置
     *
     * @param id 配置ID
     */
    void deleteById(Long id);

    /**
     * 根据 registrationId 删除客户端配置
     *
     * @param registrationId 客户端注册ID
     */
    void deleteByRegistrationId(String registrationId);
}
