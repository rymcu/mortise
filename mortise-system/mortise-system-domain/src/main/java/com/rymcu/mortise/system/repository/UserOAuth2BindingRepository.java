package com.rymcu.mortise.system.repository;

import com.rymcu.mortise.system.entity.UserOAuth2Binding;

/**
 * 用户 OAuth2 绑定仓储端口。
 */
public interface UserOAuth2BindingRepository {

    UserOAuth2Binding findByProviderAndOpenId(String provider, String openId);

    UserOAuth2Binding findByProviderAndUnionId(String provider, String unionId);

    UserOAuth2Binding findByUserIdAndProvider(Long userId, String provider);

    boolean save(UserOAuth2Binding binding);

    boolean update(UserOAuth2Binding binding);
}
