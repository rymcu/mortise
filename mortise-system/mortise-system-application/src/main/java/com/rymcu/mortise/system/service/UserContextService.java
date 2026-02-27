package com.rymcu.mortise.system.service;

import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.model.UserDetailInfo;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

/**
 * 用户上下文服务接口
 * <p>
 * 提供基于 Spring Security 的当前用户信息获取功能
 * <p>
 * 设计理念：
 * 1. 符合 Spring 依赖注入最佳实践
 * 2. 替代静态工具类 UserUtils
 * 3. 易于测试和扩展
 * 4. 可以利用 Spring 特性（缓存、AOP 等）
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2024/4/13
 */
public interface UserContextService {

    /**
     * 获取当前已认证的用户
     *
     * @return 当前用户实体
     * @throws UsernameNotFoundException 当用户未认证时抛出
     */
    User getCurrentUser();

    /**
     * 获取当前用户（Optional 方式）
     * <p>
     * 推荐在不确定用户是否已认证的场景使用
     *
     * @return Optional 包装的用户实体
     */
    Optional<User> getCurrentUserOptional();

    /**
     * 获取当前用户 ID
     *
     * @return 用户 ID，未认证时返回 null
     */
    Long getCurrentUserId();

    /**
     * 获取当前用户名
     *
     * @return 用户名，未认证时返回 null
     */
    String getCurrentUsername();

    /**
     * 获取当前用户账号
     *
     * @return 账号，未认证时返回 null
     */
    String getCurrentAccount();

    /**
     * 检查当前用户是否已认证
     *
     * @return true: 已认证, false: 未认证
     */
    boolean isAuthenticated();

    /**
     * 获取当前用户的 UserDetailInfo
     * <p>
     * 包含完整的用户信息和权限信息
     *
     * @return UserDetailInfo 对象
     * @throws UsernameNotFoundException 当用户未认证时抛出
     */
    UserDetailInfo getCurrentUserDetails();

    /**
     * 检查当前用户是否有指定权限
     *
     * @param authority 权限标识
     * @return true: 有权限, false: 无权限
     */
    boolean hasAuthority(String authority);

    /**
     * 检查当前用户是否有任意一个指定权限
     *
     * @param authorities 权限标识列表
     * @return true: 有任意权限, false: 无权限
     */
    boolean hasAnyAuthority(String... authorities);

    /**
     * 检查当前用户是否有所有指定权限
     *
     * @param authorities 权限标识列表
     * @return true: 有所有权限, false: 缺少权限
     */
    boolean hasAllAuthorities(String... authorities);
}
