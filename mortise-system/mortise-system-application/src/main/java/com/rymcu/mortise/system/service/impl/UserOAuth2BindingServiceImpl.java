package com.rymcu.mortise.system.service.impl;

import com.rymcu.mortise.system.entity.UserOAuth2Binding;
import com.rymcu.mortise.system.repository.UserOAuth2BindingRepository;
import com.rymcu.mortise.system.service.UserOAuth2BindingService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 用户 OAuth2 绑定服务实现
 *
 * @author ronger
 * @since 1.0.0
 */
@Service
public class UserOAuth2BindingServiceImpl implements UserOAuth2BindingService {

    @Resource
    private UserOAuth2BindingRepository userOAuth2BindingRepository;

    @Override
    public UserOAuth2Binding findByProviderAndOpenId(String provider, String openId) {
        return userOAuth2BindingRepository.findByProviderAndOpenId(provider, openId);
    }

    @Override
    public UserOAuth2Binding findByProviderAndUnionId(String provider, String unionId) {
        return userOAuth2BindingRepository.findByProviderAndUnionId(provider, unionId);
    }

    @Override
    public UserOAuth2Binding findByUserIdAndProvider(Long userId, String provider) {
        return userOAuth2BindingRepository.findByUserIdAndProvider(userId, provider);
    }

    @Override
    public boolean save(UserOAuth2Binding binding) {
        return userOAuth2BindingRepository.save(binding);
    }

    @Override
    public boolean update(UserOAuth2Binding binding) {
        return userOAuth2BindingRepository.update(binding);
    }
}
