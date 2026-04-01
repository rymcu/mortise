package com.rymcu.mortise.system.controller;

import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.system.controller.facade.UserAdminFacade;
import com.rymcu.mortise.system.controller.request.UserStatusRequest;
import com.rymcu.mortise.system.controller.request.UserUpsertRequest;
import com.rymcu.mortise.system.controller.vo.PasswordResetVO;
import com.rymcu.mortise.system.controller.vo.RoleVO;
import com.rymcu.mortise.system.controller.vo.UserVO;
import com.rymcu.mortise.system.model.BindUserRoleInfo;
import com.rymcu.mortise.system.model.UserSearch;
import com.rymcu.mortise.web.annotation.AdminController;
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
 * 用户管理控制器
 * 提供用户的 CRUD 操作和用户管理
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2024/8/10
 */
@Tag(name = "用户管理", description = "用户管理相关接口")
@AdminController
@RequestMapping("/users")
public class UserController {

    @Resource
    private UserAdminFacade userAdminFacade;

    @Operation(summary = "获取用户列表", description = "分页查询用户信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('system:user:list')")
    @ApiLog(recordParams = false, recordResponseBody = false, value = "查询用户列表")
    public GlobalResult<PageResult<UserVO>> listUser(@Parameter(description = "用户查询条件") @Valid UserSearch search) {
        return GlobalResult.success(userAdminFacade.listUsers(search));
    }

    @Operation(summary = "获取用户详情", description = "根据ID获取用户详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:query')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "获取用户详情")
    public GlobalResult<UserVO> getUserById(@Parameter(description = "用户ID", required = true) @PathVariable("id") Long idUser) {
        return GlobalResult.success(userAdminFacade.getUserById(idUser));
    }

    @Operation(summary = "创建用户", description = "新增用户数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "创建用户")
    @OperationLog(module = "用户管理", operation = "创建用户", recordParams = false, recordResult = true)
    public GlobalResult<Long> createUser(@Parameter(description = "用户信息", required = true) @Valid @RequestBody UserUpsertRequest request) {
        return GlobalResult.success(userAdminFacade.createUser(request));
    }

    @Operation(summary = "更新用户", description = "修改用户数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "更新用户")
    @OperationLog(module = "用户管理", operation = "更新用户", recordParams = false)
    public GlobalResult<Boolean> updateUser(@Parameter(description = "用户ID", required = true) @PathVariable("id") Long idUser,
                                           @Parameter(description = "用户信息", required = true) @Valid @RequestBody UserUpsertRequest request) {
        return GlobalResult.success(userAdminFacade.updateUser(idUser, request));
    }

    @Operation(summary = "更新用户状态", description = "启用/禁用用户")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @ApiLog(recordParams = true, recordRequestBody = false, recordResponseBody = false, value = "更新用户状态")
    @OperationLog(module = "用户管理", operation = "更新用户状态", recordParams = true)
    public GlobalResult<Boolean> updateUserStatus(@Parameter(description = "用户ID", required = true) @PathVariable("id") Long idUser,
                                                  @Parameter(description = "用户状态信息", required = true) @Valid @RequestBody UserStatusRequest request) {
        return GlobalResult.success(userAdminFacade.updateUserStatus(idUser, request));
    }

    @Operation(summary = "重置用户密码", description = "重置用户密码并返回新密码")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "重置成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('system:user:reset-password')")
    @ApiLog(value = "重置用户密码", recordParams = true, recordRequestBody = false, recordResponseBody = false)
    @OperationLog(module = "用户管理", operation = "重置用户密码", recordParams = true, recordResult = false)
    public GlobalResult<PasswordResetVO> resetUserPassword(@Parameter(description = "用户ID", required = true) @PathVariable("id") Long idUser) {
        return GlobalResult.success(userAdminFacade.resetUserPassword(idUser));
    }

    @Operation(summary = "绑定用户用户", description = "为用户分配用户")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "绑定成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('system:user:assign-role')")
    @ApiLog(recordRequestBody = false, recordResponseBody = false, value = "绑定用户角色")
    @OperationLog(module = "用户管理", operation = "绑定用户角色", recordParams = true)
    public GlobalResult<Boolean> bindUserRoles(@Parameter(description = "用户ID", required = true) @PathVariable("id") Long idUser,
                                              @Parameter(description = "用户用户绑定信息", required = true) @Valid @RequestBody BindUserRoleInfo bindUserRoleInfo) {
        return GlobalResult.success(userAdminFacade.bindUserRoles(idUser, bindUserRoleInfo));
    }

    @Operation(summary = "删除用户", description = "软删除用户数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "删除用户")
    @OperationLog(module = "用户管理", operation = "删除用户", recordParams = true, recordResult = true)
    public GlobalResult<Boolean> deleteUser(@Parameter(description = "用户ID", required = true) @PathVariable("id") Long idUser) {
        return GlobalResult.success(userAdminFacade.deleteUser(idUser));
    }

    @Operation(summary = "批量删除用户", description = "批量软删除用户数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system:user:delete')")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "批量删除用户")
    @OperationLog(module = "用户管理", operation = "批量删除用户", recordParams = false, recordResult = true)
    public GlobalResult<Boolean> batchDeleteUsers(@Parameter(description = "批量更新信息", required = true) @Valid @RequestBody BatchUpdateInfo batchUpdateInfo) {
        return GlobalResult.success(userAdminFacade.batchDeleteUsers(batchUpdateInfo));
    }

    @Operation(summary = "获取用户-角色", description = "获取用户关联的角色列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "角色不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('system:user:query')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "获取用户角色")
    public GlobalResult<List<RoleVO>> getRoleUsers(@Parameter(description = "用户ID", required = true) @PathVariable("id") Long idUser) {
        return GlobalResult.success(userAdminFacade.getUserRoles(idUser));
    }

    @Operation(summary = "绑定用户-角色", description = "给用户分配角色")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "绑定成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('system:user:assign-role')")
    @ApiLog(recordRequestBody = false, recordResponseBody = false, value = "更新用户角色")
    @OperationLog(module = "用户管理", operation = "更新用户角色", recordParams = true)
    public GlobalResult<Boolean> bindRoleUsers(@Parameter(description = "用户ID", required = true) @PathVariable("id") Long idUser,
                                               @Parameter(description = "用户角色绑定信息", required = true) @Valid @RequestBody BindUserRoleInfo bindUserRoleInfo) {
        return GlobalResult.success(userAdminFacade.bindRoleUsers(idUser, bindUserRoleInfo));
    }
}

