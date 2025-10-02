package com.rymcu.mortise.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.common.model.BatchUpdateInfo;
import com.rymcu.mortise.system.model.BindUserRoleInfo;
import com.rymcu.mortise.system.model.UserInfo;
import com.rymcu.mortise.system.model.UserSearch;
import com.rymcu.mortise.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 * 提供用户的 CRUD 操作和用户管理
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2024/8/10
 */
@Tag(name = "用户管理", description = "用户管理相关接口")
@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    @Resource
    private UserService userService;

    @Operation(summary = "获取用户列表", description = "分页查询用户信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping
    public GlobalResult<Page<UserInfo>> listUser(@Parameter(description = "用户查询条件") @Valid UserSearch search) {
        Page<UserInfo> page = new Page<>(search.getPageNum(), search.getPageSize());
        page = userService.findUsers(page, search);
        return GlobalResult.success(page);
    }

    @Operation(summary = "获取用户详情", description = "根据ID获取用户详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/{id}")
    public GlobalResult<UserInfo> getUserById(@Parameter(description = "用户ID", required = true) @PathVariable("id") Long idUser) {
        return GlobalResult.success(userService.findUserInfoById(idUser));
    }

    @Operation(summary = "创建用户", description = "新增用户数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping
    public GlobalResult<Boolean> createUser(@Parameter(description = "用户信息", required = true) @Valid @RequestBody UserInfo userInfo) {
        return GlobalResult.success(userService.saveUser(userInfo));
    }

    @Operation(summary = "更新用户", description = "修改用户数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PutMapping("/{id}")
    public GlobalResult<Boolean> updateUser(@Parameter(description = "用户ID", required = true) @PathVariable("id") Long idUser,
                                           @Parameter(description = "用户信息", required = true) @Valid @RequestBody UserInfo userInfo) {
        userInfo.setId(idUser);
        return GlobalResult.success(userService.saveUser(userInfo));
    }

    @Operation(summary = "更新用户状态", description = "启用/禁用用户")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PatchMapping("/{id}/status")
    public GlobalResult<Boolean> updateUserStatus(@Parameter(description = "用户ID", required = true) @PathVariable("id") Long idUser,
                                                  @Parameter(description = "用户状态信息", required = true) @Valid @RequestBody UserInfo userInfo) {
        return GlobalResult.success(userService.updateStatus(idUser, userInfo.getStatus()));
    }

    @Operation(summary = "重置用户密码", description = "重置用户密码并返回新密码")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "重置成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping("/{id}/reset-password")
    public GlobalResult<ObjectNode> resetUserPassword(@Parameter(description = "用户ID", required = true) @PathVariable("id") Long idUser) {
        String password = userService.resetPassword(idUser);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonObject = objectMapper.createObjectNode();
        jsonObject.put("password", password);
        return GlobalResult.success(jsonObject);
    }

    @Operation(summary = "绑定用户用户", description = "为用户分配用户")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "绑定成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping("/{id}/roles")
    public GlobalResult<Boolean> bindUserRoles(@Parameter(description = "用户ID", required = true) @PathVariable("id") Long idUser,
                                              @Parameter(description = "用户用户绑定信息", required = true) @Valid @RequestBody BindUserRoleInfo bindUserRoleInfo) {
        bindUserRoleInfo.setIdUser(idUser);
        return GlobalResult.success(userService.bindUserRole(bindUserRoleInfo));
    }

    @Operation(summary = "删除用户", description = "软删除用户数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/{id}")
    public GlobalResult<Boolean> deleteUser(@Parameter(description = "用户ID", required = true) @PathVariable("id") Long idUser) {
        return GlobalResult.success(userService.deleteUser(idUser));
    }

    @Operation(summary = "批量删除用户", description = "批量软删除用户数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/batch")
    public GlobalResult<Boolean> batchDeleteUsers(@Parameter(description = "批量更新信息", required = true) @Valid @RequestBody BatchUpdateInfo batchUpdateInfo) {
        return GlobalResult.success(userService.batchDeleteUsers(batchUpdateInfo.getIds()));
    }
}
