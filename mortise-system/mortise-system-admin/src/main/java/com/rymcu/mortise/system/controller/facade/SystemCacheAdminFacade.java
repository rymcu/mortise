package com.rymcu.mortise.system.controller.facade;

import com.rymcu.mortise.core.result.GlobalResult;

public interface SystemCacheAdminFacade {

    GlobalResult<Void> evictUserCache(Long userId);

    GlobalResult<Void> evictDictCache(String dictType);

    GlobalResult<Void> evictAllDictCache();

    GlobalResult<Void> sendVerificationCode(String email);

    GlobalResult<Boolean> verifyCode(String email, String code);
}
