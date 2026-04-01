package com.rymcu.mortise.system.repository;

import com.rymcu.mortise.system.entity.Menu;

import java.util.List;

/**
 * 菜单仓储端口。
 */
public interface MenuRepository {

    List<Menu> findMenusByUserId(Long userId);

    List<Menu> findLinksByUserIdAndParentId(Long userId, Long parentId);

    List<Menu> findAllLinksByUserId(Long userId);

    List<Menu> findTreeMenus(Long parentId);

    Menu findById(Long menuId);

    boolean save(Menu menu);

    boolean saveAll(List<Menu> menus);

    boolean update(Menu menu);

    boolean updateStatus(Long menuId, Integer status);

    boolean deleteById(Long menuId);

    boolean deleteByIds(List<Long> menuIds);

    long count();

    long countEnabled();
}
