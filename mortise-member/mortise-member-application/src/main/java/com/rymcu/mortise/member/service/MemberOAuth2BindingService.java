package com.rymcu.mortise.member.service;

import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.member.entity.MemberOAuth2Binding;

/**
 * 会员 OAuth2 绑定服务
 * <p>
 * 提供会员与 OAuth2 账号绑定关系的管理功能
 * <p>
 * 参考 mortise-system 模块的 {@code UserOAuth2BindingService} 接口设计
 *
 * @author ronger
 * @since 1.0.0
 */
public interface MemberOAuth2BindingService extends IService<MemberOAuth2Binding> {

    /**
     * 根据提供商和 OpenID 查找绑定关系
     *
     * @param provider OAuth2 提供商（如 wechat、wechat_mp、qq 等）
     * @param openId   OpenID
     * @return 绑定关系，未找到返回 null
     */
    MemberOAuth2Binding findByProviderAndOpenId(String provider, String openId);

    /**
     * 根据提供商和 UnionID 查找绑定关系
     * 主要用于微信开放平台场景，通过 UnionID 关联不同应用的用户
     *
     * @param provider OAuth2 提供商（如 wechat、wechat_mp 等）
     * @param unionId  微信 UnionID
     * @return 绑定关系，未找到返回 null
     */
    MemberOAuth2Binding findByProviderAndUnionId(String provider, String unionId);

    /**
     * 根据会员ID和提供商查找绑定关系
     *
     * @param memberId 会员ID
     * @param provider OAuth2 提供商
     * @return 绑定关系，未找到返回 null
     */
    MemberOAuth2Binding findByMemberIdAndProvider(Long memberId, String provider);

    /**
     * 创建 OAuth2 绑定关系
     *
     * @param binding 绑定实体
     * @return 是否成功
     */
    Boolean createBinding(MemberOAuth2Binding binding);

    /**
     * 更新绑定信息（如 access_token、用户信息等）
     *
     * @param binding 绑定实体
     * @return 是否成功
     */
    Boolean updateBinding(MemberOAuth2Binding binding);

    /**
     * 解除会员的 OAuth2 绑定
     *
     * @param memberId 会员ID
     * @param provider OAuth2 提供商
     * @return 是否成功
     */
    Boolean unbind(Long memberId, String provider);
}
