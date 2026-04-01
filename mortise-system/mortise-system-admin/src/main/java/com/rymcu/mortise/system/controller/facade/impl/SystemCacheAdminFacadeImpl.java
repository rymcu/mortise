package com.rymcu.mortise.system.controller.facade.impl;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.controller.facade.SystemCacheAdminFacade;
import com.rymcu.mortise.system.service.SystemCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class SystemCacheAdminFacadeImpl implements SystemCacheAdminFacade {

    private final SystemCacheService systemCacheService;

    public SystemCacheAdminFacadeImpl(SystemCacheService systemCacheService) {
        this.systemCacheService = systemCacheService;
    }

    @Override
    public GlobalResult<Void> evictUserCache(Long userId) {
        systemCacheService.evictUserAllCache(userId);
        return GlobalResult.success();
    }

    @Override
    public GlobalResult<Void> evictDictCache(String dictType) {
        systemCacheService.evictDictData(dictType);
        return GlobalResult.success();
    }

    @Override
    public GlobalResult<Void> evictAllDictCache() {
        systemCacheService.evictAllDictData();
        return GlobalResult.success();
    }

    @Override
    public GlobalResult<Void> sendVerificationCode(String email) {
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        systemCacheService.cacheVerificationCode(email, code, Duration.ofMinutes(5));
        log.info("验证码已生成并缓存");
        return GlobalResult.success();
    }

    @Override
    public GlobalResult<Boolean> verifyCode(String email, String code) {
        String cachedCode = systemCacheService.getVerificationCode(email);
        if (cachedCode == null) {
            return GlobalResult.error("验证码已过期");
        }

        boolean isValid = cachedCode.equals(code);
        if (isValid) {
            systemCacheService.evictVerificationCode(email);
        }
        return GlobalResult.success(isValid);
    }
}
