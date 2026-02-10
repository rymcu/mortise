package com.rymcu.mortise.system.controller;

import com.rymcu.mortise.web.annotation.AdminController;
import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.system.entity.Menu;
import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.model.BindRoleMenuInfo;
import com.rymcu.mortise.system.model.BindRoleUserInfo;
import com.rymcu.mortise.system.model.RoleSearch;
import com.rymcu.mortise.system.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 * 提供角色的 CRUD 操作和权限管理
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2024/8/10
 */
@Tag(name = "角色管理", description = "角色管理相关接口")
@AdminController
@RequestMapping("/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {
    @Resource
    private RoleService roleService;

    @Operation(summary = "获取角色列表", description = "分页查询角色信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping
        @ApiLog("查询角色列表")
    public GlobalResult<Page<Role>> listRole(@Parameter(description = "角色查询条件") @Valid RoleSearch search) {
        Page<Role> page = new Page<>(search.getPageNum(), search.getPageSize());
        return GlobalResult.success(roleService.findRoles(page, search));
    }

    @Operation(summary = "获取角色详情", description = "根据ID获取角色详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "角色不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/{id}")
        @ApiLog("获取角色详情")
    public GlobalResult<Role> getRoleById(@Parameter(description = "角色ID", required = true) @PathVariable("id") Long idRole) {
        return GlobalResult.success(roleService.getById(idRole));
    }

    @Operation(summary = "创建角色", description = "新增角色数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping
        @ApiLog("创建角色")
        @OperationLog(module = "角色管理", operation = "创建角色", recordParams = true, recordResult = true)
    public GlobalResult<Long> createRole(@Parameter(description = "角色信息", required = true) @Valid @RequestBody Role role) {
        return GlobalResult.success(roleService.createRole(role));
    }

    @Operation(summary = "更新角色", description = "修改角色数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "404", description = "角色不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PutMapping("/{id}")
        @ApiLog("更新角色")
        @OperationLog(module = "角色管理", operation = "更新角色", recordParams = true)
    public GlobalResult<Boolean> updateRole(@Parameter(description = "角色ID", required = true) @PathVariable("id") Long idRole,
                                           @Parameter(description = "角色信息", required = true) @Valid @RequestBody Role role) {
        role.setId(idRole);
        return GlobalResult.success(roleService.updateRole(role));
    }

    @Operation(summary = "更新角色状态", description = "启用/禁用角色")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "角色不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PatchMapping("/{id}/status")
        @ApiLog("更新角色状态")
        @OperationLog(module = "角色管理", operation = "更新角色状态", recordParams = true)
    public GlobalResult<Boolean> updateRoleStatus(@Parameter(description = "角色ID", required = true) @PathVariable("id") Long idRole,
                                                  @Parameter(description = "角色状态信息", required = true) @Valid @RequestBody Role role) {
        return GlobalResult.success(roleService.updateStatus(idRole, role.getStatus()));
    }

    @Operation(summary = "获取角色-用户", description = "获取角色关联的用户列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "角色不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/{id}/users")
        @ApiLog("获取角色用户")
    public GlobalResult<List<User>> getRoleUsers(@Parameter(description = "角色ID", required = true) @PathVariable("id") Long idRole) {
        return GlobalResult.success(roleService.findUsersByIdRole(idRole));
    }

    @Operation(summary = "绑定角色-用户", description = "分配角色给用户")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "绑定成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PutMapping("/{id}/users")
        @ApiLog("绑定角色用户")
        @OperationLog(module = "角色管理", operation = "绑定角色用户", recordParams = true)
    public GlobalResult<Boolean> bindRoleUsers(@Parameter(description = "角色ID", required = true) @PathVariable("id") Long idRole,
                                               @Parameter(description = "角色用户绑定信息", required = true) @Valid @RequestBody BindRoleUserInfo bindRoleUserInfo) {
        bindRoleUserInfo.setIdRole(idRole);
        return GlobalResult.success(roleService.bindRoleUser(bindRoleUserInfo));
    }

    @Operation(summary = "获取角色菜单权限", description = "获取角色关联的菜单列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "角色不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/{id}/menus")
        @ApiLog("获取角色菜单")
    public GlobalResult<List<Menu>> getRoleMenus(@Parameter(description = "角色ID", required = true) @PathVariable("id") Long idRole) {
        return GlobalResult.success(roleService.findMenusByIdRole(idRole));
    }

    @Operation(summary = "绑定角色菜单权限", description = "为角色分配菜单权限")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "绑定成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PutMapping("/{id}/menus")
        @ApiLog("绑定角色菜单")
        @OperationLog(module = "角色管理", operation = "绑定角色菜单", recordParams = true)
    public GlobalResult<Boolean> bindRoleMenus(@Parameter(description = "角色ID", required = true) @PathVariable("id") Long idRole,
                                              @Parameter(description = "角色菜单绑定信息", required = true) @Valid @RequestBody BindRoleMenuInfo bindRoleMenuInfo) {
        bindRoleMenuInfo.setIdRole(idRole);
        return GlobalResult.success(roleService.bindRoleMenu(bindRoleMenuInfo));
    }

    @Operation(summary = "删除角色", description = "软删除角色数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "角色不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/{id}")
        @ApiLog("删除角色")
        @OperationLog(module = "角色管理", operation = "删除角色")
    public GlobalResult<Boolean> deleteRole(@Parameter(description = "角色ID", required = true) @PathVariable("id") Long idRole) {
        return GlobalResult.success(roleService.deleteRole(idRole));
    }

    @Operation(summary = "批量删除角色", description = "批量软删除角色数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/batch")
        @ApiLog("批量删除角色")
        @OperationLog(module = "角色管理", operation = "批量删除角色", recordParams = true)
    public GlobalResult<Boolean> batchDeleteRoles(@Parameter(description = "批量更新信息", required = true) @Valid @RequestBody BatchUpdateInfo batchUpdateInfo) {
        return GlobalResult.success(roleService.batchDeleteRoles(batchUpdateInfo.getIds()));
    }
}

