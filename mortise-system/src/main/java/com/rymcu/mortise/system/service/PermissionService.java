package com.rymcu.mortise.system.service;

import java.util.Set;

/**
 * 权限服务接口
 * 专门处理用户权限相关的业务逻辑，避免服务间的循环依赖
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2025/9/29
 */
public interface PermissionService {
    
    /**
     * 获取用户的所有权限
     * 
     * @param idUser 用户ID
     * @return 用户权限集合
     */
    Set<String> findUserPermissionsByIdUser(Long idUser);
    
    /**
     * 获取用户的角色权限
     * 
     * @param idUser 用户ID
     * @return 角色权限集合
     */
    Set<String> findUserRolePermissionsByIdUser(Long idUser);
}