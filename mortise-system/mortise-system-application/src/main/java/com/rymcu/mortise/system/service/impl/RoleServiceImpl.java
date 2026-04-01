package com.rymcu.mortise.system.service.impl;

import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.entity.Menu;
import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.model.BindRoleMenuInfo;
import com.rymcu.mortise.system.model.BindRoleUserInfo;
import com.rymcu.mortise.system.model.RoleSearch;
import com.rymcu.mortise.system.query.RoleQueryService;
import com.rymcu.mortise.system.repository.RoleMenuRepository;
import com.rymcu.mortise.system.repository.RoleRepository;
import com.rymcu.mortise.system.repository.UserRoleRepository;
import com.rymcu.mortise.system.service.RoleService;
import com.rymcu.mortise.system.service.SystemCacheService;
import com.rymcu.mortise.system.service.command.RoleCommandService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Created on 2024/4/13 22:06.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service.impl
 */
@Service
public class RoleServiceImpl implements RoleService, RoleCommandService {

    @Resource
    private RoleRepository roleRepository;

    @Resource
    private RoleMenuRepository roleMenuRepository;

    @Resource
    private UserRoleRepository userRoleRepository;

    @Resource
    private SystemCacheService systemCacheService;
    @Resource
    private RoleQueryService roleQueryService;

    @Override
    public List<Role> findRolesByIdUser(Long idUser) {
        return roleQueryService.findRolesByIdUser(idUser);
    }

    @Override
    public PageResult<Role> findRoles(PageQuery pageQuery, RoleSearch search) {
        return roleQueryService.findRoles(pageQuery, search);
    }

    @Override
    public Role findById(Long idRole) {
        return roleQueryService.findById(idRole);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean bindRoleMenu(BindRoleMenuInfo bindRoleMenuInfo) {
        return roleMenuRepository.replaceMenus(
                bindRoleMenuInfo.getIdRole(),
                bindRoleMenuInfo.getIdMenus() == null ? null : new ArrayList<>(bindRoleMenuInfo.getIdMenus())
        );
    }

    @Override
    public Boolean updateStatus(Long idRole, Integer status) {
        return roleRepository.updateStatus(idRole, status);
    }

    @Override
    public List<Menu> findMenusByIdRole(Long idRole) {
        return roleQueryService.findMenusByIdRole(idRole);
    }

    @Override
    public Boolean deleteRole(Long idRole) {
        boolean result = roleRepository.deleteById(idRole);
        if (result) {
            systemCacheService.cacheRoleCount(count());
        }
        return result;
    }

    @Override
    public Boolean batchDeleteRoles(List<Long> idRoleList) {
        if (idRoleList == null || idRoleList.isEmpty()) {
            return false;
        }
        boolean result = roleRepository.deleteByIds(idRoleList);
        if (result) {
            systemCacheService.cacheRoleCount(count());
        }
        return result;
    }

    @Override
    public List<Role> findDefaultRole() {
        return roleQueryService.findDefaultRole();
    }

    @Override
    public Boolean bindRoleUser(BindRoleUserInfo bindRoleUserInfo) {
        return userRoleRepository.replaceUsers(
                bindRoleUserInfo.getIdRole(),
                bindRoleUserInfo.getIdUsers() == null ? null : new ArrayList<>(bindRoleUserInfo.getIdUsers())
        );
    }

    @Override
    public List<User> findUsersByIdRole(Long idRole) {
        return roleQueryService.findUsersByIdRole(idRole);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRole(Role role) {
        roleRepository.save(role);
        systemCacheService.cacheRoleCount(count());
        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateRole(Role role) {
        if (Objects.isNull(role) || Objects.isNull(role.getId())) {
            throw new ServiceException("数据不存在");
        }
        Role oldRole = roleRepository.findById(role.getId());
        if (Objects.isNull(oldRole)) {
            throw new ServiceException("数据不存在");
        }
        oldRole.setLabel(role.getLabel());
        oldRole.setPermission(role.getPermission());
        oldRole.setStatus(role.getStatus());
        if (role.getIsDefault() != null) {
            oldRole.setIsDefault(role.getIsDefault());
        }
        oldRole.setUpdatedTime(LocalDateTime.now());
        return roleRepository.update(oldRole);
    }

    @Override
    public long count() {
        return roleQueryService.count();
    }

    @Override
    public long countEnabled() {
        return roleQueryService.countEnabled();
    }
}
