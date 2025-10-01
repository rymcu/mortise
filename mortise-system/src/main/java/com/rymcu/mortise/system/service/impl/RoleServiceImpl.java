package com.rymcu.mortise.system.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.system.mapper.RoleMapper;
import com.rymcu.mortise.system.model.BindRoleMenuInfo;
import com.rymcu.mortise.system.model.RoleSearch;
import com.rymcu.mortise.system.service.RoleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.rymcu.mortise.system.entity.table.RoleMenuTableDef.ROLE_MENU;
import static com.rymcu.mortise.system.entity.table.RoleTableDef.ROLE;
import static com.rymcu.mortise.system.entity.table.UserRoleTableDef.USER_ROLE;

/**
 * Created on 2024/4/13 22:06.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service.impl
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public List<Role> findRolesByIdUser(Long idUser) {
        // 原 SQL: select tr.id, tr.label, tr.permission from mortise_user_role tur
        // left join mortise_role tr on tur.id_mortise_role = tr.id where tur.id_mortise_user = #{idUser}
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(ROLE.ID, ROLE.LABEL, ROLE.PERMISSION)
                .from(ROLE.as("tr"))
                .join(USER_ROLE.as("tur")).on(USER_ROLE.ID_MORTISE_ROLE.eq(ROLE.ID))
                .where(USER_ROLE.ID_MORTISE_USER.eq(idUser));
        return mapper.selectListByQuery(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRole(Role role) {
        boolean isUpdate = role.getId() != null;
        if (isUpdate) {
            Role oldRole = mapper.selectOneById(role.getId());
            if (oldRole == null) {
                throw new ServiceException("数据不存在");
            }
            oldRole.setLabel(role.getLabel());
            oldRole.setPermission(role.getPermission());
            oldRole.setStatus(role.getStatus());
            oldRole.setUpdatedTime(LocalDateTime.now());
            return mapper.update(oldRole) > 0;
        }
        return mapper.insertSelective(role) > 0;
    }

    @Override
    public Page<Role> findRoles(Page<Role> page, RoleSearch search) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select("id", "label", "permission", "status", "created_time")
                .eq("label", search.getQuery(), StringUtils.isNotBlank(search.getQuery()));
        return mapper.paginate(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean bindRoleMenu(BindRoleMenuInfo bindRoleMenuInfo) {
        int num = 0;
        // 先删除原有关系
        QueryWrapper deleteWrapper = QueryWrapper.create()
                .where(ROLE_MENU.ID_MORTISE_ROLE.eq(bindRoleMenuInfo.getIdRole()));
        Db.deleteByQuery(ROLE_MENU.getTableName(), deleteWrapper);

        // 批量插入新关系
        if (bindRoleMenuInfo.getIdMenus() != null && !bindRoleMenuInfo.getIdMenus().isEmpty()) {
            List<Row> roleMenus = bindRoleMenuInfo.getIdMenus().stream().map(idMenu -> {
                Row row = new Row();
                row.set(ROLE_MENU.ID_MORTISE_ROLE, bindRoleMenuInfo.getIdRole());
                row.set(ROLE_MENU.ID_MORTISE_MENU, idMenu);
                return row;
            }).toList();
            int[] result = Db.insertBatch(ROLE_MENU.getTableName(), roleMenus);
            for (int i : result) {
                num += i;
            }
            return num == bindRoleMenuInfo.getIdMenus().size();
        }
        return true;
    }

    @Override
    public Boolean updateStatus(Long idRole, Integer status) {
        Role role = UpdateEntity.of(Role.class, idRole);
        role.setStatus(status);
        return mapper.update(role) > 0;
    }

    @Override
    public Set<Long> findRoleMenus(Long idRole) {
        // 原 SQL: select id_mortise_menu from mortise_role_menu where id_mortise_role = #{idRole}
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(ROLE_MENU.ID_MORTISE_MENU)
                .from(ROLE_MENU)
                .where(ROLE_MENU.ID_MORTISE_ROLE.eq(idRole));
        List<Row> rows = Db.selectListByQuery(queryWrapper);
        return rows.stream()
                .map(row -> row.getLong(ROLE_MENU.ID_MORTISE_MENU.getName()))
                .collect(Collectors.toSet());
    }

    @Override
    public Boolean deleteRole(Long idRole) {
        return mapper.deleteById(idRole) > 0;
    }

    @Override
    public Boolean batchDeleteRoles(List<Long> idRoleList) {
        if (idRoleList == null || idRoleList.isEmpty()) {
            return false;
        }
        return mapper.deleteBatchByIds(idRoleList) > 0;
    }

    @Override
    public Role findById(Long idRole) {
        return mapper.selectOneById(idRole);
    }

    /**
     * 根据权限查找角色
     * 使用 QueryWrapper 替代 Mapper 方法
     */
    public Role findRoleByPermission(String permission) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(ROLE.ID, ROLE.LABEL, ROLE.PERMISSION)
                .where(ROLE.PERMISSION.eq(permission));
        return mapper.selectOneByQuery(queryWrapper);
    }
}
