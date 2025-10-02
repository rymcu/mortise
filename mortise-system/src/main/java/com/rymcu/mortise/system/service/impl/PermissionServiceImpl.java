package com.rymcu.mortise.system.service.impl;

import com.rymcu.mortise.system.entity.Menu;
import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.system.service.MenuService;
import com.rymcu.mortise.system.service.PermissionService;
import com.rymcu.mortise.system.service.RoleService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 权限服务实现类
 * 专门处理用户权限相关的业务逻辑，避免服务间的循环依赖
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2025/9/29
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Resource
    private MenuService menuService;

    @Resource
    private RoleService roleService;

    @Override
    public Set<String> findUserPermissionsByIdUser(Long idUser) {
        Set<String> permissions = new HashSet<>();

        // 获取用户菜单权限
        List<Menu> menus = menuService.findMenusByIdUser(idUser);
        for (Menu menu : menus) {
            if (StringUtils.isNotBlank(menu.getPermission())) {
                permissions.add(menu.getPermission());
            }
        }

        // 获取角色权限
        permissions.addAll(findUserRolePermissionsByIdUser(idUser));

        return permissions;
    }

    @Override
    public Set<String> findUserRolePermissionsByIdUser(Long idUser) {
        List<Role> roles = roleService.findRolesByIdUser(idUser);
        Set<String> permissions = new HashSet<>();
        for (Role role : roles) {
            if (StringUtils.isNotBlank(role.getPermission())) {
                // 为角色权限添加 ROLE_ 前缀，以支持 @PreAuthorize("hasRole('admin')")
                permissions.add("ROLE_" + role.getPermission());
            }
        }
        return permissions;
    }
}
