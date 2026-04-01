package com.rymcu.mortise.system.controller.assembler;

import com.rymcu.mortise.system.controller.request.RoleUpsertRequest;
import com.rymcu.mortise.system.controller.vo.RoleVO;
import com.rymcu.mortise.system.entity.Role;

import java.util.List;

/**
 * 管理端角色视图装配器。
 */
public final class RoleAdminAssembler {

    private RoleAdminAssembler() {
    }

    public static List<RoleVO> toRoleVOs(List<Role> roles) {
        return roles.stream()
                .map(RoleAdminAssembler::toRoleVO)
                .toList();
    }

    public static RoleVO toRoleVO(Role role) {
        RoleVO roleVO = new RoleVO();
        roleVO.setId(role.getId());
        roleVO.setLabel(role.getLabel());
        roleVO.setPermission(role.getPermission());
        roleVO.setStatus(role.getStatus());
        roleVO.setIsDefault(role.getIsDefault());
        roleVO.setDelFlag(role.getDelFlag());
        roleVO.setCreatedTime(role.getCreatedTime());
        roleVO.setUpdatedTime(role.getUpdatedTime());
        return roleVO;
    }

    public static Role toRole(RoleUpsertRequest request) {
        Role role = new Role();
        role.setLabel(request.getLabel());
        role.setPermission(request.getPermission());
        role.setStatus(request.getStatus());
        role.setIsDefault(request.getIsDefault());
        return role;
    }
}
