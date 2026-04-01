package com.rymcu.mortise.system.controller.facade.impl;

import com.rymcu.mortise.auth.enumerate.UserType;
import com.rymcu.mortise.auth.model.SendSmsCodeRequest;
import com.rymcu.mortise.auth.model.SmsLoginRequest;
import com.rymcu.mortise.auth.service.SmsCodeService;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.controller.facade.SmsAuthAdminFacade;
import com.rymcu.mortise.system.model.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmsAuthAdminFacadeImpl implements SmsAuthAdminFacade {

    private final SmsCodeService smsCodeService;

    public SmsAuthAdminFacadeImpl(SmsCodeService smsCodeService) {
        this.smsCodeService = smsCodeService;
    }

    @Override
    public GlobalResult<String> sendSmsCode(SendSmsCodeRequest request) {
        String userType = request.getUserType();
        if (StringUtils.isBlank(userType)) {
            userType = UserType.SYSTEM.getCode();
        }

        String mobileMasked = maskMobile(request.getMobile());
        log.info("发送短信验证码请求: mobile={}, userType={}", mobileMasked, userType);

        try {
            String code = smsCodeService.generateAndSend(request.getMobile(), userType);
            if (code != null) {
                return GlobalResult.success("验证码已发送（测试环境）: " + code);
            }
            return GlobalResult.success("验证码已发送，请注意查收");
        } catch (IllegalStateException e) {
            log.warn("发送验证码失败: {}", e.getMessage());
            return GlobalResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("发送验证码异常", e);
            return GlobalResult.error("验证码发送失败，请稍后重试");
        }
    }

    @Override
    public GlobalResult<TokenUser> smsLogin(SmsLoginRequest request) {
        String userType = request.getUserType();
        if (StringUtils.isBlank(userType)) {
            userType = UserType.SYSTEM.getCode();
        }

        String mobileMasked = maskMobile(request.getMobile());
        log.info("短信验证码登录请求: mobile={}, userType={}", mobileMasked, userType);

        try {
            return GlobalResult.error("该接口暂未实现，请使用 POST /api/v1/auth/login/sms 进行验证码登录");
        } catch (Exception e) {
            log.error("短信验证码登录失败: mobile={}", mobileMasked, e);
            return GlobalResult.error("登录失败: " + e.getMessage());
        }
    }

    private String maskMobile(String mobile) {
        if (StringUtils.isBlank(mobile)) {
            return mobile;
        }
        if (mobile.length() <= 7) {
            return "***";
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }
}
