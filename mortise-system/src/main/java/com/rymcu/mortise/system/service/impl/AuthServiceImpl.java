package com.rymcu.mortise.system.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.rymcu.mortise.system.exception.AccountExistsException;
import com.rymcu.mortise.auth.service.TokenManager;
import com.rymcu.mortise.common.util.BeanCopierUtil;
import com.rymcu.mortise.common.util.Utils;
import com.rymcu.mortise.core.result.ResultCode;
import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.common.exception.CaptchaException;
import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.handler.event.RegisterEvent;
import com.rymcu.mortise.system.handler.event.UserLoginEvent;
import com.rymcu.mortise.system.model.auth.AuthInfo;
import com.rymcu.mortise.common.model.Link;
import com.rymcu.mortise.system.model.auth.TokenUser;
import com.rymcu.mortise.system.service.*;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


/**
 * Created on 2025/7/22 16:47.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service.impl
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private UserService userService;
    @Resource
    private MenuService menuService;
    @Resource
    private SystemNotificationService systemNotificationService;
    @Resource
    private TokenManager tokenManager;
    @Resource
    private SystemCacheService systemCacheService;
    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    private final static String DEFAULT_AVATAR = "https://static.rymcu.com/article/1578475481946.png";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean register(String email, String nickname, String password, String code) throws AccountExistsException {
        // 使用缓存服务验证验证码
        String validateCode = systemCacheService.getVerificationCode(email);
        if (StringUtils.isBlank(validateCode) || !validateCode.equals(code)) {
            throw new CaptchaException();
        }
        try {
            User user = new User();
            user.setNickname(userService.checkNickname(nickname));
            user.setAccount(userService.nextAccount());
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setAvatar(DEFAULT_AVATAR);
            boolean result = userService.save(user);
            if (result) {
                // 注册成功后执行相关初始化事件
                applicationEventPublisher.publishEvent(new RegisterEvent(user.getId(), user.getAccount(), ""));
                // 删除已使用的验证码
                systemCacheService.removeVerificationCode(email);
                return true;
            }
            throw new BusinessException(ResultCode.REGISTER_FAIL.getMessage());
        } catch (DataIntegrityViolationException e) {
            // 捕获数据库层面的唯一键冲突异常
            // 可以进一步检查异常信息确定是哪个字段冲突
            throw new AccountExistsException(ResultCode.EMAIL_EXISTS.getMessage());
        }
    }

    @Override
    public TokenUser login(@NotBlank String account, @NotBlank String password) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(account, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // 认证成功，可以获取用户信息
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByAccount(userDetails.getUsername());
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException(ResultCode.UNKNOWN_ACCOUNT.getMessage());
        }
        // 用户登陆事件
        applicationEventPublisher.publishEvent(new UserLoginEvent(user.getAccount()));
        return generateAndStoreTokens(user);
    }

    @Override
    public TokenUser refreshToken(String refreshToken) {
        // 使用缓存服务获取刷新令牌对应的账号
        String account = systemCacheService.getAccountByRefreshToken(refreshToken);
        if (StringUtils.isNotBlank(account)) {
            User user = userService.findByAccount(account);
            if (Objects.nonNull(user)) {
                TokenUser tokenUser = generateAndStoreTokens(user);
                // 删除旧的刷新令牌
                systemCacheService.removeRefreshToken(refreshToken);
                return tokenUser;
            }
        }
        throw new UsernameNotFoundException(ResultCode.UNKNOWN_ACCOUNT.getMessage());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenUser oauth2Login(OidcUser oidcUser, String registrationId) {
        // 查找或创建用户实体
        User user = findOrCreateUser(oidcUser, registrationId);

        // 为用户生成并存储令牌
        return generateAndStoreTokens(user);
    }

    @Override
    public void requestPasswordReset(String email) throws AccountNotFoundException {
        User user = userService.findByAccount(email);
        if (Objects.isNull(user)) {
            throw new AccountNotFoundException(ResultCode.UNKNOWN_ACCOUNT.getMessage());
        }
        String resetToken = UlidCreator.getUlid().toString();
        systemCacheService.storePasswordResetToken(resetToken, email);
        boolean result = systemNotificationService.sendPasswordResetEmail(email, resetToken);
        if (result) {
            throw new ServiceException(ResultCode.SEND_EMAIL_FAIL.getMessage());
        }
    }

    @Override
    public void requestEmailVerify(String email) throws AccountExistsException {
        User user = userService.findByAccount(email);
        if (user != null) {
            throw new AccountExistsException(ResultCode.EMAIL_EXISTS.getMessage());
        } else {
            String code = String.valueOf(Utils.genCode());
            systemCacheService.storeVerificationCode(email, code);
            boolean result = systemNotificationService.sendVerificationCodeEmail(email, code);
            if (result) {
                throw new BusinessException(ResultCode.SEND_EMAIL_FAIL.getMessage());
            }
        }
    }

    @Override
    public boolean forgetPassword(String code, String password) {
        // 使用缓存服务获取密码重置令牌对应的邮箱
        String email = systemCacheService.getEmailByResetToken(code);
        if (StringUtils.isBlank(email)) {
            throw new CaptchaException();
        } else {
            boolean result = UpdateChain.of(User.class)
                    .set(User::getPassword, passwordEncoder.encode(password))
                    .where(User::getEmail).eq(email)
                    .update();
            if (result) {
                // 删除已使用的密码重置令牌
                systemCacheService.removePasswordResetToken(code);
                return true;
            }
            throw new BusinessException(ResultCode.UPDATE_PASSWORD_FAIL.getMessage());
        }
    }

    @Override
    public AuthInfo userSession(User user) {
        AuthInfo authInfo = new AuthInfo();
        BeanCopierUtil.copy(user, authInfo);
        authInfo.setScope(userService.findUserPermissionsByIdUser(user.getId()));
        authInfo.setRole(userService.findUserRoleListByIdUser(user.getId()));
        authInfo.setLinks(menuService.findLinksByIdUser(user.getId()));
        return authInfo;
    }

    @Override
    public List<Link> userMenus(User user) {
        return menuService.findLinksByIdUser(user.getId());
    }

    /**
     * 核心逻辑：查找或创建用户。
     * 使用 @Transactional 注解确保数据库操作的原子性。
     */
    @Transactional(rollbackFor = Exception.class)
    protected User findOrCreateUser(OidcUser oidcUser, String registrationId) {
        User newUser = createNewUser(oidcUser, registrationId);

        try {
            String code = Utils.genPassword(); // 生成一个随机密码
            newUser.setPassword(passwordEncoder.encode(code));
            boolean saved = userService.save(newUser);
            if (saved) {
                // 注册成功后，发布事件，可用于发送欢迎邮件等后续操作
                applicationEventPublisher.publishEvent(new RegisterEvent(newUser.getId(), newUser.getEmail(), code));
                return newUser;
            }
        } catch (DataIntegrityViolationException e) {
            // 插入失败，意味着用户已存在。现在去查询并更新。
            User existingUser = userService.getMapper().selectOneByQuery(
                    QueryWrapper.create()
                            .where(User::getProvider).eq(registrationId)
                            .and(User::getOpenId).eq(oidcUser.getSubject())
            );
            if (existingUser != null) {
                return updateExistingUser(existingUser, oidcUser);
            }
        }

        throw new BusinessException(ResultCode.REGISTER_FAIL.getMessage());
    }

    /**
     * 创建一个全新的用户并持久化。
     */
    private User createNewUser(OidcUser oidcUser, String registrationId) {
        User newUser = new User();
        newUser.setNickname(userService.checkNickname(oidcUser.getName()));
        newUser.setEmail(oidcUser.getEmail());
        String picture = oidcUser.getPicture();
        newUser.setAvatar(StringUtils.isNotBlank(picture) ? picture : DEFAULT_AVATAR);
        newUser.setOpenId(oidcUser.getSubject());
        newUser.setProvider(registrationId);
        newUser.setAccount(userService.nextAccount());
        newUser.setCreatedTime(LocalDateTime.now());
        return newUser;
    }

    /**
     * 更新一个已存在的用户信息。
     */
    private User updateExistingUser(User existingUser, OidcUser oidcUser) {
        boolean needsUpdate = false;

        // 按需更新昵称
        String newNickname = userService.checkNickname(oidcUser.getName());
        if (!Objects.equals(existingUser.getNickname(), newNickname)) {
            existingUser.setNickname(newNickname);
            needsUpdate = true;
        }

        // 按需更新邮箱
        if (!Objects.equals(existingUser.getEmail(), oidcUser.getEmail())) {
            existingUser.setEmail(oidcUser.getEmail());
            needsUpdate = true;
        }

        // 按需更新头像（仅当OIDC提供了新头像且与旧的不同时）
        String newAvatar = oidcUser.getPicture();
        if (StringUtils.isNotBlank(newAvatar) && !Objects.equals(existingUser.getAvatar(), newAvatar)) {
            existingUser.setAvatar(newAvatar);
            needsUpdate = true;
        }

        if (needsUpdate) {
            userService.updateById(existingUser);
        }

        return existingUser;
    }

    /**
     * 为指定用户生成访问令牌和刷新令牌。
     */
    private TokenUser generateAndStoreTokens(User user) {
        String accessToken = tokenManager.createToken(user.getAccount());
        String refreshToken = UlidCreator.getUlid().toString();

        // 使用缓存服务存储刷新令牌
        systemCacheService.storeRefreshToken(refreshToken, user.getAccount());

        TokenUser tokenUser = new TokenUser();
        tokenUser.setToken(accessToken);
        tokenUser.setRefreshToken(refreshToken);

        return tokenUser;
    }
}
