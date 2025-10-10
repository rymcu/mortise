package com.rymcu.mortise.system.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.mybatisflex.core.update.UpdateChain;
import com.rymcu.mortise.auth.service.TokenManager;
import com.rymcu.mortise.auth.spi.StandardOAuth2UserInfo;
import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.common.exception.CaptchaException;
import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.common.model.Link;
import com.rymcu.mortise.common.util.BeanCopierUtil;
import com.rymcu.mortise.common.util.Utils;
import com.rymcu.mortise.core.result.ResultCode;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.entity.UserOAuth2Binding;
import com.rymcu.mortise.system.exception.AccountExistsException;
import com.rymcu.mortise.system.handler.event.RegisterEvent;
import com.rymcu.mortise.system.handler.event.UserLoginEvent;
import com.rymcu.mortise.system.model.auth.AuthInfo;
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
    @Resource
    private UserOAuth2BindingService userOAuth2BindingService;

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
        try {
            // 使用缓存服务获取刷新令牌对应的账号
            String account = systemCacheService.getAccountByRefreshToken(refreshToken);
            if (StringUtils.isBlank(account)) {
                log.warn("无效的刷新令牌");
                throw new BusinessException("刷新令牌无效或已过期");
            }

            // 查询用户信息
            User user = userService.findByAccount(account);
            if (Objects.isNull(user)) {
                log.warn("刷新令牌关联的用户不存在: account={}", account);
                throw new UsernameNotFoundException(ResultCode.UNKNOWN_ACCOUNT.getMessage());
            }

            // 生成新的 Access Token 和 Refresh Token
            TokenUser tokenUser = generateAndStoreTokens(user);

            // 删除旧的刷新令牌（一次性使用原则）
            systemCacheService.removeRefreshToken(refreshToken);

            log.info("令牌刷新成功: account={}", account);
            return tokenUser;

        } catch (UsernameNotFoundException | BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("刷新令牌时发生异常", e);
            throw new BusinessException("刷新令牌失败");
        }
    }

    @Override
    public String refreshAccessToken(String accessToken, String account) {
        try {
            // 验证用户是否存在
            User user = userService.findByAccount(account);
            if (Objects.isNull(user)) {
                log.warn("用户不存在: account={}", account);
                return null;
            }

            // 使用 TokenManager 刷新 Access Token
            String newAccessToken = tokenManager.refreshAccessToken(accessToken, account);
            if (newAccessToken != null) {
                log.info("Access Token 刷新成功: account={}", account);
            } else {
                log.debug("Access Token 暂不需要刷新或刷新失败: account={}", account);
            }

            return newAccessToken;

        } catch (Exception e) {
            log.error("刷新 Access Token 时发生异常: account={}", account, e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Deprecated
    public TokenUser oauth2Login(OidcUser oidcUser, String registrationId) {
        // 兼容旧方法，转换为新方法调用
        StandardOAuth2UserInfo userInfo = StandardOAuth2UserInfo.builder()
                .provider(registrationId)
                .openId(oidcUser.getSubject())
                .nickname(oidcUser.getName())
                .email(oidcUser.getEmail())
                .avatar(oidcUser.getPicture())
                .build();

        User user = findOrCreateUserFromOAuth2(userInfo);
        return generateTokens(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User findOrCreateUserFromOAuth2(StandardOAuth2UserInfo userInfo) {
        log.info("从 OAuth2 查找或创建用户: provider={}, openId={}, unionId={}, email={}",
                userInfo.getProvider(), userInfo.getOpenId(), userInfo.getUnionId(), userInfo.getEmail());

        // 1. 查找已存在的 OAuth2 绑定关系
        UserOAuth2Binding existingBinding = findExistingBinding(userInfo);

        if (existingBinding != null) {
            // 找到绑定关系，获取用户信息
            User existingUser = userService.getById(existingBinding.getUserId());
            if (existingUser != null) {
                log.info("找到已存在的用户: userId={}, account={}",
                        existingUser.getId(), existingUser.getAccount());

                // 更新绑定信息
                updateOAuth2Binding(existingBinding, userInfo);
                return existingUser;
            }
        }

        // 2. 尝试通过邮箱匹配现有用户（可选功能）
        User existingUser;
        if (StringUtils.isNotBlank(userInfo.getEmail())) {
            existingUser = userService.findByAccount(userInfo.getEmail());
            if (existingUser != null) {
                log.info("通过邮箱匹配到现有用户: userId={}, email={}",
                        existingUser.getId(), userInfo.getEmail());

                // 创建 OAuth2 绑定关系
                createOAuth2Binding(existingUser.getId(), userInfo);
                return existingUser;
            }
        }
        // 3. 尝试通过手机号匹配现有用户
        if (StringUtils.isNotBlank(userInfo.getPhone())) {
            existingUser = userService.findByAccount(userInfo.getPhone());
            if (existingUser != null) {
                log.info("通过手机号匹配到现有用户: userId={}, phone={}",
                        existingUser.getId(), userInfo.getPhone());

                // 创建 OAuth2 绑定关系
                createOAuth2Binding(existingUser.getId(), userInfo);
                return existingUser;
            }
        }

        // 4. 创建新用户和绑定关系
        User newUser = createNewUserFromOAuth2(userInfo);

        try {
            String randomPassword = Utils.genKey();
            newUser.setPassword(passwordEncoder.encode(randomPassword));
            boolean saved = userService.save(newUser);

            if (saved) {
                log.info("创建新用户成功: userId={}, account={}", newUser.getId(), newUser.getAccount());

                // 创建 OAuth2 绑定关系
                createOAuth2Binding(newUser.getId(), userInfo);

                // 发布注册事件
                applicationEventPublisher.publishEvent(
                        new RegisterEvent(newUser.getId(), newUser.getAccount(), randomPassword)
                );

                return newUser;
            }
        } catch (DataIntegrityViolationException e) {
            log.warn("用户创建冲突，重新查询: {}", e.getMessage());

            // 并发创建冲突，重新查询绑定关系
            UserOAuth2Binding retryBinding = userOAuth2BindingService.findByProviderAndOpenId(
                    userInfo.getProvider(), userInfo.getOpenId()
            );

            if (retryBinding != null) {
                User retryUser = userService.getById(retryBinding.getUserId());
                if (retryUser != null) {
                    return retryUser;
                }
            }
        }

        throw new BusinessException(ResultCode.REGISTER_FAIL.getMessage());
    }

    @Override
    public TokenUser generateTokens(User user) {
        return generateAndStoreTokens(user);
    }

    /**
     * 从 OAuth2 用户信息创建新用户
     */
    private User createNewUserFromOAuth2(StandardOAuth2UserInfo userInfo) {
        User newUser = new User();
        newUser.setNickname(userService.checkNickname(userInfo.getNickname()));
        newUser.setAccount(userService.nextAccount());
        newUser.setEmail(userInfo.getEmail());
        newUser.setPhone(userInfo.getPhone());

        String avatar = userInfo.getAvatar();
        newUser.setAvatar(StringUtils.isNotBlank(avatar) ? avatar : DEFAULT_AVATAR);

        newUser.setCreatedTime(LocalDateTime.now());

        return newUser;
    }

    /**
     * 创建 OAuth2 绑定关系
     */
    private void createOAuth2Binding(Long userId, StandardOAuth2UserInfo userInfo) {
        UserOAuth2Binding binding = new UserOAuth2Binding();
        binding.setUserId(userId);
        binding.setProvider(userInfo.getProvider());
        binding.setOpenId(userInfo.getOpenId());
        binding.setUnionId(userInfo.getUnionId()); // 微信 UnionID
        binding.setNickname(userInfo.getNickname());
        binding.setAvatar(userInfo.getAvatar());
        binding.setEmail(userInfo.getEmail());
        binding.setCreatedTime(LocalDateTime.now());
        binding.setUpdatedTime(LocalDateTime.now());

        userOAuth2BindingService.save(binding);
        log.debug("创建 OAuth2 绑定: userId={}, provider={}, openId={}, unionId={}",
                userId, userInfo.getProvider(), userInfo.getOpenId(), userInfo.getUnionId());
    }

    /**
     * 更新 OAuth2 绑定信息
     */
    private void updateOAuth2Binding(UserOAuth2Binding binding, StandardOAuth2UserInfo userInfo) {
        boolean needsUpdate = false;

        // 按需更新 UnionID（仅微信）
        if (StringUtils.isNotBlank(userInfo.getUnionId())
                && !Objects.equals(binding.getUnionId(), userInfo.getUnionId())) {
            binding.setUnionId(userInfo.getUnionId());
            needsUpdate = true;
        }

        // 按需更新昵称
        if (StringUtils.isNotBlank(userInfo.getNickname())
                && !Objects.equals(binding.getNickname(), userInfo.getNickname())) {
            binding.setNickname(userInfo.getNickname());
            needsUpdate = true;
        }

        // 按需更新邮箱
        if (StringUtils.isNotBlank(userInfo.getEmail())
                && !Objects.equals(binding.getEmail(), userInfo.getEmail())) {
            binding.setEmail(userInfo.getEmail());
            needsUpdate = true;
        }

        // 按需更新头像
        if (StringUtils.isNotBlank(userInfo.getAvatar())
                && !Objects.equals(binding.getAvatar(), userInfo.getAvatar())) {
            binding.setAvatar(userInfo.getAvatar());
            needsUpdate = true;
        }

        if (needsUpdate) {
            binding.setUpdatedTime(LocalDateTime.now());
            userOAuth2BindingService.updateById(binding);
            log.debug("更新 OAuth2 绑定信息: bindingId={}", binding.getId());
        }
    }

    /**
     * 查找已存在的绑定关系
     * 优先通过 openId 查找，如果是微信且有 unionId，也尝试通过 unionId 查找
     */
    private UserOAuth2Binding findExistingBinding(StandardOAuth2UserInfo userInfo) {
        // 1. 首先通过 provider + openId 查找（标准查找方式）
        UserOAuth2Binding binding = userOAuth2BindingService.findByProviderAndOpenId(
                userInfo.getProvider(), userInfo.getOpenId()
        );

        if (binding != null) {
            return binding;
        }

        // 2. 如果是微信且有 unionId，尝试通过 unionId 查找
        // 这可以解决同一微信用户在不同应用（公众号、小程序）的账号关联问题
        if ("wechat".equalsIgnoreCase(userInfo.getProvider())
                && StringUtils.isNotBlank(userInfo.getUnionId())) {
            binding = userOAuth2BindingService.findByProviderAndUnionId(
                    userInfo.getProvider(), userInfo.getUnionId()
            );

            if (binding != null) {
                log.info("通过 unionId 找到已存在的微信绑定: unionId={}, userId={}",
                        userInfo.getUnionId(), binding.getUserId());

                // 更新 openId（可能是从公众号切换到小程序，或反之）
                if (!Objects.equals(binding.getOpenId(), userInfo.getOpenId())) {
                    binding.setOpenId(userInfo.getOpenId());
                    binding.setUpdatedTime(LocalDateTime.now());
                    userOAuth2BindingService.updateById(binding);
                    log.debug("更新微信绑定的 openId: bindingId={}, newOpenId={}",
                            binding.getId(), userInfo.getOpenId());
                }
            }
        }

        return binding;
    }


    @Override
    public void requestPasswordReset(String email) throws AccountNotFoundException {
        User user = userService.findByAccount(email);
        if (Objects.isNull(user)) {
            throw new AccountNotFoundException(ResultCode.UNKNOWN_ACCOUNT.getMessage());
        }
        String resetToken = Utils.genKey();
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
    public void storeOauth2TokenUser(String state, TokenUser tokenUser) {
        systemCacheService.storeTokenUser(state, tokenUser);
    }

    @Override
    public TokenUser getOauth2TokenUser(String state) {
        return systemCacheService.getOauth2TokenUser(state);
    }

    @Override
    public AuthInfo userSession(User user) {
        AuthInfo authInfo = new AuthInfo();
        BeanCopierUtil.copy(user, authInfo);
        authInfo.setScope(userService.findUserPermissionsByIdUser(user.getId()));
        authInfo.setRole(userService.findUserRoleListByIdUser(user.getId()));
        authInfo.setLinks(userMenus(user));
        return authInfo;
    }

    @Override
    public List<Link> userMenus(User user) {
        return menuService.findLinksByIdUser(user.getId());
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
