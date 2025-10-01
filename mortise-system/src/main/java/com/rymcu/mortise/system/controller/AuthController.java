package com.rymcu.mortise.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rymcu.mortise.auth.service.TokenManager;
import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.common.model.Link;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.core.result.ResultCode;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.exception.AccountExistsException;
import com.rymcu.mortise.system.model.auth.UserDetailInfo;
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
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
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
    @Operation(summary = "用户登出", description = "退出登录")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登出成功")
    })
    @PostMapping("/logout")
    public GlobalResult<?> logout(@AuthenticationPrincipal UserDetailInfo userDetails) {
        User user = userDetails.getUser();
        if (Objects.nonNull(user)) {
            tokenManager.deleteToken(user.getAccount());
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
    public GlobalResult<String> requestEmailVerify(@RequestParam("email") String email) throws MessagingException, AccountExistsException {
        authService.requestEmailVerify(email);
        return GlobalResult.success(ResultCode.SUCCESS.getMessage());
    }

    /**
     * OAuth2 登录回调
     */
    @Operation(summary = "OAuth2登录回调", description = "OAuth2/OIDC 登录成功后的回调处理")
    @GetMapping("/oauth2/callback")
    public GlobalResult<TokenUser> oauth2Callback(
            @AuthenticationPrincipal OidcUser oidcUser,
            @RequestParam String registrationId) {
        log.info("OAuth2 登录回调: {}", registrationId);
        TokenUser tokenUser = authService.oauth2Login(oidcUser, registrationId);
        return GlobalResult.success(tokenUser);
    }

}
