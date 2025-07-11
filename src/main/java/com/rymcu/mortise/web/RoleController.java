package com.rymcu.mortise.web;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.entity.Role;
import com.rymcu.mortise.enumerate.DelFlag;
import com.rymcu.mortise.model.BindRoleMenuInfo;
import com.rymcu.mortise.service.RoleService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * Created on 2024/8/10 17:27.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.web
 */
@RestController
@RequestMapping("/api/v1/admin/role")
@PreAuthorize("hasRole('admin')")
public class RoleController {
    @Resource
    private RoleService roleService;

    @GetMapping("/detail/{idRole}")
    public GlobalResult<Role> role(@PathVariable Long idRole) {
        return GlobalResult.success(roleService.findById(idRole));
    }

    @PostMapping("/post")
    public GlobalResult<Boolean> addRole(@RequestBody Role role) {
        return GlobalResult.success(roleService.saveRole(role));
    }

    @PutMapping("/post")
    public GlobalResult<Boolean> updateRole(@RequestBody Role role) {
        return GlobalResult.success(roleService.saveRole(role));
    }

    @PostMapping("/update-status")
    public GlobalResult<Boolean> updateRoleStatus(@RequestBody Role role) {
        return GlobalResult.success(roleService.updateStatus(role.getId(), role.getStatus()));
    }

    @GetMapping("/{idRole}/menus")
    public GlobalResult<Set<Long>> menus(@PathVariable Long idRole) {
        return GlobalResult.success(roleService.findRoleMenus(idRole));
    }

    @PostMapping("/bind-menu")
    public GlobalResult<Boolean> bindRoleMenu(@RequestBody BindRoleMenuInfo bindRoleMenuInfo) {
        return GlobalResult.success(roleService.bindRoleMenu(bindRoleMenuInfo));
    }

    @DeleteMapping("/update-del-flag")
    public GlobalResult<Boolean> updateDelFlag(Long idRole) {
        return GlobalResult.success(roleService.updateDelFlag(idRole, DelFlag.DELETED.ordinal()));
    }
}
