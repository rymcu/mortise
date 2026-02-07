package com.rymcu.mortise.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rymcu.mortise.auth.service.TokenManager;
import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.common.model.Link;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.core.result.ResultCode;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.exception.AccountExistsException;
import com.rymcu.mortise.system.model.auth.*;
import com.rymcu.mortise.system.service.AuthService;
import com.rymcu.mortise.web.annotation.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;
import java.util.Objects;

/**
 * 认证授权控制器
 * 提供登录、注册、Token 刷新等认证相关接口
 *
 * @author ronger
 */
@Slf4j
@Tag(name = "认证管理", description = "认证授权相关接口")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    @Resource
    private TokenManager tokenManager;

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
        log.info("用户登录请求: {}", loginInfo.getAccount());
        TokenUser tokenUser = authService.login(loginInfo.getAccount(), loginInfo.getPassword());
        return GlobalResult.success(tokenUser);
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
        log.info("用户注册请求: {}", registerInfo.getEmail());
        Boolean result = authService.register(
                registerInfo.getEmail(),
                registerInfo.getNickname(),
                registerInfo.getPassword(),
                registerInfo.getCode()
        );
        return GlobalResult.success(result);
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
        log.info("Token 刷新请求");
        TokenUser tokenUser = authService.refreshToken(refreshTokenInfo.getRefreshToken());
        return GlobalResult.success(tokenUser);
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
            @AuthenticationPrincipal UserDetailInfo userDetails,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = userDetails.getUser();
        if (Objects.nonNull(user)) {
            // 从 Authorization 头中提取 Token
            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
            
            if (token != null) {
                // 使用带黑名单的注销方法
                tokenManager.revokeToken(user.getAccount(), token);
                log.info("用户登出成功（Token 已加入黑名单）: {}", user.getAccount());
            } else {
                // 回退到简单删除
                tokenManager.deleteToken(user.getAccount());
                log.info("用户登出成功: {}", user.getAccount());
            }
        }
        return GlobalResult.success();
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
    public GlobalResult<ObjectNode> getUserSession(@AuthenticationPrincipal UserDetailInfo userDetails) {
        log.info("获取用户会话信息: {}", userDetails.getUsername());
        AuthInfo authInfo = authService.userSession(userDetails.getUser());
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode object = objectMapper.createObjectNode();
        object.set("user", objectMapper.valueToTree(authInfo));
        return GlobalResult.success(object);
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
    public GlobalResult<List<Link>> menus(@AuthenticationPrincipal UserDetailInfo userDetails) {
        List<Link> menus = authService.userMenus(userDetails.getUser());
        return GlobalResult.success(menus);
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
        authService.requestPasswordReset(email);
        return GlobalResult.success(ResultCode.SUCCESS.getMessage());
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
        boolean flag = authService.forgetPassword(forgetPassword.getCode(), forgetPassword.getPassword());
        return GlobalResult.success(flag);
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
        authService.requestEmailVerify(email);
        return GlobalResult.success(ResultCode.SUCCESS.getMessage());
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
        TokenUser tokenUser = authService.getOauth2TokenUser(state);
        return GlobalResult.success(tokenUser);
    }

}
