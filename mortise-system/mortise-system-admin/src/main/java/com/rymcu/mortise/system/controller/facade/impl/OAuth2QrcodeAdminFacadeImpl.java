package com.rymcu.mortise.system.controller.facade.impl;

import com.rymcu.mortise.auth.enumerate.QrcodeState;
import com.rymcu.mortise.auth.service.AuthCacheService;
import com.rymcu.mortise.auth.support.UnifiedOAuth2AuthorizationRequestResolver;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.controller.facade.OAuth2QrcodeAdminFacade;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OAuth2QrcodeAdminFacadeImpl implements OAuth2QrcodeAdminFacade {

    private final UnifiedOAuth2AuthorizationRequestResolver authorizationRequestResolver;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository;
    private final AuthCacheService authCacheService;

    public OAuth2QrcodeAdminFacadeImpl(
            UnifiedOAuth2AuthorizationRequestResolver authorizationRequestResolver,
            ClientRegistrationRepository clientRegistrationRepository,
            AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository,
            AuthCacheService authCacheService) {
        this.authorizationRequestResolver = authorizationRequestResolver;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.authorizationRequestRepository = authorizationRequestRepository;
        this.authCacheService = authCacheService;
    }

    @Override
    public GlobalResult<Map<String, String>> getWeChatQRCode(
            String registrationId,
            HttpServletRequest request,
            HttpServletResponse response) {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(registrationId);
        if (clientRegistration == null) {
            throw new IllegalArgumentException("Unknown client registrationId: " + registrationId);
        }

        OAuth2AuthorizationRequest authRequest = authorizationRequestResolver.resolve(request, registrationId);
        if (authRequest == null) {
            throw new IllegalStateException("Could not resolve AuthorizationRequest for registrationId: " + registrationId);
        }

        authorizationRequestRepository.saveAuthorizationRequest(authRequest, request, response);
        String state = authRequest.getState();
        if (StringUtils.isNotBlank(state)) {
            authCacheService.storeOAuth2QrcodeState(state, QrcodeState.WAITED.getValue());
        }

        Map<String, String> result = new HashMap<>();
        result.put("state", state);
        result.put("appid", authRequest.getClientId());
        result.put("scope", "snsapi_login");
        result.put("redirectUri", authRequest.getRedirectUri());
        result.put("authorizationUri", authRequest.getAuthorizationRequestUri());
        return GlobalResult.success(result);
    }

    @Override
    public GlobalResult<Map<String, Object>> getStateQRCode(String state) {
        int qrcodeState = authCacheService.getOAuth2QrcodeState(state);
        Map<String, Object> result = new HashMap<>();
        result.put("state", state);
        result.put("qrcodeState", qrcodeState);
        return GlobalResult.success(result);
    }
}
