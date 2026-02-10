package com.rymcu.mortise.member.api.service;

import com.rymcu.mortise.member.api.model.OAuth2LoginResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * OAuth2 会员绑定和登录服务
 *
 * 负责处理 OAuth2 用户信息与本地会员的关联，包括：
 * - 查找或创建会员
 * - 绑定 OAuth2 账号
 * - 生成登录 Token
 *
 * @author ronger
 */
public interface OAuth2MemberBindingService {

    /**
     * 根据 OAuth2 用户信息处理会员登录或注册
     *
     * @param oauth2User     OAuth2 用户对象（从微信等 OAuth2 提供商获取）
     * @param providerType   提供商类型（如 "wechat"）
     * @param registrationId 注册ID
     * @return OAuth2 登录响应，包含会员信息和 JWT Token
     */
    OAuth2LoginResponse bindOrLoginWithOAuth2User(OAuth2User oauth2User, String providerType, String registrationId);

    /**
     * 根据 openid 查找会员
     *
     * @param openid       微信 openid
     * @param providerType 提供商类型（如 "wechat"）
     * @return 如果找到则返回登录响应，否则返回 null
     */
    OAuth2LoginResponse findMemberByOpenid(String openid, String providerType);

    /**
     * 根据 unionid 查找会员
     *
     * @param unionid      微信 unionid
     * @param providerType 提供商类型（如 "wechat"）
     * @return 如果找到则返回登录响应，否则返回 null
     */
    OAuth2LoginResponse findMemberByUnionid(String unionid, String providerType);

    /**
     * 创建新会员并绑定 OAuth2 账号
     *
     * @param oauth2User     OAuth2 用户对象
     * @param providerType   提供商类型
     * @param registrationId 注册ID
     * @return 新创建的会员的登录响应
     */
    OAuth2LoginResponse createMemberAndBind(OAuth2User oauth2User, String providerType, String registrationId);

    /**
     * 为现有会员绑定 OAuth2 账号
     *
     * @param memberId       会员ID
     * @param oauth2User     OAuth2 用户对象
     * @param providerType   提供商类型
     * @param registrationId 注册ID
     * @return 绑定成功则返回登录响应
     */
    OAuth2LoginResponse bindMemberToOAuth2(Long memberId, OAuth2User oauth2User, String providerType, String registrationId);

    /**
     * 更新会员的 OAuth2 用户信息
     *
     * @param memberId  会员ID
     * @param oauth2User OAuth2 用户对象
     * @return 更新成功则返回 true
     */
    Boolean updateMemberFromOAuth2User(Long memberId, OAuth2User oauth2User);
}
