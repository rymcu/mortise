package com.rymcu.mortise.system.controller.facade;

import com.rymcu.mortise.auth.model.SendSmsCodeRequest;
import com.rymcu.mortise.auth.model.SmsLoginRequest;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.model.TokenUser;

public interface SmsAuthAdminFacade {

    GlobalResult<String> sendSmsCode(SendSmsCodeRequest request);

    GlobalResult<TokenUser> smsLogin(SmsLoginRequest request);
}
