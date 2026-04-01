package com.rymcu.mortise.system.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.system.entity.Menu;
import com.rymcu.mortise.system.infra.persistence.entity.RoleMenuPO;
import com.rymcu.mortise.system.mapper.RoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.rymcu.mortise.system.infra.persistence.entity.table.RoleMenuPOTableDef.ROLE_MENU_PO;

/**
 * MyBatis-Flex 角色菜单关系仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class RoleMenuRepositoryImpl implements RoleMenuRepository {

    private final RoleMenuMapper roleMenuMapper;

    @Override
    public List<Menu> findMenusByRoleId(Long roleId) {
        return roleMenuMapper.findMenusByIdRole(roleId);
    }

    @Override
    public boolean replaceMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.deleteByQuery(QueryWrapper.create().where(ROLE_MENU_PO.ID_MORTISE_ROLE.eq(roleId)));
        if (menuIds == null || menuIds.isEmpty()) {
            return true;
        }
        List<RoleMenuPO> roleMenus = menuIds.stream()
                .map(menuId -> new RoleMenuPO(roleId, menuId))
                .toList();
        int inserted = roleMenuMapper.insertBatch(roleMenus);
        return inserted == menuIds.size();
    }
}
