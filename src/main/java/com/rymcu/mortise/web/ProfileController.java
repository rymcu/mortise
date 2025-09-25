package com.rymcu.mortise.web;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.entity.User;
import com.rymcu.mortise.model.UserProfileInfo;
import com.rymcu.mortise.service.UserService;
import com.rymcu.mortise.util.UserUtils;
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
 * 用户个人信息控制器
 * 提供用户个人信息管理功能
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2025/3/19
 */
@Tag(name = "个人信息管理", description = "用户个人信息管理相关接口")
@RestController
@RequestMapping("/api/v1/profile")
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    @Resource
    private UserService userService;

    @Operation(summary = "更新个人信息", description = "更新当前用户的个人资料信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    @PutMapping
    public GlobalResult<Boolean> updateUserProfile(@Parameter(description = "用户个人信息", required = true) @Valid @RequestBody UserProfileInfo userProfileInfo) {
        User user = UserUtils.getCurrentUserByToken();
        return GlobalResult.success(userService.updateUserProfileInfo(userProfileInfo, user));
    }

    @Operation(summary = "获取个人信息", description = "获取当前用户的个人资料信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    @GetMapping
    public GlobalResult<User> getUserProfile() {
        User user = UserUtils.getCurrentUserByToken();
        return GlobalResult.success(user);
    }
}
