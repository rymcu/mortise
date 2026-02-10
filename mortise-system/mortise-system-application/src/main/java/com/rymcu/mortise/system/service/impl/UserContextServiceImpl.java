package com.rymcu.mortise.system.service.impl;

import com.rymcu.mortise.core.result.ResultCode;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.model.auth.UserDetailInfo;
import com.rymcu.mortise.system.service.UserContextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 用户上下文服务实现
 * <p>
 * 提供基于 Spring Security 的当前用户信息获取功能
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2024/4/13
 */
@Slf4j
@Service
public class UserContextServiceImpl implements UserContextService {

    @Override
    public User getCurrentUser() {
        Authentication authentication = getAuthentication();

        if (!isAuthenticationValid(authentication)) {
            log.warn("当前请求未认证或为匿名用户");
            throw new UsernameNotFoundException(ResultCode.UNAUTHORIZED.getMessage());
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailInfo userDetails) {
            User user = userDetails.getUser();
            if (user != null) {
                return user;
            }
            log.error("UserDetailInfo 中的 User 实体为 null");
            throw new UsernameNotFoundException("用户信息不完整");
        }

        log.error("Principal 类型不是 UserDetailInfo: {}", principal.getClass().getName());
        log.error("请确保 JWT 过滤器将 UserDetailInfo 设置到 SecurityContext");
        throw new UsernameNotFoundException("无法获取用户信息，请检查认证配置");
    }

    @Override
    public Optional<User> getCurrentUserOptional() {
        try {
            return Optional.of(getCurrentUser());
        } catch (UsernameNotFoundException e) {
            log.debug("获取当前用户失败: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Long getCurrentUserId() {
        return getCurrentUserOptional()
                .map(User::getId)
                .orElse(null);
    }

    @Override
    public String getCurrentUsername() {
        Authentication authentication = getAuthentication();

        if (!isAuthenticationValid(authentication)) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailInfo) {
            return ((UserDetailInfo) principal).getUsername();
        }

        return null;
    }

    @Override
    public String getCurrentAccount() {
        return getCurrentUserOptional()
                .map(User::getAccount)
                .orElse(null);
    }

    @Override
    public boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return isAuthenticationValid(authentication);
    }

    @Override
    public UserDetailInfo getCurrentUserDetails() {
        Authentication authentication = getAuthentication();

        if (!isAuthenticationValid(authentication)) {
            log.warn("当前请求未认证或为匿名用户");
            throw new UsernameNotFoundException(ResultCode.UNAUTHORIZED.getMessage());
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailInfo) {
            return (UserDetailInfo) principal;
        }

        log.error("Principal 类型不是 UserDetailInfo: {}", principal.getClass().getName());
        throw new UsernameNotFoundException("无法获取用户详情");
    }

    @Override
    public boolean hasAuthority(String authority) {
        if (authority == null || authority.trim().isEmpty()) {
            return false;
        }

        Authentication authentication = getAuthentication();

        if (!isAuthenticationValid(authentication)) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }

    @Override
    public boolean hasAnyAuthority(String... authorities) {
        if (authorities == null || authorities.length == 0) {
            return false;
        }

        Authentication authentication = getAuthentication();

        if (!isAuthenticationValid(authentication)) {
            return false;
        }

        for (String authority : authorities) {
            if (authority != null && hasAuthority(authority)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAllAuthorities(String... authorities) {
        if (authorities == null || authorities.length == 0) {
            return false;
        }

        Authentication authentication = getAuthentication();

        if (!isAuthenticationValid(authentication)) {
            return false;
        }

        for (String authority : authorities) {
            if (authority != null && !hasAuthority(authority)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取 Authentication 对象
     *
     * @return Authentication 对象，可能为 null
     */
    private Authentication getAuthentication() {
        try {
            return SecurityContextHolder.getContext().getAuthentication();
        } catch (Exception e) {
            log.error("获取 Authentication 失败", e);
            return null;
        }
    }

    /**
     * 验证 Authentication 是否有效
     *
     * @param authentication Authentication 对象
     * @return true: 有效, false: 无效
     */
    private boolean isAuthenticationValid(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
