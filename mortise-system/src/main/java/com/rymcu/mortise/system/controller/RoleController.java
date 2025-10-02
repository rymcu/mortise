package com.rymcu.mortise.system.controller;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.system.model.BindRoleMenuInfo;
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

import java.util.Set;

/**
 * 角色管理控制器
 * 提供角色的 CRUD 操作和权限管理
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2024/8/10
 */
@Tag(name = "角色管理", description = "角色管理相关接口")
@RestController
@RequestMapping("/api/v1/admin/roles")
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
    public GlobalResult<Role> getRoleById(@Parameter(description = "角色ID", required = true) @PathVariable("id") Long idRole) {
        return GlobalResult.success(roleService.findById(idRole));
    }

    @Operation(summary = "创建角色", description = "新增角色数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping
    public GlobalResult<Boolean> createRole(@Parameter(description = "角色信息", required = true) @Valid @RequestBody Role role) {
        return GlobalResult.success(roleService.saveRole(role));
    }

    @Operation(summary = "更新角色", description = "修改角色数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "404", description = "角色不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PutMapping("/{id}")
    public GlobalResult<Boolean> updateRole(@Parameter(description = "角色ID", required = true) @PathVariable("id") Long idRole,
                                           @Parameter(description = "角色信息", required = true) @Valid @RequestBody Role role) {
        role.setId(idRole);
        return GlobalResult.success(roleService.saveRole(role));
    }

    @Operation(summary = "更新角色状态", description = "启用/禁用角色")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "角色不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PatchMapping("/{id}/status")
    public GlobalResult<Boolean> updateRoleStatus(@Parameter(description = "角色ID", required = true) @PathVariable("id") Long idRole,
                                                  @Parameter(description = "角色状态信息", required = true) @Valid @RequestBody Role role) {
        return GlobalResult.success(roleService.updateStatus(idRole, role.getStatus()));
    }

    @Operation(summary = "获取角色菜单权限", description = "获取角色关联的菜单ID列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "角色不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/{id}/menus")
    public GlobalResult<Set<Long>> getRoleMenus(@Parameter(description = "角色ID", required = true) @PathVariable("id") Long idRole) {
        return GlobalResult.success(roleService.findRoleMenus(idRole));
    }

    @Operation(summary = "绑定角色菜单权限", description = "为角色分配菜单权限")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "绑定成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping("/{id}/menus")
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
    public GlobalResult<Boolean> batchDeleteRoles(@Parameter(description = "批量更新信息", required = true) @Valid @RequestBody BatchUpdateInfo batchUpdateInfo) {
        return GlobalResult.success(roleService.batchDeleteRoles(batchUpdateInfo.getIds()));
    }
}
