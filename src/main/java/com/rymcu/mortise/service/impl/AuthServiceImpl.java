package com.rymcu.mortise.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.auth.JwtConstants;
import com.rymcu.mortise.auth.TokenManager;
import com.rymcu.mortise.core.constant.ProjectConstant;
import com.rymcu.mortise.core.exception.AccountExistsException;
import com.rymcu.mortise.core.exception.BusinessException;
import com.rymcu.mortise.core.exception.CaptchaException;
import com.rymcu.mortise.core.result.ResultCode;
import com.rymcu.mortise.entity.User;
import com.rymcu.mortise.handler.event.RegisterEvent;
import com.rymcu.mortise.handler.event.UserLoginEvent;
import com.rymcu.mortise.model.TokenUser;
import com.rymcu.mortise.service.AuthService;
import com.rymcu.mortise.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.mybatisflex.core.query.QueryMethods.max;
import static com.rymcu.mortise.entity.table.UserTableDef.USER;

/**
 * Created on 2025/7/22 16:47.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service.impl
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private TokenManager tokenManager;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;
    @Resource
    private UserService userService;

    private final static String DEFAULT_AVATAR = "https://static.rymcu.com/article/1578475481946.png";
    private final static String DEFAULT_ACCOUNT = "1411780000";
    private final static String CURRENT_ACCOUNT_KEY = "current:account";
    private static final String REFRESH_TOKEN_KEY_PREFIX = "auth:refresh_token:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean register(String email, String nickname, String password, String code) throws AccountExistsException {
        String validateCodeKey = ProjectConstant.REDIS_REGISTER + email;
        String validateCode = stringRedisTemplate.boundValueOps(validateCodeKey).get();
        if (StringUtils.isNotBlank(validateCode)) {
            if (validateCode.equals(code)) {
                User user = userService.findByAccount(email);
                if (user != null) {
                    throw new AccountExistsException("该邮箱已被注册！");
                } else {
                    user = new User();
                    user.setNickname(checkNickname(nickname));
                    user.setAccount(nextAccount());
                    user.setEmail(email);
                    user.setPassword(passwordEncoder.encode(password));
                    user.setAvatar(DEFAULT_AVATAR);
                    boolean result = userService.save(user);
                    if (result) {
                        // 注册成功后执行相关初始化事件
                        applicationEventPublisher.publishEvent(new RegisterEvent(user.getId(), user.getAccount(), ""));
                        stringRedisTemplate.delete(validateCodeKey);
                        return true;
                    }
                    throw new BusinessException("注册失败！");
                }
            }
        }
        throw new CaptchaException();
    }

    @Override
    public TokenUser login(@NotBlank String account, @NotBlank String password) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(account, password));
        if (authentication.isAuthenticated()) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // 认证成功，可以获取用户信息
            UserDetails user = (UserDetails) authentication.getPrincipal();
            if (Objects.nonNull(user)) {
                TokenUser tokenUser = new TokenUser();
                tokenUser.setToken(tokenManager.createToken(user.getUsername()));
                tokenUser.setRefreshToken(UlidCreator.getUlid().toString());
                stringRedisTemplate.boundValueOps(tokenUser.getRefreshToken()).set(user.getUsername(), JwtConstants.REFRESH_TOKEN_EXPIRES_HOUR, TimeUnit.HOURS);
                // 用户登陆事件
                applicationEventPublisher.publishEvent(new UserLoginEvent(user.getUsername()));
                return tokenUser;
            }
        }
        throw new UsernameNotFoundException(ResultCode.UNKNOWN_ACCOUNT.getMessage());
    }

    @Override
    public TokenUser refreshToken(String refreshToken) {
        String account = stringRedisTemplate.boundValueOps(refreshToken).get();
        if (StringUtils.isNotBlank(account)) {
            User user = userService.getMapper().selectOneByQuery(QueryWrapper.create().where(USER.ACCOUNT.eq(account)).or(USER.EMAIL.eq(account)).or(USER.PHONE.eq(account)));
            if (user != null) {
                TokenUser tokenUser = new TokenUser();
                tokenUser.setToken(tokenManager.createToken(user.getAccount()));
                tokenUser.setRefreshToken(UlidCreator.getUlid().toString());
                stringRedisTemplate.boundValueOps(tokenUser.getRefreshToken()).set(account, JwtConstants.REFRESH_TOKEN_EXPIRES_HOUR, TimeUnit.HOURS);
                stringRedisTemplate.delete(refreshToken);
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

    /**
     * 核心逻辑：查找或创建用户。
     * 使用 @Transactional 注解确保数据库操作的原子性。
     */
    @Transactional(rollbackFor = Exception.class)
    protected User findOrCreateUser(OidcUser oidcUser, String registrationId) {
        String openId = oidcUser.getSubject();

        // 1. 先根据唯一标识查询用户
        User user = userService.getMapper().selectOneByQuery(QueryWrapper.create()
                .select(USER.ID, USER.ACCOUNT, USER.AVATAR, USER.EMAIL, USER.NICKNAME)
                .where(USER.PROVIDER.eq(registrationId))
                .and(USER.OPEN_ID.eq(openId)));

        if (Objects.nonNull(user)) {
            // 2. 如果用户存在，更新其信息并返回
            return updateExistingUser(user, oidcUser);
        } else {
            // 3. 如果用户不存在，创建新用户并返回
            return createNewUser(oidcUser, registrationId);
        }

        // 注意：更强大的并发处理方式是直接尝试 INSERT，如果因为唯一键冲突而失败，
        // 再去 SELECT 和 UPDATE。这可以避免 "check-then-act" 竞态条件。
        // 但目前的实现在 @Transactional 下对于大多数场景已经足够。
    }

    /**
     * 创建一个全新的用户并持久化。
     */
    private User createNewUser(OidcUser oidcUser, String registrationId) {
        User newUser = new User();
        String rawPassword = UlidCreator.getUlid().toString(); // 生成一个安全的随机密码/代码

        newUser.setNickname(checkNickname(oidcUser.getName()));
        newUser.setEmail(oidcUser.getEmail());

        String picture = oidcUser.getPicture();
        newUser.setAvatar(StringUtils.isNotBlank(picture) ? picture : DEFAULT_AVATAR);

        newUser.setOpenId(oidcUser.getSubject());
        newUser.setProvider(registrationId);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setAccount(nextAccount()); // 假设这是一个生成唯一账户ID的方法
        newUser.setCreatedTime(LocalDateTime.now());

        // 使用 insert 而不是 update！
        boolean result = userService.save(newUser);

        if (result) {
            // 注册成功后，发布事件，可用于发送欢迎邮件等后续操作
            applicationEventPublisher.publishEvent(new RegisterEvent(newUser.getId(), newUser.getEmail(), rawPassword));
        }

        return newUser;
    }

    /**
     * 更新一个已存在的用户信息。
     */
    private User updateExistingUser(User existingUser, OidcUser oidcUser) {
        boolean needsUpdate = false;

        // 按需更新昵称
        String newNickname = checkNickname(oidcUser.getName());
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

        // 刷新令牌存入Redis，使用常量前缀
        String redisKey = REFRESH_TOKEN_KEY_PREFIX + refreshToken;
        stringRedisTemplate.boundValueOps(redisKey).set(
                user.getAccount(),
                JwtConstants.REFRESH_TOKEN_EXPIRES_HOUR,
                TimeUnit.HOURS
        );

        TokenUser tokenUser = new TokenUser();
        tokenUser.setToken(accessToken);
        tokenUser.setRefreshToken(refreshToken);

        return tokenUser;
    }

    private String checkNickname(String nickname) {
        nickname = formatNickname(nickname);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.where(USER.NICKNAME.eq(nickname));
        long result = userService.getMapper().selectCountByQuery(queryWrapper);
        if (result > 0) {
            StringBuilder stringBuilder = new StringBuilder(nickname);
            return checkNickname(stringBuilder.append("_").append(System.currentTimeMillis()).toString());
        }
        return nickname;
    }

    private String formatNickname(String nickname) {
        return nickname.replaceAll("\\.", "");
    }

    private String nextAccount() {
        // 获取当前账号
        String currentAccount = stringRedisTemplate.boundValueOps(CURRENT_ACCOUNT_KEY).get();
        BigDecimal account;
        if (StringUtils.isNotBlank(currentAccount)) {
            account = BigDecimal.valueOf(Long.parseLong(currentAccount));
        } else {
            // 查询最大账号
            QueryWrapper query = new QueryWrapper()
                    .select(max(USER.ACCOUNT)).from(USER);
            currentAccount = userService.getMapper().selectObjectByQueryAs(query, String.class);
            if (StringUtils.isNotBlank(currentAccount)) {
                account = BigDecimal.valueOf(Long.parseLong(currentAccount));
            } else {
                account = BigDecimal.valueOf(Long.parseLong(DEFAULT_ACCOUNT));
            }
        }
        currentAccount = account.add(BigDecimal.ONE).toString();
        stringRedisTemplate.boundValueOps(CURRENT_ACCOUNT_KEY).set(currentAccount);
        return currentAccount;
    }
}
