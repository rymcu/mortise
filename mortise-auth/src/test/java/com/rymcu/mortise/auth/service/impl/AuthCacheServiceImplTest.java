package com.rymcu.mortise.auth.service.impl;

import com.rymcu.mortise.auth.constant.AuthCacheConstant;
import com.rymcu.mortise.cache.service.CacheService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthCacheServiceImplTest {

    @Test
    void getMemberIdByRefreshTokenShouldSupportIntegerCacheValue() {
        CacheService cacheService = mock(CacheService.class);
        AuthCacheServiceImpl authCacheService = new AuthCacheServiceImpl();
        ReflectionTestUtils.setField(authCacheService, "cacheService", cacheService);

        when(cacheService.get(AuthCacheConstant.MEMBER_REFRESH_TOKEN_CACHE, "rt", Number.class)).thenReturn(123);

        Long memberId = authCacheService.getMemberIdByRefreshToken("rt");

        assertEquals(123L, memberId);
        verify(cacheService).get(AuthCacheConstant.MEMBER_REFRESH_TOKEN_CACHE, "rt", Number.class);
    }

    @Test
    void getMemberIdByRefreshTokenShouldReturnNullWhenCacheMissed() {
        CacheService cacheService = mock(CacheService.class);
        AuthCacheServiceImpl authCacheService = new AuthCacheServiceImpl();
        ReflectionTestUtils.setField(authCacheService, "cacheService", cacheService);

        when(cacheService.get(AuthCacheConstant.MEMBER_REFRESH_TOKEN_CACHE, "missing", Number.class)).thenReturn(null);

        assertNull(authCacheService.getMemberIdByRefreshToken("missing"));
    }
}
