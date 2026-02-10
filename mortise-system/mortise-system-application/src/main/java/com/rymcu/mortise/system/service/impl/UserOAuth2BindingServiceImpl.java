package com.rymcu.mortise.system.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.system.entity.UserOAuth2Binding;
import com.rymcu.mortise.system.mapper.UserOAuth2BindingMapper;
import com.rymcu.mortise.system.service.UserOAuth2BindingService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 用户 OAuth2 绑定服务实现
 *
 * @author ronger
 * @since 1.0.0
 */
@Service
public class UserOAuth2BindingServiceImpl extends ServiceImpl<UserOAuth2BindingMapper, UserOAuth2Binding>
        implements UserOAuth2BindingService {

    @Override
    public UserOAuth2Binding findByProviderAndOpenId(String provider, String openId) {
        if (StringUtils.isBlank(provider) || StringUtils.isBlank(openId)) {
            return null;
        }
        return getOne(QueryWrapper.create()
                .where(UserOAuth2Binding::getProvider).eq(provider)
                .and(UserOAuth2Binding::getOpenId).eq(openId)
        );
    }

    @Override
    public UserOAuth2Binding findByProviderAndUnionId(String provider, String unionId) {
        if (StringUtils.isBlank(provider) || StringUtils.isBlank(unionId)) {
            return null;
        }
        return getOne(QueryWrapper.create()
                .where(UserOAuth2Binding::getProvider).eq(provider)
                .and(UserOAuth2Binding::getUnionId).eq(unionId)
        );
    }

    @Override
    public UserOAuth2Binding findByUserIdAndProvider(Long userId, String provider) {
        if (userId == null || StringUtils.isBlank(provider)) {
            return null;
        }
        return getOne(QueryWrapper.create()
                .where(UserOAuth2Binding::getUserId).eq(userId)
                .and(UserOAuth2Binding::getProvider).eq(provider)
        );
    }
}
