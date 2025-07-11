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
import com.rymcu.mortise.service.JavaMailService;
import com.rymcu.mortise.service.MenuService;
import com.rymcu.mortise.service.UserService;
import com.rymcu.mortise.util.BeanCopierUtil;
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
    private MenuService menuService;
    @Resource
    private UserService userService;
    @Resource
    TokenManager tokenManager;
    @Resource
    private JavaMailService javaMailService;

    @GetMapping("/menus")
    public GlobalResult<List<Link>> menus() {
        User user = UserUtils.getCurrentUserByToken();
        List<Link> menus = menuService.findLinksByIdUser(user.getId());
        return GlobalResult.success(menus);
    }

    @PostMapping("/login")
    @LogRecord(success = "提交成功", type = "系统", subType = "账号登录", bizNo = "{\"account\": {{#user.account}}}",
            fail = "提交失败，失败原因：「{{#_errorMsg ? #_errorMsg : #result.message }}」", extra = "{\"account\": {{#user.account}}}",
            successCondition = "{{#result.code==200}}")
    public GlobalResult<TokenUser> login(@RequestBody LoginInfo loginInfo) {
        TokenUser tokenUser = userService.login(loginInfo.getAccount(), loginInfo.getPassword());
        LogRecordContext.putVariable("idUser", tokenUser.getIdUser());
        tokenUser.setIdUser(null);
        GlobalResult<TokenUser> tokenUserGlobalResult = GlobalResult.success(tokenUser);
        LogRecordContext.putVariable("result", tokenUserGlobalResult);
        return tokenUserGlobalResult;
    }

    @PostMapping("/register")
    public GlobalResult<Boolean> register(@RequestBody RegisterInfo registerInfo) throws AccountExistsException {
        boolean flag = userService.register(registerInfo.getEmail(), registerInfo.getNickname(), registerInfo.getPassword(), registerInfo.getCode());
        return GlobalResult.success(flag);
    }

    @PostMapping("/refresh-token")
    public GlobalResult<TokenUser> refreshToken(@RequestBody TokenUser tokenUser) {
        tokenUser = userService.refreshToken(tokenUser.getRefreshToken());
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
    public GlobalResult<ObjectNode> user() {
        User user = UserUtils.getCurrentUserByToken();
        AuthInfo authInfo = new AuthInfo();
        BeanCopierUtil.copy(user, authInfo);
        authInfo.setScope(userService.findUserPermissionsByIdUser(user.getId()));
        authInfo.setRole(userService.findUserRoleListByIdUser(user.getId()));
        authInfo.setLinks(menuService.findLinksByIdUser(user.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode object = objectMapper.createObjectNode();
        object.set("user", objectMapper.valueToTree(authInfo));
        return GlobalResult.success(object);
    }

    @GetMapping("/password/request")
    public GlobalResult<String> requestPasswordReset(@RequestParam("email") String email) throws MessagingException, ServiceException, AccountNotFoundException {
        User user = userService.findByAccount(email);
        if (user != null) {
            int result = javaMailService.sendForgetPasswordEmail(email);
            if (result == 0) {
                throw new ServiceException(ResultCode.SEND_EMAIL_FAIL.getMessage());
            }
        } else {
            throw new AccountNotFoundException("未知账号");
        }
        return GlobalResult.success(ResultCode.SUCCESS.getMessage());
    }

    @PatchMapping("/password/reset")
    public GlobalResult<Boolean> resetPassword(@RequestBody ForgetPasswordInfo forgetPassword) throws ServiceException {
        boolean flag = userService.forgetPassword(forgetPassword.getCode(), forgetPassword.getPassword());
        return GlobalResult.success(flag);
    }

    @GetMapping("/email/request")
    public GlobalResult<String> requestEmailVerify(@RequestParam("email") String email) throws MessagingException, AccountExistsException {
        User user = userService.findByAccount(email);
        if (user != null) {
            throw new AccountExistsException("该邮箱已被注册!");
        } else {
            int result = javaMailService.sendEmailCode(email);
            if (result == 0) {
                return GlobalResult.error(ResultCode.SEND_EMAIL_FAIL.getMessage());
            }
        }
        return GlobalResult.success(ResultCode.SUCCESS.getMessage());
    }

}
