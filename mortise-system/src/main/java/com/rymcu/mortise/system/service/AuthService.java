package com.rymcu.mortise.system.service;

import com.rymcu.mortise.system.exception.AccountExistsException;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.model.auth.AuthInfo;
import com.rymcu.mortise.common.model.Link;
import com.rymcu.mortise.system.model.auth.TokenUser;
import jakarta.mail.MessagingException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;

/**
 * Created on 2025/7/22 16:46.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service
 */
public interface AuthService {
    /**
     * 注册接口
     *
     * @param email    邮箱
     * @param nickname 昵称
     * @param password 密码
     * @param code     验证码
     * @return Boolean 注册成功标志
     */
    Boolean register(String email, String nickname, String password, String code) throws AccountExistsException;

    /**
     * 登录接口
     *
     * @param account  邮箱
     * @param password 密码
     * @return TokenUser
     */
    TokenUser login(String account, String password);

    /**
     * 刷新 token 接口
     *
     * @param refreshToken 刷新 token
     * @return TokenUser
     */
    TokenUser refreshToken(String refreshToken);


    TokenUser oauth2Login(OidcUser oidcUser, String registrationId);

    void requestPasswordReset(String email) throws AccountNotFoundException, MessagingException;

    void requestEmailVerify(String email) throws AccountExistsException, MessagingException;

    AuthInfo userSession(User user);

    List<Link> userMenus(User user);

    boolean forgetPassword(String code, String password);
}
