package com.rymcu.mortise.system.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OAuth2ClientConfigVO {

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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    private Long updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    private String redirectUri;
    private String appType;
    private String icon;
}
