package com.rymcu.mortise.auth.service;

import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 自定义用户详情服务接口
 * <p>
 * 扩展 Spring Security 的 UserDetailsService 接口，增加策略选择方法，
 * 用于支持多用户表（系统用户、会员用户等）登录的场景。
 * </p>
 * <p>
 * 参考实现：<a href="https://www.cnblogs.com/qq545505061/p/17511591.html">...</a>
 * </p>
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2025/11/11
 */
public interface CustomUserDetailsService extends UserDetailsService {

    /**
     * 判断当前 UserDetailsService 是否支持指定的用户类型
     * <p>
     * 用于在多用户表场景下，根据用户类型选择对应的 UserDetailsService 实现。
     * </p>
     *
     * @param userType 用户类型标识（如 "system" 表示系统用户，"member" 表示会员用户）
     * @return true 表示支持该用户类型，false 表示不支持
     */
    Boolean supports(String userType);
}
