package com.rymcu.mortise.system.controller.facade.impl;

import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.controller.assembler.MenuAdminAssembler;
import com.rymcu.mortise.system.controller.assembler.RoleAdminAssembler;
import com.rymcu.mortise.system.controller.assembler.UserAdminAssembler;
import com.rymcu.mortise.system.controller.facade.RoleAdminFacade;
import com.rymcu.mortise.system.controller.request.RoleStatusRequest;
import com.rymcu.mortise.system.controller.request.RoleUpsertRequest;
import com.rymcu.mortise.system.controller.vo.MenuVO;
import com.rymcu.mortise.system.controller.vo.RoleVO;
import com.rymcu.mortise.system.controller.vo.UserVO;
import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.system.model.BindRoleMenuInfo;
import com.rymcu.mortise.system.model.BindRoleUserInfo;
import com.rymcu.mortise.system.model.RoleSearch;
import com.rymcu.mortise.system.query.RoleQueryService;
import com.rymcu.mortise.system.service.command.RoleCommandService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleAdminFacadeImpl implements RoleAdminFacade {

    private final RoleCommandService roleCommandService;
    private final RoleQueryService roleQueryService;

    public RoleAdminFacadeImpl(RoleCommandService roleCommandService, RoleQueryService roleQueryService) {
        this.roleCommandService = roleCommandService;
        this.roleQueryService = roleQueryService;
    }

    @Override
    public PageResult<RoleVO> listRoles(RoleSearch search) {
        PageResult<Role> rolePage = roleQueryService.findRoles(PageQuery.of(search.getPageNum(), search.getPageSize()), search);
        return rolePage.map(RoleAdminAssembler::toRoleVO);
    }

    @Override
    public RoleVO getRoleById(Long roleId) {
        return RoleAdminAssembler.toRoleVO(roleQueryService.findById(roleId));
    }

    @Override
    public Long createRole(RoleUpsertRequest request) {
        return roleCommandService.createRole(RoleAdminAssembler.toRole(request));
    }

    @Override
    public Boolean updateRole(Long roleId, RoleUpsertRequest request) {
        Role role = RoleAdminAssembler.toRole(request);
        role.setId(roleId);
        return roleCommandService.updateRole(role);
    }

    @Override
    public Boolean updateRoleStatus(Long roleId, RoleStatusRequest request) {
        return roleCommandService.updateStatus(roleId, request.getStatus());
    }

    @Override
    public List<UserVO> getRoleUsers(Long roleId) {
        return UserAdminAssembler.toUserVOs(roleQueryService.findUsersByIdRole(roleId));
    }

    @Override
    public Boolean bindRoleUsers(Long roleId, BindRoleUserInfo bindRoleUserInfo) {
        bindRoleUserInfo.setIdRole(roleId);
        return roleCommandService.bindRoleUser(bindRoleUserInfo);
    }

    @Override
    public List<MenuVO> getRoleMenus(Long roleId) {
        return MenuAdminAssembler.toMenuVOs(roleQueryService.findMenusByIdRole(roleId));
    }

    @Override
    public Boolean bindRoleMenus(Long roleId, BindRoleMenuInfo bindRoleMenuInfo) {
        bindRoleMenuInfo.setIdRole(roleId);
        return roleCommandService.bindRoleMenu(bindRoleMenuInfo);
    }

    @Override
    public Boolean deleteRole(Long roleId) {
        return roleCommandService.deleteRole(roleId);
    }

    @Override
    public Boolean batchDeleteRoles(BatchUpdateInfo batchUpdateInfo) {
        return roleCommandService.batchDeleteRoles(batchUpdateInfo.getIds());
    }
}
