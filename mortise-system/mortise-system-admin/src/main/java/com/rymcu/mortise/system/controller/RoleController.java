package com.rymcu.mortise.system.controller;

import com.rymcu.mortise.web.annotation.AdminController;
import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.system.controller.facade.RoleAdminFacade;
import com.rymcu.mortise.system.controller.request.RoleStatusRequest;
import com.rymcu.mortise.system.controller.request.RoleUpsertRequest;
import com.rymcu.mortise.system.controller.vo.MenuVO;
import com.rymcu.mortise.system.controller.vo.RoleVO;
import com.rymcu.mortise.system.controller.vo.UserVO;
import com.rymcu.mortise.system.model.BindRoleMenuInfo;
import com.rymcu.mortise.system.model.BindRoleUserInfo;
import com.rymcu.mortise.system.model.RoleSearch;
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
public class RoleController {
    @Resource
    private RoleAdminFacade roleAdminFacade;

    @Operation(summary = "获取角色列表", description = "分页查询角色信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('system:role:list')")
    @ApiLog("查询角色列表")
    public GlobalResult<PageResult<RoleVO>> listRole(@Parameter(description = "角色查询条件") @Valid RoleSearch search) {
        return GlobalResult.success(roleAdminFacade.listRoles(search));
    }

    @Operation(summary = "获取角色详情", description = "根据ID获取角色详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "角色不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:query')")
    @ApiLog("获取角色详情")
    public GlobalResult<RoleVO> getRoleById(@Parameter(description = "角色ID", required = true) @PathVariable("id") Long idRole) {
        return GlobalResult.success(roleAdminFacade.getRoleById(idRole));
    }

    @Operation(summary = "创建角色", description = "新增角色数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('system:role:add')")
    @ApiLog("创建角色")
    @OperationLog(module = "角色管理", operation = "创建角色", recordParams = true, recordResult = true)
    public GlobalResult<Long> createRole(@Parameter(description = "角色信息", required = true) @Valid @RequestBody RoleUpsertRequest request) {
        return GlobalResult.success(roleAdminFacade.createRole(request));
    }

    @Operation(summary = "更新角色", description = "修改角色数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "404", description = "角色不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:edit')")
    @ApiLog("更新角色")
    @OperationLog(module = "角色管理", operation = "更新角色", recordParams = true)
    public GlobalResult<Boolean> updateRole(@Parameter(description = "角色ID", required = true) @PathVariable("id") Long idRole,
                                           @Parameter(description = "角色信息", required = true) @Valid @RequestBody RoleUpsertRequest request) {
        return GlobalResult.success(roleAdminFacade.updateRole(idRole, request));
    }

    @Operation(summary = "更新角色状态", description = "启用/禁用角色")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "角色不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:role:edit')")
    @ApiLog("更新角色状态")
    @OperationLog(module = "角色管理", operation = "更新角色状态", recordParams = true)
    public GlobalResult<Boolean> updateRoleStatus(@Parameter(description = "角色ID", required = true) @PathVariable("id") Long idRole,
                                                  @Parameter(description = "角色状态信息", required = true) @Valid @RequestBody RoleStatusRequest request) {
        return GlobalResult.success(roleAdminFacade.updateRoleStatus(idRole, request));
    }

    @Operation(summary = "获取角色-用户", description = "获取角色关联的用户列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "角色不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/{id}/users")
    @PreAuthorize("hasAuthority('system:role:query')")
    @ApiLog("获取角色用户")
    public GlobalResult<List<UserVO>> getRoleUsers(@Parameter(description = "角色ID", required = true) @PathVariable("id") Long idRole) {
        return GlobalResult.success(roleAdminFacade.getRoleUsers(idRole));
    }

    @Operation(summary = "绑定角色-用户", description = "分配角色给用户")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "绑定成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PutMapping("/{id}/users")
    @PreAuthorize("hasAuthority('system:role:assign')")
    @ApiLog("绑定角色用户")
    @OperationLog(module = "角色管理", operation = "绑定角色用户", recordParams = true)
    public GlobalResult<Boolean> bindRoleUsers(@Parameter(description = "角色ID", required = true) @PathVariable("id") Long idRole,
                                               @Parameter(description = "角色用户绑定信息", required = true) @Valid @RequestBody BindRoleUserInfo bindRoleUserInfo) {
        return GlobalResult.success(roleAdminFacade.bindRoleUsers(idRole, bindRoleUserInfo));
    }

    @Operation(summary = "获取角色菜单权限", description = "获取角色关联的菜单列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "角色不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/{id}/menus")
    @PreAuthorize("hasAuthority('system:role:query')")
    @ApiLog("获取角色菜单")
    public GlobalResult<List<MenuVO>> getRoleMenus(@Parameter(description = "角色ID", required = true) @PathVariable("id") Long idRole) {
        return GlobalResult.success(roleAdminFacade.getRoleMenus(idRole));
    }

    @Operation(summary = "绑定角色菜单权限", description = "为角色分配菜单权限")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "绑定成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PutMapping("/{id}/menus")
    @PreAuthorize("hasAuthority('system:role:assign')")
    @ApiLog("绑定角色菜单")
    @OperationLog(module = "角色管理", operation = "绑定角色菜单", recordParams = true)
    public GlobalResult<Boolean> bindRoleMenus(@Parameter(description = "角色ID", required = true) @PathVariable("id") Long idRole,
                                              @Parameter(description = "角色菜单绑定信息", required = true) @Valid @RequestBody BindRoleMenuInfo bindRoleMenuInfo) {
        return GlobalResult.success(roleAdminFacade.bindRoleMenus(idRole, bindRoleMenuInfo));
    }

    @Operation(summary = "删除角色", description = "软删除角色数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "角色不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:delete')")
    @ApiLog("删除角色")
    @OperationLog(module = "角色管理", operation = "删除角色")
    public GlobalResult<Boolean> deleteRole(@Parameter(description = "角色ID", required = true) @PathVariable("id") Long idRole) {
        return GlobalResult.success(roleAdminFacade.deleteRole(idRole));
    }

    @Operation(summary = "批量删除角色", description = "批量软删除角色数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system:role:delete')")
    @ApiLog("批量删除角色")
    @OperationLog(module = "角色管理", operation = "批量删除角色", recordParams = true)
    public GlobalResult<Boolean> batchDeleteRoles(@Parameter(description = "批量更新信息", required = true) @Valid @RequestBody BatchUpdateInfo batchUpdateInfo) {
        return GlobalResult.success(roleAdminFacade.batchDeleteRoles(batchUpdateInfo));
    }
}
