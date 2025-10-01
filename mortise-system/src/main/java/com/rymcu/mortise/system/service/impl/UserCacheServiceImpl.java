package com.rymcu.mortise.system.service.impl;

import com.rymcu.mortise.system.constant.SystemCacheConstant;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.model.auth.AuthInfo;
import com.rymcu.mortise.system.service.SystemCacheService;
import com.rymcu.mortise.system.service.MenuService;
import com.rymcu.mortise.system.service.UserService;
import com.rymcu.mortise.common.util.BeanCopierUtil;

import java.util.Set;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 用户缓存服务
 * 演示如何使用 Spring Cache 注解管理用户相关缓存
 *
 * @author ronger
 */
@Slf4j
@Service
public class UserCacheServiceImpl {

    @Resource
    private UserService userService;
    @Resource
    private MenuService menuService;
    @Resource
    private SystemCacheService systemCacheService;

    /**
     * 获取用户信息（带缓存）
     * 首次查询时从数据库获取，后续查询从缓存获取
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Cacheable(value = SystemCacheConstant.USER_INFO_CACHE, key = "#userId", unless = "#result == null")
    public User getCachedUserInfo(Long userId) {
        log.info("从数据库查询用户信息，用户ID: {}", userId);
        return userService.getById(userId);
    }

    /**
     * 获取用户会话信息（带缓存）
     * 包含用户基本信息、权限、角色等
     *
     * @param userId 用户ID
     * @return 用户会话信息
     */
    @Cacheable(value = SystemCacheConstant.USER_SESSION_CACHE, key = "#userId", unless = "#result == null")
    public AuthInfo getCachedUserSession(Long userId) {
        log.info("构建用户会话信息，用户ID: {}", userId);
        User user = userService.getById(userId);
        if (user == null) {
            return null;
        }

        AuthInfo authInfo = new AuthInfo();
        BeanCopierUtil.copy(user, authInfo);
        authInfo.setScope(userService.findUserPermissionsByIdUser(userId));
        authInfo.setRole(userService.findUserRoleListByIdUser(userId));
        authInfo.setLinks(menuService.findLinksByIdUser(userId));
        return authInfo;
    }

    /**
     * 获取用户权限列表（带缓存）
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Cacheable(value = SystemCacheConstant.USER_PERMISSIONS_CACHE, key = "#userId", unless = "#result == null")
    public Set<String> getCachedUserPermissions(Long userId) {
        log.info("查询用户权限，用户ID: {}", userId);
        return userService.findUserPermissionsByIdUser(userId);
    }

    /**
     * 更新用户信息并刷新缓存
     * 使用 @CachePut 注解确保数据库更新后缓存也同步更新
     *
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    @CachePut(value = SystemCacheConstant.USER_INFO_CACHE, key = "#user.id")
    public User updateUserInfo(User user) {
        log.info("更新用户信息并刷新缓存，用户ID: {}", user.getId());
        userService.updateById(user);
        return user;
    }

    /**
     * 清除用户相关的所有缓存
     * 当用户信息发生重大变更时（如角色变更、权限变更等）调用
     *
     * @param userId 用户ID
     */
    @CacheEvict(value = {
            SystemCacheConstant.USER_INFO_CACHE,
            SystemCacheConstant.USER_SESSION_CACHE,
            SystemCacheConstant.USER_PERMISSIONS_CACHE
    }, key = "#userId")
    public void evictUserCache(Long userId) {
        log.info("清除用户相关缓存，用户ID: {}", userId);
        // 使用统一缓存服务进行额外的缓存清理
        systemCacheService.evictUserCache(userId);
    }

    /**
     * 清除所有用户信息缓存
     * 当进行批量用户操作时调用
     */
    @CacheEvict(value = SystemCacheConstant.USER_INFO_CACHE, allEntries = true)
    public void evictAllUserInfoCache() {
        log.info("清除所有用户信息缓存");
    }

    /**
     * 清除所有用户会话缓存
     * 当系统配置发生变更影响所有用户时调用
     */
    @CacheEvict(value = {
            SystemCacheConstant.USER_SESSION_CACHE,
            SystemCacheConstant.USER_PERMISSIONS_CACHE
    }, allEntries = true)
    public void evictAllUserSessionCache() {
        log.info("清除所有用户会话和权限缓存");
        // 使用统一缓存服务进行额外的缓存清理
        systemCacheService.evictAllUserCache();
    }

    /**
     * 使用通用缓存服务的示例方法
     * 演示如何在业务中混合使用注解缓存和编程式缓存
     */
    public void cacheServiceExample(Long userId) {
        // 使用通用缓存服务存储临时数据
        systemCacheService.putString(SystemCacheConstant.TEMP_DATA_CACHE, "temp:" + userId, "临时数据");

        // 获取临时数据
        String tempData = systemCacheService.getString(SystemCacheConstant.TEMP_DATA_CACHE, "temp:" + userId);
        log.info("获取到临时数据: {}", tempData);

        // 存储对象
        User user = userService.getById(userId);
        if (user != null) {
            systemCacheService.putObject(SystemCacheConstant.HOT_DATA_CACHE, "user:" + userId, user);

            // 获取对象
            User cachedUser = systemCacheService.getObject(SystemCacheConstant.HOT_DATA_CACHE, "user:" + userId, User.class);
            log.info("获取到缓存用户: {}", cachedUser != null ? cachedUser.getNickname() : "null");
        }
    }
}
