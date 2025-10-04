package com.rymcu.mortise.system.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.common.enumerate.DefaultFlag;
import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.system.entity.*;
import com.rymcu.mortise.system.mapper.RoleMapper;
import com.rymcu.mortise.system.mapper.RoleMenuMapper;
import com.rymcu.mortise.system.mapper.UserRoleMapper;
import com.rymcu.mortise.system.model.BindRoleMenuInfo;
import com.rymcu.mortise.system.model.BindRoleUserInfo;
import com.rymcu.mortise.system.model.RoleSearch;
import com.rymcu.mortise.system.service.RoleService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

    @Resource
    private RoleMenuMapper roleMenuMapper;
    @Resource
    private UserRoleMapper userRoleMapper;

    @Override
    public List<Role> findRolesByIdUser(Long idUser) {
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
            // 更新默认角色标识
            if (role.getIsDefault() != null) {
                oldRole.setIsDefault(role.getIsDefault());
            }
            oldRole.setUpdatedTime(LocalDateTime.now());
            return mapper.update(oldRole) > 0;
        }
        return mapper.insertSelective(role) > 0;
    }

    @Override
    public Page<Role> findRoles(Page<Role> page, RoleSearch search) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(ROLE.ID, ROLE.LABEL, ROLE.PERMISSION, ROLE.STATUS, ROLE.IS_DEFAULT, ROLE.CREATED_TIME)
                .and(ROLE.LABEL.eq(search.getQuery(), StringUtils.isNotBlank(search.getQuery())));
        return mapper.paginate(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean bindRoleMenu(BindRoleMenuInfo bindRoleMenuInfo) {
        // 先删除原有关系
        QueryWrapper deleteWrapper = QueryWrapper.create()
                .where(ROLE_MENU.ID_MORTISE_ROLE.eq(bindRoleMenuInfo.getIdRole()));
        roleMenuMapper.deleteByQuery(deleteWrapper);

        // 批量插入新关系
        if (bindRoleMenuInfo.getIdMenus() != null && !bindRoleMenuInfo.getIdMenus().isEmpty()) {
            List<RoleMenu> roleMenus = bindRoleMenuInfo.getIdMenus().stream().map(idMenu -> new RoleMenu(bindRoleMenuInfo.getIdRole(), idMenu)).toList();
            int num = roleMenuMapper.insertBatch(roleMenus);
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
    public List<Menu> findMenusByIdRole(Long idRole) {
        // 使用 Mapper 方法，避免参数绑定问题
        return roleMenuMapper.findMenusByIdRole(idRole);
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

    /**
     * 查找默认角色（用于新用户注册）
     *
     * @return 默认角色
     */
    @Override
    public List<Role> findDefaultRole() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(ROLE.ID, ROLE.LABEL, ROLE.PERMISSION)
                .where(ROLE.IS_DEFAULT.eq(DefaultFlag.YES.getValue()));
        return mapper.selectListByQuery(queryWrapper);
    }

    @Override
    public Boolean bindRoleUser(BindRoleUserInfo bindRoleUserInfo) {
        // 删除原有关系
        QueryWrapper deleteWrapper = QueryWrapper.create()
                .where(USER_ROLE.ID_MORTISE_ROLE.eq(bindRoleUserInfo.getIdRole()));
        userRoleMapper.deleteByQuery(deleteWrapper);

        // 批量插入新关系
        if (bindRoleUserInfo.getIdUsers() != null && !bindRoleUserInfo.getIdUsers().isEmpty()) {
            List<UserRole> userRoles = bindRoleUserInfo.getIdUsers().stream().map(idUser -> new UserRole(idUser, bindRoleUserInfo.getIdRole())).toList();
            int num = userRoleMapper.insertBatch(userRoles);
            return num == bindRoleUserInfo.getIdUsers().size();
        }
        return true;
    }

    @Override
    public List<User> findUsersByIdRole(Long idRole) {
        // 使用 Mapper 方法，避免参数绑定问题
        return userRoleMapper.findUsersByIdRole(idRole);
    }
}
