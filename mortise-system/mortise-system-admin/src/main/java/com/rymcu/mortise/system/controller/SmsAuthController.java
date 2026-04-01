package com.rymcu.mortise.system.controller;

import com.rymcu.mortise.web.annotation.AdminController;
import com.rymcu.mortise.auth.model.SendSmsCodeRequest;
import com.rymcu.mortise.auth.model.SmsLoginRequest;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.system.controller.facade.SmsAuthAdminFacade;
import com.rymcu.mortise.system.model.TokenUser;
import com.rymcu.mortise.web.annotation.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 短信验证码认证控制器
 * <p>
 * 提供短信验证码发送和验证码登录接口
 * </p>
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2025/11/12
 */
@Tag(name = "短信验证码认证", description = "短信验证码登录相关接口")
@AdminController
@RequestMapping("/auth/sms")
public class SmsAuthController {

    @Resource
    private SmsAuthAdminFacade smsAuthAdminFacade;

    /**
     * 发送短信验证码
     */
    @Operation(summary = "发送短信验证码", description = "向指定手机号发送登录验证码")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "发送成功"),
            @ApiResponse(responseCode = "400", description = "手机号格式错误或发送过于频繁"),
            @ApiResponse(responseCode = "500", description = "短信服务异常")
    })
    @RateLimit(limitForPeriod = 3, refreshPeriodSeconds = 300, message = "验证码发送过于频繁，请 5 分钟后再试")
    @PostMapping("/send")
    @ApiLog(value = "发送短信验证码", recordRequestBody = false, recordResponseBody = false)
    @OperationLog(module = "短信验证码认证", operation = "发送短信验证码", recordParams = false, recordResult = false)
    public GlobalResult<String> sendSmsCode(
            @Parameter(description = "发送验证码请求", required = true)
            @Valid @RequestBody SendSmsCodeRequest request) {
        return smsAuthAdminFacade.sendSmsCode(request);
    }

    /**
     * 短信验证码登录（非Spring Security方式，直接验证）
     * <p>
     * 注意：这是一个备用接口，主要的验证码登录应该通过 /api/v1/auth/login/sms 使用Spring Security过滤器
     * </p>
     */
    @Operation(summary = "短信验证码登录", description = "使用手机号和验证码登录系统")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "401", description = "验证码错误或已过期"),
            @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @RateLimit(limitForPeriod = 5, refreshPeriodSeconds = 300, message = "登录请求过于频繁，请 5 分钟后再试")
    @PostMapping("/login")
    @ApiLog(value = "短信验证码登录", recordRequestBody = false, recordResponseBody = false)
    @OperationLog(module = "短信验证码认证", operation = "短信验证码登录", recordParams = false, recordResult = false)
    public GlobalResult<TokenUser> smsLogin(
            @Parameter(description = "验证码登录请求", required = true)
            @Valid @RequestBody SmsLoginRequest request) {
        return smsAuthAdminFacade.smsLogin(request);
    }
}

