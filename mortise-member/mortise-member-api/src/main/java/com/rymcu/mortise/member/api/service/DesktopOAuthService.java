package com.rymcu.mortise.member.api.service;

import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.member.api.model.DesktopTokenRequest;
import com.rymcu.mortise.member.api.model.DesktopTokenResponse;

/**
 * 桌面端授权桥服务。
 *
 * @author ronger
 */
public interface DesktopOAuthService {

    /**
     * 创建授权码。
     *
     * @return 回调地址
     */
    String authorize(CurrentUser currentUser,
                     String clientId,
                     String redirectUri,
                     String state,
                     String codeChallenge,
                     String codeChallengeMethod,
                     String scope,
                     String deviceName,
                     String deviceFingerprint);

    /**
     * 换取或刷新 Token。
     */
    DesktopTokenResponse token(DesktopTokenRequest request);

    /**
     * 退出桌面端会话。
     */
    Boolean logout(CurrentUser currentUser, String refreshToken, Long sessionId);

    /**
     * 撤销指定桌面端会话及其刷新令牌。
     */
    Boolean revokeSession(Long memberId, Long sessionId);
}
