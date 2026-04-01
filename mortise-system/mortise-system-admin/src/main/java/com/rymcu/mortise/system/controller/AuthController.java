package com.rymcu.mortise.system.controller;

import com.rymcu.mortise.auth.constant.JwtConstants;
import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.common.model.Link;
import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.system.constant.SystemAuthConstants;
import com.rymcu.mortise.system.controller.facade.AuthAdminFacade;
import com.rymcu.mortise.system.controller.vo.UserSessionVO;
import com.rymcu.mortise.system.exception.AccountExistsException;
import com.rymcu.mortise.system.model.ForgetPasswordInfo;
import com.rymcu.mortise.system.model.RefreshTokenInfo;
import com.rymcu.mortise.system.model.TokenUser;
import com.rymcu.mortise.system.model.UserProfileInfo;
import com.rymcu.mortise.system.model.auth.EmailUpdateCodeRequest;
import com.rymcu.mortise.system.model.auth.EmailUpdateConfirmInfo;
import com.rymcu.mortise.system.model.auth.LoginInfo;
import com.rymcu.mortise.system.model.auth.OAuth2ProviderInfo;
import com.rymcu.mortise.system.model.auth.RegisterInfo;
import com.rymcu.mortise.web.annotation.AdminController;
import com.rymcu.mortise.web.annotation.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;

/**
 * 认证授权控制器
 * 提供登录、注册、Token 刷新等认证相关接口
 *
 * @author ronger
 */
@Tag(name = "认证管理", description = "认证授权相关接口")
@AdminController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthAdminFacade authAdminFacade;

    /**
     * 获取已启用的 OAuth2 登录提供商列表（公开接口，登录页使用）
     * 仅返回 registrationId 和 clientName，不暴露任何敏感配置
     */
    @Operation(summary = "获取 OAuth2 提供商列表", description = "返回当前已启用的第三方登录提供商，供登录页动态渲染")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @GetMapping("/oauth2-providers")
    @ApiLog(value = "获取OAuth2提供商列表", recordRequestBody = false, recordResponseBody = false)
    public GlobalResult<List<OAuth2ProviderInfo>> listOAuth2Providers(
            @Parameter(description = "登录入口类型：admin=管理端，site=用户端", example = SystemAuthConstants.OAUTH2_APP_TYPE_ADMIN)
            @RequestParam(defaultValue = SystemAuthConstants.OAUTH2_APP_TYPE_ADMIN) String appType) {
        return authAdminFacade.listOAuth2Providers(appType);
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "使用账号密码登录系统")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "401", description = "认证失败"),
            @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @RateLimit(limitForPeriod = 5, refreshPeriodSeconds = 300, message = "登录请求过于频繁，请 5 分钟后再试")
    @PostMapping("/login")
    @ApiLog(value = "用户登录", recordRequestBody = false, recordResponseBody = false)
    @OperationLog(module = "认证管理", operation = "用户登录", recordParams = false)
    public GlobalResult<TokenUser> login(
            @Parameter(description = "登录请求", required = true)
            @Valid @RequestBody LoginInfo loginInfo) {
        return authAdminFacade.login(loginInfo);
    }
    
    /**
     * 用户注册
     */
    @Operation(summary = "用户注册", description = "新用户注册")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注册成功"),
            @ApiResponse(responseCode = "400", description = "参数错误或账号已存在")
    })
    @RateLimit(limitForPeriod = 3, refreshPeriodSeconds = 600, message = "注册请求过于频繁，请 10 分钟后再试")
    @PostMapping("/register")
    @ApiLog(value = "用户注册", recordRequestBody = false)
    @OperationLog(module = "认证管理", operation = "用户注册", recordParams = false, recordResult = true)
    public GlobalResult<Boolean> register(
            @Parameter(description = "注册请求", required = true)
            @Valid @RequestBody RegisterInfo registerInfo) throws AccountExistsException {
        return authAdminFacade.register(registerInfo);
    }

    /**
     * 刷新 Token
     */
    @Operation(summary = "刷新 Token", description = "使用 refresh token 获取新的 access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "刷新成功"),
            @ApiResponse(responseCode = "401", description = "Token 无效或已过期")
    })
    @PostMapping("/refresh-token")
    @RateLimit(limitForPeriod = 20, refreshPeriodSeconds = 300, message = "刷新 Token 请求过于频繁，请 5 分钟后再试")
    @ApiLog(value = "刷新Token", recordRequestBody = false, recordResponseBody = false)
    @OperationLog(module = "认证管理", operation = "刷新Token", recordParams = false)
    public GlobalResult<TokenUser> refreshToken(
            @Parameter(description = "刷新 Token 请求", required = true)
            @Valid @RequestBody RefreshTokenInfo refreshTokenInfo) {
        return authAdminFacade.refreshToken(refreshTokenInfo);
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "退出登录，注销 Token 并加入黑名单")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登出成功")
    })
    @PostMapping("/logout")
    @ApiLog(value = "用户登出", recordRequestBody = false, recordResponseBody = false)
    @OperationLog(module = "认证管理", operation = "用户登出", recordParams = false)
    public GlobalResult<?> logout(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = JwtConstants.AUTHORIZATION, required = false) String authHeader) {
        return authAdminFacade.logout(currentUser, authHeader);
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    @GetMapping("/me")
    @ApiLog(value = "获取当前用户信息", recordRequestBody = false, recordResponseBody = false)
    public GlobalResult<UserSessionVO> getUserSession(@AuthenticationPrincipal CurrentUser currentUser) {
        return authAdminFacade.getUserSession(currentUser);
    }

    /**
     * 获取当前用户菜单
     */
    @Operation(summary = "获取当前用户菜单", description = "获取当前登录用户的菜单权限")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    @GetMapping("/menus")
    @ApiLog(value = "获取当前用户菜单", recordRequestBody = false)
    public GlobalResult<List<Link>> menus(@AuthenticationPrincipal CurrentUser currentUser) {
        return authAdminFacade.menus(currentUser);
    }

    @Operation(summary = "忘记密码", description = "发送密码重置邮件")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "邮件发送成功"),
            @ApiResponse(responseCode = "404", description = "账号不存在")
    })
    @GetMapping("/password/request")
    @ApiLog(value = "忘记密码", recordRequestBody = false, recordResponseBody = false)
    @OperationLog(module = "认证管理", operation = "忘记密码", recordParams = true, recordResult = false)
    public GlobalResult<String> requestPasswordReset(@RequestParam("email") String email) throws MessagingException, ServiceException, AccountNotFoundException {
        return authAdminFacade.requestPasswordReset(email);
    }

    @Operation(summary = "重置密码", description = "使用验证码重置密码")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "密码重置成功"),
            @ApiResponse(responseCode = "400", description = "验证码无效或已过期")
    })
    @PatchMapping("/password/reset")
    @ApiLog(value = "重置密码", recordRequestBody = false, recordResponseBody = false)
    @OperationLog(module = "认证管理", operation = "重置密码", recordParams = false, recordResult = true)
    public GlobalResult<Boolean> resetPassword(@RequestBody ForgetPasswordInfo forgetPassword) throws ServiceException {
        return authAdminFacade.resetPassword(forgetPassword);
    }

    @Operation(summary = "发送邮箱验证", description = "发送邮箱验证码")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "发送成功"),
            @ApiResponse(responseCode = "400", description = "邮箱已存在")
    })
    @GetMapping("/email/request")
    @ApiLog(value = "发送邮箱验证", recordRequestBody = false, recordResponseBody = false)
    @OperationLog(module = "认证管理", operation = "发送邮箱验证码", recordParams = true, recordResult = false)
    public GlobalResult<String> requestEmailVerify(@RequestParam("email") String email) throws MessagingException, AccountExistsException {
        return authAdminFacade.requestEmailVerify(email);
    }

    /**
     * 兑换 Token
     */
    @Operation(summary = "兑换 Token", description = "使用 state 兑换 Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "兑换成功"),
            @ApiResponse(responseCode = "401", description = "认证失败"),
            @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @RateLimit(limitForPeriod = 5, refreshPeriodSeconds = 300, message = "兑换 Token 请求过于频繁,请 5 分钟后再试")
    @GetMapping("/callback")
    @ApiLog(value = "OAuth2兑换Token", recordRequestBody = false, recordResponseBody = false)
    @OperationLog(module = "认证管理", operation = "OAuth2兑换Token", recordParams = false)
    public GlobalResult<TokenUser> oauth2Login(
            @Parameter(description = "兑换 Token 请求", required = true)
            @Valid @RequestParam("state") String state) {
        return authAdminFacade.oauth2Login(state);
    }

    /**
     * 获取当前用户资料
     */
    @Operation(summary = "获取当前用户资料", description = "获取当前登录用户的个人资料")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    @GetMapping("/profile")
    @ApiLog(value = "获取当前用户资料", recordRequestBody = false, recordResponseBody = false)
    public GlobalResult<UserProfileInfo> getProfile(@AuthenticationPrincipal CurrentUser currentUser) {
        return authAdminFacade.getProfile(currentUser);
    }

    /**
     * 更新当前用户资料
     */
    @Operation(summary = "更新当前用户资料", description = "更新当前登录用户的个人资料")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @PutMapping("/profile")
    @ApiLog(value = "更新当前用户资料", recordRequestBody = false, recordResponseBody = false)
    @OperationLog(module = "认证管理", operation = "更新用户资料", recordParams = false, recordResult = true)
    public GlobalResult<Boolean> updateProfile(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody UserProfileInfo userProfileInfo) {
        return authAdminFacade.updateProfile(currentUser, userProfileInfo);
    }

    /**
     * 发送邮箱更换验证码
     */
    @Operation(summary = "发送邮箱更换验证码", description = "向新邮箱发送 6 位验证码（有效期 10 分钟），新邮箱须未被其他账号注册")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "发送成功"),
            @ApiResponse(responseCode = "400", description = "邮箱已被注册"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    @RateLimit(limitForPeriod = 3, refreshPeriodSeconds = 300, message = "发送过于频繁，请 5 分钟后再试")
    @PostMapping("/email/send-code")
    @ApiLog(value = "发送邮箱更换验证码", recordRequestBody = false, recordResponseBody = false)
    @OperationLog(module = "认证管理", operation = "发送邮箱更换验证码", recordParams = false, recordResult = false)
    public GlobalResult<String> sendEmailUpdateCode(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody EmailUpdateCodeRequest request) {
        return authAdminFacade.sendEmailUpdateCode(currentUser, request);
    }

    /**
     * 确认邮箱更换
     */
    @Operation(summary = "确认邮箱更换", description = "提交验证码完成邮箱更换，验证码匹配后立即生效")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更换成功"),
            @ApiResponse(responseCode = "400", description = "验证码错误或已失效"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    @PutMapping("/email/confirm")
    @ApiLog(value = "确认邮箱更换", recordRequestBody = false, recordResponseBody = false)
    @OperationLog(module = "认证管理", operation = "确认邮箱更换", recordParams = false, recordResult = true)
    public GlobalResult<Boolean> confirmEmailUpdate(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody EmailUpdateConfirmInfo confirmInfo) {
        return authAdminFacade.confirmEmailUpdate(currentUser, confirmInfo);
    }

}

