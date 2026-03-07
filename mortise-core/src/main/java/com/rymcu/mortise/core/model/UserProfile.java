package com.rymcu.mortise.core.model;

/**
 * 用户公开档案信息
 * <p>
 * 作为跨模块传递用户展示信息的载体，由 {@link UserProfileProvider} 实现类提供。
 * 社区、商城等业务模块通过此接口获取用户信息，无需直接依赖 member 模块。
 * </p>
 *
 * @param userId    用户 ID
 * @param nickname  显示昵称
 * @param avatarUrl 头像 URL
 * @param bio       个人简介
 * @param website   个人网站
 * @param location  所在地
 * @param github    GitHub 账号
 * @param weibo     微博账号
 * @param wechat    微信号
 * @param qq        QQ 号
 * @author ronger
 */
public record UserProfile(
        Long userId,
        String nickname,
        String avatarUrl,
        String bio,
        String website,
        String location,
        String github,
        String weibo,
        String wechat,
        String qq
) {
}
