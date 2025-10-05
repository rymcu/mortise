package com.rymcu.mortise.system.service;

import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.system.entity.UserOAuth2Binding;

/**
 * 用户 OAuth2 绑定服务接口
 *
 * @author ronger
 * @since 1.0.0
 */
public interface UserOAuth2BindingService extends IService<UserOAuth2Binding> {

    /**
     * 根据提供商和 OpenID 查找绑定关系
     *
     * @param provider OAuth2 提供商
     * @param openId   OAuth2 提供商的用户唯一标识
     * @return 绑定关系，如果不存在则返回 null
     */
    UserOAuth2Binding findByProviderAndOpenId(String provider, String openId);

    /**
     * 根据提供商和 UnionID 查找绑定关系（仅微信）
     *
     * @param provider OAuth2 提供商
     * @param unionId  微信 UnionID
     * @return 绑定关系，如果不存在则返回 null
     */
    UserOAuth2Binding findByProviderAndUnionId(String provider, String unionId);

    /**
     * 根据用户 ID 和提供商查找绑定关系
     *
     * @param userId   用户 ID
     * @param provider OAuth2 提供商
     * @return 绑定关系，如果不存在则返回 null
     */
    UserOAuth2Binding findByUserIdAndProvider(Long userId, String provider);
}
