package com.rymcu.mortise.service.impl;

import com.rymcu.mortise.core.service.AbstractService;
import com.rymcu.mortise.entity.Role;
import com.rymcu.mortise.mapper.RoleMapper;
import com.rymcu.mortise.model.BindRoleMenuInfo;
import com.rymcu.mortise.model.RoleSearch;
import com.rymcu.mortise.service.RoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created on 2024/4/13 22:06.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service.impl
 */
@Service
public class RoleServiceImpl extends AbstractService<Role> implements RoleService {

    @Resource
    private RoleMapper roleMapper;

    @Override
    public List<Role> findRolesByIdUser(Long idUser) {
        return roleMapper.selectRolesByIdUser(idUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRole(Role role) {
        Role oldRole = roleMapper.selectByPrimaryKey(role.getIdRole());
        if (Objects.nonNull(oldRole)) {
            oldRole.setLabel(role.getLabel());
            oldRole.setPermission(role.getPermission());
            oldRole.setStatus(role.getStatus());
            oldRole.setUpdatedTime(new Date());
            return roleMapper.updateByPrimaryKeySelective(oldRole) > 0;
        }
        role.setCreatedTime(new Date());
        return roleMapper.insertSelective(role) > 0;
    }

    @Override
    public List<Role> findRoles(RoleSearch search) {
        return roleMapper.selectRoles(search.getLabel(), search.getStartDate(), search.getEndDate(), search.getOrder(), search.getSort());

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean bindRoleMenu(BindRoleMenuInfo bindRoleMenuInfo) {
        int num = 0;
        for (Long idMenu : bindRoleMenuInfo.getIdMenus()) {
            num += roleMapper.insertRoleMenu(bindRoleMenuInfo.getIdRole(), idMenu);
        }
        return num == bindRoleMenuInfo.getIdMenus().size();
    }

    @Override
    public Boolean updateStatus(Long idRole, Integer status) {
        return roleMapper.updateStatusByIdRole(idRole, status) > 0;
    }

    @Override
    public Set<Long> findRoleMenus(Long idRole) {
        return roleMapper.selectRoleMenus(idRole);
    }

    @Override
    public Boolean updateDelFlag(Long idRole, Integer delFlag) {
        return roleMapper.updateDelFlag(idRole, delFlag) > 0;
    }
}
