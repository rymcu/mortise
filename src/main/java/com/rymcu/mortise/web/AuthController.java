package com.rymcu.mortise.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import com.rymcu.mortise.auth.TokenManager;
import com.rymcu.mortise.core.exception.AccountExistsException;
import com.rymcu.mortise.core.exception.ServiceException;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.core.result.ResultCode;
import com.rymcu.mortise.entity.User;
import com.rymcu.mortise.model.*;
import com.rymcu.mortise.service.AuthService;
import com.rymcu.mortise.util.UserUtils;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;
import java.util.Objects;

/**
 * @author ronger
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Resource
    private AuthService authService;
    @Resource
    TokenManager tokenManager;

    @PostMapping("/login")
    @LogRecord(success = "提交成功", type = "系统", subType = "账号登录", bizNo = "{\"account\": {{#user.account}}}",
            fail = "提交失败，失败原因：「{{#_errorMsg ? #_errorMsg : #result.message }}」", extra = "{\"account\": {{#user.account}}}",
            successCondition = "{{#result.code==200}}")
    public GlobalResult<TokenUser> login(@RequestBody LoginInfo loginInfo) {
        TokenUser tokenUser = authService.login(loginInfo.getAccount(), loginInfo.getPassword());
        LogRecordContext.putVariable("idUser", tokenUser.getIdUser());
        tokenUser.setIdUser(null);
        GlobalResult<TokenUser> tokenUserGlobalResult = GlobalResult.success(tokenUser);
        LogRecordContext.putVariable("result", tokenUserGlobalResult);
        return tokenUserGlobalResult;
    }

    @PostMapping("/register")
    public GlobalResult<Boolean> register(@RequestBody RegisterInfo registerInfo) throws AccountExistsException {
        boolean flag = authService.register(registerInfo.getEmail(), registerInfo.getNickname(), registerInfo.getPassword(), registerInfo.getCode());
        return GlobalResult.success(flag);
    }

    @PostMapping("/refresh-token")
    public GlobalResult<TokenUser> refreshToken(@RequestBody TokenUser tokenUser) {
        tokenUser = authService.refreshToken(tokenUser.getRefreshToken());
        return GlobalResult.success(tokenUser);
    }

    @PostMapping("/logout")
    public GlobalResult<?> logout() {
        User user = UserUtils.getCurrentUserByToken();
        if (Objects.nonNull(user)) {
            tokenManager.deleteToken(user.getAccount());
        }
        return GlobalResult.success();
    }

    @GetMapping("/me")
    public GlobalResult<ObjectNode> userSession() {
        User user = UserUtils.getCurrentUserByToken();
        AuthInfo authInfo = authService.userSession(user);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode object = objectMapper.createObjectNode();
        object.set("user", objectMapper.valueToTree(authInfo));
        return GlobalResult.success(object);
    }

    @GetMapping("/password/request")
    public GlobalResult<String> requestPasswordReset(@RequestParam("email") String email) throws MessagingException, ServiceException, AccountNotFoundException {
        authService.requestPasswordReset(email);
        return GlobalResult.success(ResultCode.SUCCESS.getMessage());
    }

    @PatchMapping("/password/reset")
    public GlobalResult<Boolean> resetPassword(@RequestBody ForgetPasswordInfo forgetPassword) throws ServiceException {
        boolean flag = authService.forgetPassword(forgetPassword.getCode(), forgetPassword.getPassword());
        return GlobalResult.success(flag);
    }

    @GetMapping("/email/request")
    public GlobalResult<String> requestEmailVerify(@RequestParam("email") String email) throws MessagingException, AccountExistsException {
        authService.requestEmailVerify(email);
        return GlobalResult.success(ResultCode.SUCCESS.getMessage());
    }

    @GetMapping("/menus")
    public GlobalResult<List<Link>> menus() {
        User user = UserUtils.getCurrentUserByToken();
        List<Link> menus = authService.userMenus(user);
        return GlobalResult.success(menus);
    }

}
