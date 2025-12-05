package com.rymcu.mortise.auth.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 短信验证码认证Token
 * <p>
 * 用于封装手机号和验证码的认证信息，支持多用户表场景
 * </p>
 * <p>
 * 设计参考 UsernamePasswordAuthenticationToken
 * </p>
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2025/11/12
 */
public class VerificationCodeAuthenticationToken extends AbstractAuthenticationToken {

    /**
     * 手机号（认证前）或 UserDetails（认证后）
     */
    private final Object principal;

    /**
     * 验证码（认证前使用，认证后清空）
     */
    private Object credentials;

    /**
     * 创建未认证的 Token（认证前）
     * <p>
     * 用于封装用户提交的手机号和验证码
     * </p>
     *
     * @param mobile 手机号
     * @param smsCode 短信验证码
     */
    public VerificationCodeAuthenticationToken(String mobile, String smsCode) {
        super(null);
        this.principal = mobile;
        this.credentials = smsCode;
        setAuthenticated(false);
    }

    /**
     * 创建已认证的 Token（认证后）
     * <p>
     * 用于封装认证成功后的用户信息
     * </p>
     *
     * @param principal 用户详情（UserDetails）
     * @param authorities 用户权限列表
     */
    public VerificationCodeAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = null; // 认证成功后清空凭证
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    /**
     * 设置认证状态
     * <p>
     * 重写该方法以防止外部随意修改认证状态，只能通过构造函数设置
     * </p>
     *
     * @param isAuthenticated 认证状态
     * @throws IllegalArgumentException 如果尝试设置为已认证
     */
    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException(
                    "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }
        super.setAuthenticated(false);
    }

    /**
     * 清除敏感信息（验证码）
     */
    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }
}
