package com.rymcu.mortise.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rymcu.mortise.entity.Role;
import com.rymcu.mortise.mapper.RoleMapper;
import com.rymcu.mortise.model.BindRoleMenuInfo;
import com.rymcu.mortise.model.RoleSearch;
import com.rymcu.mortise.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public List<Role> findRolesByIdUser(Long idUser) {
        return baseMapper.selectRolesByIdUser(idUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRole(Role role) {
        Role oldRole = baseMapper.selectById(role.getIdRole());
        if (Objects.nonNull(oldRole)) {
            oldRole.setLabel(role.getLabel());
            oldRole.setPermission(role.getPermission());
            oldRole.setStatus(role.getStatus());
            oldRole.setUpdatedTime(LocalDateTime.now());
        } else {
            role.setCreatedTime(LocalDateTime.now());
        }
        return baseMapper.insertOrUpdate(role);
    }

    @Override
    public List<Role> findRoles(Page<Role> page, RoleSearch search) {
        return baseMapper.selectRoles(page, search.getLabel(), search.getStartDate(), search.getEndDate(), search.getOrder(), search.getSort());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean bindRoleMenu(BindRoleMenuInfo bindRoleMenuInfo) {
        int num = 0;
        for (Long idMenu : bindRoleMenuInfo.getIdMenus()) {
            num += baseMapper.insertRoleMenu(bindRoleMenuInfo.getIdRole(), idMenu);
        }
        return num == bindRoleMenuInfo.getIdMenus().size();
    }

    @Override
    public Boolean updateStatus(Long idRole, Integer status) {
        return baseMapper.updateStatusByIdRole(idRole, status) > 0;
    }

    @Override
    public Set<Long> findRoleMenus(Long idRole) {
        return baseMapper.selectRoleMenus(idRole);
    }

    @Override
    public Boolean updateDelFlag(Long idRole, Integer delFlag) {
        return baseMapper.updateDelFlag(idRole, delFlag) > 0;
    }

    @Override
    public Role findById(Long idRole) {
        return baseMapper.selectById(idRole);
    }
}
