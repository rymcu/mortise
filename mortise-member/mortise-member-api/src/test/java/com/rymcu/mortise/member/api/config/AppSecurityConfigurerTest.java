package com.rymcu.mortise.member.api.config;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AppSecurityConfigurerTest {

    @Test
    void shouldExposeResetPasswordAsPublicAuthEndpoint() {
        assertTrue(Arrays.asList(AppSecurityConfigurer.PUBLIC_AUTH_ENDPOINTS)
                .contains("/api/v1/app/auth/reset-password"));
    }
}
