package com.rymcu.mortise.system.controller.assembler;

import com.rymcu.mortise.auth.entity.Oauth2ClientConfig;
import com.rymcu.mortise.system.controller.request.OAuth2ClientConfigUpsertRequest;
import com.rymcu.mortise.system.controller.vo.OAuth2ClientConfigVO;

public final class OAuth2ClientConfigAdminAssembler {

    private OAuth2ClientConfigAdminAssembler() {
    }

    public static OAuth2ClientConfigVO toVO(Oauth2ClientConfig config) {
        if (config == null) {
            return null;
        }
        OAuth2ClientConfigVO vo = new OAuth2ClientConfigVO();
        vo.setId(config.getId());
        vo.setRegistrationId(config.getRegistrationId());
        vo.setClientId(config.getClientId());
        vo.setClientSecret(config.getClientSecret());
        vo.setClientName(config.getClientName());
        vo.setScopes(config.getScopes());
        vo.setRedirectUriTemplate(config.getRedirectUriTemplate());
        vo.setClientAuthenticationMethod(config.getClientAuthenticationMethod());
        vo.setAuthorizationGrantType(config.getAuthorizationGrantType());
        vo.setAuthorizationUri(config.getAuthorizationUri());
        vo.setTokenUri(config.getTokenUri());
        vo.setUserInfoUri(config.getUserInfoUri());
        vo.setUserNameAttribute(config.getUserNameAttribute());
        vo.setJwkSetUri(config.getJwkSetUri());
        vo.setStatus(config.getStatus());
        vo.setDelFlag(config.getDelFlag());
        vo.setRemark(config.getRemark());
        vo.setCreatedBy(config.getCreatedBy());
        vo.setCreatedTime(config.getCreatedTime());
        vo.setUpdatedBy(config.getUpdatedBy());
        vo.setUpdatedTime(config.getUpdatedTime());
        vo.setRedirectUri(config.getRedirectUri());
        vo.setAppType(config.getAppType());
        vo.setIcon(config.getIcon());
        return vo;
    }

    public static Oauth2ClientConfig toEntity(OAuth2ClientConfigUpsertRequest request) {
        Oauth2ClientConfig config = new Oauth2ClientConfig();
        config.setId(request.getId());
        config.setRegistrationId(request.getRegistrationId());
        config.setClientId(request.getClientId());
        config.setClientSecret(request.getClientSecret());
        config.setClientName(request.getClientName());
        config.setScopes(request.getScopes());
        config.setRedirectUriTemplate(request.getRedirectUriTemplate());
        config.setClientAuthenticationMethod(request.getClientAuthenticationMethod());
        config.setAuthorizationGrantType(request.getAuthorizationGrantType());
        config.setAuthorizationUri(request.getAuthorizationUri());
        config.setTokenUri(request.getTokenUri());
        config.setUserInfoUri(request.getUserInfoUri());
        config.setUserNameAttribute(request.getUserNameAttribute());
        config.setJwkSetUri(request.getJwkSetUri());
        config.setStatus(request.getStatus());
        config.setDelFlag(request.getDelFlag());
        config.setRemark(request.getRemark());
        config.setCreatedBy(request.getCreatedBy());
        config.setCreatedTime(request.getCreatedTime());
        config.setUpdatedBy(request.getUpdatedBy());
        config.setUpdatedTime(request.getUpdatedTime());
        config.setRedirectUri(request.getRedirectUri());
        config.setAppType(request.getAppType());
        config.setIcon(request.getIcon());
        return config;
    }
}
