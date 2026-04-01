package com.rymcu.mortise.system.repository;

import com.rymcu.mortise.system.entity.Menu;

import java.util.List;

/**
 * 角色菜单关系仓储端口。
 */
public interface RoleMenuRepository {

    List<Menu> findMenusByRoleId(Long roleId);

    boolean replaceMenus(Long roleId, List<Long> menuIds);
}
