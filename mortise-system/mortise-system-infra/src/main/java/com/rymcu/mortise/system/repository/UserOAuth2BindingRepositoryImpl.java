package com.rymcu.mortise.system.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.system.entity.UserOAuth2Binding;
import com.rymcu.mortise.system.infra.persistence.PersistenceObjectMapper;
import com.rymcu.mortise.system.infra.persistence.entity.UserOAuth2BindingPO;
import com.rymcu.mortise.system.mapper.UserOAuth2BindingMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

/**
 * MyBatis-Flex 用户 OAuth2 绑定仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class UserOAuth2BindingRepositoryImpl implements UserOAuth2BindingRepository {

    private final UserOAuth2BindingMapper userOAuth2BindingMapper;

    @Override
    public UserOAuth2Binding findByProviderAndOpenId(String provider, String openId) {
        if (StringUtils.isBlank(provider) || StringUtils.isBlank(openId)) {
            return null;
        }
        return userOAuth2BindingMapper.selectOneByQueryAs(QueryWrapper.create()
                .where(UserOAuth2BindingPO::getProvider).eq(provider)
                .and(UserOAuth2BindingPO::getOpenId).eq(openId), UserOAuth2Binding.class);
    }

    @Override
    public UserOAuth2Binding findByProviderAndUnionId(String provider, String unionId) {
        if (StringUtils.isBlank(provider) || StringUtils.isBlank(unionId)) {
            return null;
        }
        return userOAuth2BindingMapper.selectOneByQueryAs(QueryWrapper.create()
                .where(UserOAuth2BindingPO::getProvider).eq(provider)
                .and(UserOAuth2BindingPO::getUnionId).eq(unionId), UserOAuth2Binding.class);
    }

    @Override
    public UserOAuth2Binding findByUserIdAndProvider(Long userId, String provider) {
        if (userId == null || StringUtils.isBlank(provider)) {
            return null;
        }
        return userOAuth2BindingMapper.selectOneByQueryAs(QueryWrapper.create()
                .where(UserOAuth2BindingPO::getUserId).eq(userId)
                .and(UserOAuth2BindingPO::getProvider).eq(provider), UserOAuth2Binding.class);
    }

    @Override
    public boolean save(UserOAuth2Binding binding) {
        UserOAuth2BindingPO bindingPO = PersistenceObjectMapper.copy(binding, UserOAuth2BindingPO::new);
        boolean saved = userOAuth2BindingMapper.insertSelective(bindingPO) > 0;
        if (saved) {
            binding.setId(bindingPO.getId());
        }
        return saved;
    }

    @Override
    public boolean update(UserOAuth2Binding binding) {
        return userOAuth2BindingMapper.update(PersistenceObjectMapper.copy(binding, UserOAuth2BindingPO::new)) > 0;
    }
}
