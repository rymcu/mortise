package com.rymcu.mortise.member.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.member.entity.MemberOAuth2Binding;
import com.rymcu.mortise.member.mapper.MemberOAuth2BindingMapper;
import com.rymcu.mortise.member.service.MemberOAuth2BindingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会员 OAuth2 绑定服务实现
 * <p>
 * 参考 mortise-system 模块的 {@code UserOAuth2BindingServiceImpl} 实现
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Service
public class MemberOAuth2BindingServiceImpl extends ServiceImpl<MemberOAuth2BindingMapper, MemberOAuth2Binding>
        implements MemberOAuth2BindingService {

    @Override
    public MemberOAuth2Binding findByProviderAndOpenId(String provider, String openId) {
        if (StringUtils.isBlank(provider) || StringUtils.isBlank(openId)) {
            log.debug("查询参数为空: provider={}, openId={}", provider, openId);
            return null;
        }
        
        log.debug("根据 provider 和 openId 查询绑定关系: provider={}, openId={}", provider, openId);
        
        return getOne(QueryWrapper.create()
                .where(MemberOAuth2Binding::getProvider).eq(provider)
                .and(MemberOAuth2Binding::getOpenId).eq(openId)
                .and(MemberOAuth2Binding::getDelFlag).eq(0)
        );
    }

    @Override
    public MemberOAuth2Binding findByProviderAndUnionId(String provider, String unionId) {
        if (StringUtils.isBlank(provider) || StringUtils.isBlank(unionId)) {
            log.debug("查询参数为空: provider={}, unionId={}", provider, unionId);
            return null;
        }
        
        log.debug("根据 provider 和 unionId 查询绑定关系: provider={}, unionId={}", provider, unionId);
        
        return getOne(QueryWrapper.create()
                .where(MemberOAuth2Binding::getProvider).eq(provider)
                .and(MemberOAuth2Binding::getUnionId).eq(unionId)
                .and(MemberOAuth2Binding::getDelFlag).eq(0)
        );
    }

    @Override
    public MemberOAuth2Binding findByMemberIdAndProvider(Long memberId, String provider) {
        if (memberId == null || StringUtils.isBlank(provider)) {
            log.debug("查询参数为空: memberId={}, provider={}", memberId, provider);
            return null;
        }
        
        log.debug("根据 memberId 和 provider 查询绑定关系: memberId={}, provider={}", memberId, provider);
        
        return getOne(QueryWrapper.create()
                .where(MemberOAuth2Binding::getMemberId).eq(memberId)
                .and(MemberOAuth2Binding::getProvider).eq(provider)
                .and(MemberOAuth2Binding::getDelFlag).eq(0)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createBinding(MemberOAuth2Binding binding) {
        if (binding == null) {
            log.warn("绑定实体为空");
            return false;
        }
        
        log.info("创建 OAuth2 绑定: memberId={}, provider={}, openId={}", 
                binding.getMemberId(), binding.getProvider(), binding.getOpenId());
        
        // 设置默认值
        if (binding.getStatus() == null) {
            binding.setStatus(0);
        }
        if (binding.getDelFlag() == null) {
            binding.setDelFlag(0);
        }
        
        boolean result = save(binding);
        
        if (result) {
            log.info("OAuth2 绑定创建成功: id={}", binding.getId());
        } else {
            log.error("OAuth2 绑定创建失败: memberId={}, provider={}", 
                    binding.getMemberId(), binding.getProvider());
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateBinding(MemberOAuth2Binding binding) {
        if (binding == null || binding.getId() == null) {
            log.warn("绑定实体或ID为空");
            return false;
        }
        
        log.info("更新 OAuth2 绑定: id={}, provider={}", binding.getId(), binding.getProvider());
        
        boolean result = updateById(binding);
        
        if (result) {
            log.info("OAuth2 绑定更新成功: id={}", binding.getId());
        } else {
            log.error("OAuth2 绑定更新失败: id={}", binding.getId());
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unbind(Long memberId, String provider) {
        if (memberId == null || StringUtils.isBlank(provider)) {
            log.warn("解绑参数为空: memberId={}, provider={}", memberId, provider);
            return false;
        }
        
        log.info("解除 OAuth2 绑定: memberId={}, provider={}", memberId, provider);
        
        MemberOAuth2Binding binding = findByMemberIdAndProvider(memberId, provider);
        if (binding == null) {
            log.warn("未找到要解除的绑定关系: memberId={}, provider={}", memberId, provider);
            return false;
        }
        
        // 软删除
        binding.setDelFlag(1);
        boolean result = updateById(binding);
        
        if (result) {
            log.info("OAuth2 绑定解除成功: memberId={}, provider={}", memberId, provider);
        } else {
            log.error("OAuth2 绑定解除失败: memberId={}, provider={}", memberId, provider);
        }
        
        return result;
    }
}
