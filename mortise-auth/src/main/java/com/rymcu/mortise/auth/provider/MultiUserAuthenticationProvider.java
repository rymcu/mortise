package com.rymcu.mortise.auth.provider;

import com.rymcu.mortise.auth.enumerate.UserType;
import com.rymcu.mortise.auth.service.CustomUserDetailsService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 多用户系统认证提供者
 * <p>
 * 支持多个用户表的认证场景，根据认证请求中的用户类型标识，
 * 选择对应的 UserDetailsService 进行用户加载和认证。
 * </p>
 * <p>
 * 实现参考：<a href="https://www.cnblogs.com/qq545505061/p/17511591.html">...</a>
 * </p>
 * <p>
 * 核心逻辑：
 * <ol>
 *   <li>从 UsernamePasswordAuthenticationToken 的 details 中获取用户类型</li>
 *   <li>遍历所有注册的 CustomUserDetailsService，找到支持该类型的服务</li>
 *   <li>使用选定的服务加载用户信息并进行密码校验</li>
 * </ol>
 * </p>
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2025/11/11
 */
@Slf4j
public class MultiUserAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    /**
     * 密码编码器，用于密码验证
     */
    private PasswordEncoder passwordEncoder;

    /**
     * 自定义用户详情服务列表
     * <p>
     * 每个服务对应一种用户类型（如系统用户、会员用户等）
     * </p>
     * -- GETTER --
     *  获取自定义用户详情服务列表
     *

     */
    @Getter
    private List<CustomUserDetailsService> userDetailsServices;

    /**
     * 是否隐藏用户不存在异常
     * <p>
     * true: 返回 "Bad credentials" 统一错误信息
     * false: 直接抛出 UsernameNotFoundException
     * </p>
     * -- SETTER --
     *  设置是否隐藏用户不存在异常
     *  true 表示隐藏，false 表示不隐藏
     */
    @Setter
    private boolean hideUserNotFoundExceptions = true;

    /**
     * 构造函数
     *
     * @param userDetailsServices 自定义用户详情服务列表
     * @param passwordEncoder 密码编码器
     */
    public MultiUserAuthenticationProvider(
            List<CustomUserDetailsService> userDetailsServices,
            PasswordEncoder passwordEncoder) {
        Assert.notEmpty(userDetailsServices, "userDetailsServices 不能为空");
        Assert.notNull(passwordEncoder, "passwordEncoder 不能为空");
        this.userDetailsServices = userDetailsServices;
        this.passwordEncoder = passwordEncoder;
        log.info("多用户认证提供者已初始化，支持的服务数量: {}", userDetailsServices.size());
    }

    /**
     * 附加认证检查 - 密码验证
     * <p>
     * 在用户加载成功后，验证提供的密码是否正确
     * </p>
     *
     * @param userDetails 从数据库加载的用户详情
     * @param authentication 认证请求
     * @throws AuthenticationException 认证失败时抛出
     */
    @Override
    protected void additionalAuthenticationChecks(
            UserDetails userDetails,
            UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

        // 时序攻击防护：无论密码是否匹配都执行相同的操作
        if (authentication.getCredentials() == null) {
            log.debug("认证失败: 密码为空");
            throw new BadCredentialsException(this.messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "Bad credentials"));
        }

        String presentedPassword = authentication.getCredentials().toString();

        // 验证密码
        if (!this.passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
            log.debug("认证失败: 密码不匹配 - username={}", userDetails.getUsername());
            throw new BadCredentialsException(this.messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "Bad credentials"));
        }

        log.debug("密码验证成功: username={}", userDetails.getUsername());
    }

    /**
     * 检索用户信息
     * <p>
     * 核心方法：根据用户类型选择对应的 UserDetailsService 加载用户
     * </p>
     *
     * @param username 用户名
     * @param authentication 认证请求（从 details 中获取用户类型）
     * @return 用户详情
     * @throws AuthenticationException 认证失败时抛出
     */
    @Override
    protected UserDetails retrieveUser(
            String username,
            UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

        try {
            // 1. 从 authentication.getDetails() 中获取用户类型
            String userType = extractUserType(authentication);
            log.debug("开始检索用户: username={}, userType={}", username, userType);

            // 2. 遍历所有 UserDetailsService，找到支持该类型的服务
            UserDetails loadedUser = null;
            CustomUserDetailsService selectedService = null;

            for (CustomUserDetailsService service : this.userDetailsServices) {
                if (service.supports(userType)) {
                    selectedService = service;
                    log.debug("选中用户服务: {}", service.getClass().getSimpleName());
                    loadedUser = service.loadUserByUsername(username);
                    break;
                }
            }

            // 3. 如果没有找到支持的服务或用户不存在
            if (loadedUser == null) {
                log.warn("用户加载失败: 未找到支持用户类型 '{}' 的服务", userType);
                throw new InternalAuthenticationServiceException(
                        "未找到支持用户类型 '" + userType + "' 的认证服务");
            }

            log.debug("用户加载成功: username={}, service={}", username,
                    selectedService.getClass().getSimpleName());
            return loadedUser;

        } catch (UsernameNotFoundException ex) {
            // 根据配置决定是否隐藏用户不存在异常
            if (this.hideUserNotFoundExceptions) {
                throw new BadCredentialsException(this.messages.getMessage(
                        "AbstractUserDetailsAuthenticationProvider.badCredentials",
                        "Bad credentials"));
            }
            throw ex;
        } catch (InternalAuthenticationServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("用户检索过程中发生异常", ex);
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * 从认证对象中提取用户类型
     * <p>
     * 用户类型存储在 UsernamePasswordAuthenticationToken 的 details 属性中
     * </p>
     *
     * @param authentication 认证对象
     * @return 用户类型标识（如 "system", "member"）
     */
    private String extractUserType(UsernamePasswordAuthenticationToken authentication) {
        Object details = authentication.getDetails();
        if (details instanceof String) {
            return (String) details;
        }

        log.warn("认证对象中未找到有效的用户类型信息，使用默认类型 'system'");
        return UserType.SYSTEM.getCode(); // 默认使用系统用户类型
    }

    /**
     * 设置密码编码器
     *
     * @param passwordEncoder 密码编码器
     */
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        Assert.notNull(passwordEncoder, "passwordEncoder 不能为空");
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 设置自定义用户详情服务列表
     *
     * @param userDetailsServices 用户详情服务列表
     */
    public void setUserDetailsServices(List<CustomUserDetailsService> userDetailsServices) {
        Assert.notEmpty(userDetailsServices, "userDetailsServices 不能为空");
        this.userDetailsServices = userDetailsServices;
    }

}
