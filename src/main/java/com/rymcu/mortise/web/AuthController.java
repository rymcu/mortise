package com.rymcu.mortise.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import com.rymcu.mortise.auth.TokenManager;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.core.result.GlobalResultGenerator;
import com.rymcu.mortise.entity.User;
import com.rymcu.mortise.model.AuthInfo;
import com.rymcu.mortise.model.Link;
import com.rymcu.mortise.model.TokenUser;
import com.rymcu.mortise.service.MenuService;
import com.rymcu.mortise.service.UserService;
import com.rymcu.mortise.util.BeanCopierUtil;
import com.rymcu.mortise.util.UserUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/menus")
    public GlobalResult<List<Link>> menus() {
        User user = UserUtils.getCurrentUserByToken();
        List<Link> menus = menuService.findLinksByIdUser(user.getIdUser());
        return GlobalResultGenerator.genSuccessResult(menus);
    }

    @PostMapping("/login")
    @LogRecord(success = "提交成功", type = "系统", subType = "账号登录", bizNo = "{\"account\": {{#user.account}}}",
            fail = "提交失败，失败原因：「{{#_errorMsg ? #_errorMsg : #result.message }}」", extra = "{\"account\": {{#user.account}}}",
            successCondition = "{{#result.code==200}}")
    public GlobalResult<TokenUser> login(@RequestBody User user) {
        TokenUser tokenUser = userService.login(user.getAccount(), user.getPassword());
        LogRecordContext.putVariable("idUser", tokenUser.getIdUser());
        tokenUser.setIdUser(null);
        GlobalResult<TokenUser> tokenUserGlobalResult = GlobalResultGenerator.genSuccessResult(tokenUser);
        LogRecordContext.putVariable("result", tokenUserGlobalResult);
        return tokenUserGlobalResult;
    }

    @PostMapping("/refresh-token")
    public GlobalResult<TokenUser> refreshToken(@RequestBody TokenUser tokenUser) {
        tokenUser = userService.refreshToken(tokenUser.getRefreshToken());
        return GlobalResultGenerator.genSuccessResult(tokenUser);
    }

    @PostMapping("/logout")
    public GlobalResult<?> logout() {
        User user = UserUtils.getCurrentUserByToken();
        if (Objects.nonNull(user)) {
            tokenManager.deleteToken(user.getAccount());
        }
        return GlobalResultGenerator.genSuccessResult();
    }

    @GetMapping("/me")
    public GlobalResult<ObjectNode> user() {
        User user = UserUtils.getCurrentUserByToken();
        AuthInfo authInfo = new AuthInfo();
        BeanCopierUtil.copy(user, authInfo);
        authInfo.setScope(userService.findUserPermissionsByIdUser(user.getIdUser()));
        authInfo.setRole(userService.findUserRoleListByIdUser(user.getIdUser()));
        authInfo.setLinks(menuService.findLinksByIdUser(user.getIdUser()));
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode object = objectMapper.createObjectNode();
        object.set("user", objectMapper.valueToTree(authInfo));
        return GlobalResultGenerator.genSuccessResult(object);
    }

}
