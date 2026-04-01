package com.rymcu.mortise.system.controller.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OAuth2ClientConfigUpsertRequest {

    private Long id;
    private String registrationId;
    private String clientId;
    private String clientSecret;
    private String clientName;
    private String scopes;
    private String redirectUriTemplate;
    private String clientAuthenticationMethod;
    private String authorizationGrantType;
    private String authorizationUri;
    private String tokenUri;
    private String userInfoUri;
    private String userNameAttribute;
    private String jwkSetUri;
    private Integer status;
    private Integer delFlag;
    private String remark;
    private Long createdBy;
    private LocalDateTime createdTime;
    private Long updatedBy;
    private LocalDateTime updatedTime;
    private String redirectUri;
    private String appType;
    private String icon;
}
