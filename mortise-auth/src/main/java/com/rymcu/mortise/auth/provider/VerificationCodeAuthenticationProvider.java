package com.rymcu.mortise.auth.provider;

import com.rymcu.mortise.auth.service.CustomUserDetailsService;
import com.rymcu.mortise.auth.service.SmsCodeService;
import com.rymcu.mortise.auth.token.VerificationCodeAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 短信验证码认证提供者（支持多用户表）
 * <p>
 * 负责验证码登录的认证逻辑：
 * <ol>
 *   <li>验证短信验证码是否正确</li>
 *   <li>根据用户类型选择对应的 UserDetailsService</li>
 *   <li>通过手机号加载用户信息</li>
 *   <li>返回认证成功的 Token</li>
 * </ol>
 * </p>
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2025/11/12
 */
@Slf4j
public class VerificationCodeAuthenticationProvider implements AuthenticationProvider {

    /**
     * 验证码服务
     */
    private final SmsCodeService smsCodeService;

    /**
     * 所有 CustomUserDetailsService 实现
     */
    private final List<CustomUserDetailsService> userDetailsServices;

    /**
     * 构造函数
     *
     * @param smsCodeService 验证码服务
     * @param userDetailsServices 用户详情服务列表
     */
    public VerificationCodeAuthenticationProvider(
            SmsCodeService smsCodeService,
            List<CustomUserDetailsService> userDetailsServices) {
        Assert.notNull(smsCodeService, "smsCodeService 不能为空");
        Assert.notEmpty(userDetailsServices, "userDetailsServices 不能为空");
        this.smsCodeService = smsCodeService;
        this.userDetailsServices = userDetailsServices;
        log.info("短信验证码认证提供者已初始化，支持的服务数量: {}", userDetailsServices.size());
    }

    /**
     * 执行认证
     *
     * @param authentication 认证请求（SmsCodeAuthenticationToken）
     * @return 认证成功的 Token（包含用户信息和权限）
     * @throws AuthenticationException 认证失败时抛出
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        VerificationCodeAuthenticationToken authRequest = (VerificationCodeAuthenticationToken) authentication;

        // 1. 提取手机号、验证码和用户类型
        String mobile = (String) authRequest.getPrincipal();
        String smsCode = (String) authRequest.getCredentials();
        String userType = extractUserType(authRequest);

        log.debug("开始短信验证码认证: mobile={}, userType={}", mobile, userType);

        // 2. 验证验证码
        if (!smsCodeService.verify(mobile, smsCode, userType)) {
            log.warn("验证码认证失败: 验证码不正确或已过期 - mobile={}", mobile);
            throw new BadCredentialsException("验证码不正确或已过期");
        }

        log.debug("验证码校验成功: mobile={}", mobile);

        // 3. 根据用户类型选择对应的 UserDetailsService
        CustomUserDetailsService selectedService = selectUserDetailsService(userType);
        if (selectedService == null) {
            log.error("未找到支持用户类型 '{}' 的 UserDetailsService", userType);
            throw new InternalAuthenticationServiceException(
                    "未找到支持用户类型 '" + userType + "' 的认证服务");
        }

        // 4. 通过手机号加载用户信息
        UserDetails user;
        try {
            user = selectedService.loadUserByUsername(mobile);
        } catch (UsernameNotFoundException ex) {
            log.warn("用户不存在: mobile={}", mobile);
            // 清除验证码，防止暴力破解
            smsCodeService.clear(mobile, userType);
            throw new BadCredentialsException("用户不存在");
        } catch (Exception ex) {
            log.error("加载用户信息失败: mobile={}", mobile, ex);
            throw new InternalAuthenticationServiceException("加载用户信息失败", ex);
        }

        log.debug("用户信息加载成功: mobile={}, username={}", mobile, user.getUsername());

        // 5. 检查用户状态
        if (!user.isEnabled()) {
            log.warn("用户已被禁用: mobile={}", mobile);
            throw new BadCredentialsException("用户已被禁用");
        }

        // 6. 清除验证码（认证成功后）
        smsCodeService.clear(mobile, userType);

        // 7. 创建认证成功的 Token
        VerificationCodeAuthenticationToken authResult = new VerificationCodeAuthenticationToken(
                user, user.getAuthorities());
        authResult.setDetails(authRequest.getDetails());

        log.info("短信验证码认证成功: mobile={}, username={}", mobile, user.getUsername());
        return authResult;
    }

    /**
     * 判断是否支持该认证类型
     *
     * @param authentication 认证类型
     * @return true 支持 SmsCodeAuthenticationToken
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return VerificationCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * 从认证对象中提取用户类型
     *
     * @param authentication 认证对象
     * @return 用户类型标识
     */
    private String extractUserType(VerificationCodeAuthenticationToken authentication) {
        Object details = authentication.getDetails();
        if (details instanceof String) {
            return (String) details;
        }
        log.warn("认证对象中未找到用户类型信息，使用默认类型 'system'");
        return "system"; // 默认使用系统用户类型
    }

    /**
     * 根据用户类型选择对应的 UserDetailsService
     *
     * @param userType 用户类型标识
     * @return 对应的 CustomUserDetailsService，如果未找到则返回 null
     */
    private CustomUserDetailsService selectUserDetailsService(String userType) {
        for (CustomUserDetailsService service : userDetailsServices) {
            if (service.supports(userType)) {
                log.debug("为用户类型 '{}' 选择服务: {}", userType, service.getClass().getSimpleName());
                return service;
            }
        }
        log.warn("未找到支持用户类型 '{}' 的 UserDetailsService", userType);
        return null;
    }
}
