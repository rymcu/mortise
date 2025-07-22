package com.rymcu.mortise.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.github.f4b6a3.ulid.UlidCreator;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.core.util.UpdateEntity;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.auth.JwtConstants;
import com.rymcu.mortise.auth.TokenManager;
import com.rymcu.mortise.core.constant.ProjectConstant;
import com.rymcu.mortise.core.exception.AccountExistsException;
import com.rymcu.mortise.core.exception.BusinessException;
import com.rymcu.mortise.core.exception.CaptchaException;
import com.rymcu.mortise.core.result.ResultCode;
import com.rymcu.mortise.entity.Menu;
import com.rymcu.mortise.entity.Role;
import com.rymcu.mortise.entity.User;
import com.rymcu.mortise.handler.event.RegisterEvent;
import com.rymcu.mortise.handler.event.ResetPasswordEvent;
import com.rymcu.mortise.handler.event.UserLoginEvent;
import com.rymcu.mortise.mapper.MenuMapper;
import com.rymcu.mortise.mapper.RoleMapper;
import com.rymcu.mortise.mapper.UserMapper;
import com.rymcu.mortise.model.*;
import com.rymcu.mortise.service.UserService;
import com.rymcu.mortise.util.Utils;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.mybatisflex.core.query.QueryMethods.max;
import static com.rymcu.mortise.entity.table.UserTableDef.USER;

/**
 * Created on 2024/4/13 21:25.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service.impl
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private TokenManager tokenManager;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private MenuMapper menuMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    private final static String DEFAULT_AVATAR = "https://static.rymcu.com/article/1578475481946.png";
    private final static String DEFAULT_ACCOUNT = "1411780000";
    private final static String CURRENT_ACCOUNT_KEY = "current:account";
    private static final String REFRESH_TOKEN_KEY_PREFIX = "auth:refresh_token:";

    @Override
    public boolean updateLastOnlineTimeByAccount(String account) {
        return UpdateChain.of(User.class)
                .set(User::getLastOnlineTime, LocalDateTime.now())
                .where(User::getAccount).eq(account)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean register(String email, String nickname, String password, String code) throws AccountExistsException {
        String validateCodeKey = ProjectConstant.REDIS_REGISTER + email;
        String validateCode = stringRedisTemplate.boundValueOps(validateCodeKey).get();
        if (StringUtils.isNotBlank(validateCode)) {
            if (validateCode.equals(code)) {
                User user = findByAccount(email);
                if (user != null) {
                    throw new AccountExistsException("该邮箱已被注册！");
                } else {
                    user = new User();
                    user.setNickname(checkNickname(nickname));
                    user.setAccount(nextAccount());
                    user.setEmail(email);
                    user.setPassword(passwordEncoder.encode(password));
                    user.setAvatar(DEFAULT_AVATAR);
                    int result = mapper.insert(user);
                    if (result > 0) {
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


    private String checkNickname(String nickname) {
        nickname = formatNickname(nickname);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.where(USER.NICKNAME.eq(nickname));
        long result = mapper.selectCountByQuery(queryWrapper);
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
            currentAccount = mapper.selectObjectByQueryAs(query, String.class);
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
            User user = mapper.selectOneByQuery(QueryWrapper.create().where(USER.ACCOUNT.eq(account)).or(USER.EMAIL.eq(account)).or(USER.PHONE.eq(account)));
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
    public Set<String> findUserPermissionsByIdUser(Long idUser) {
        Set<String> permissions = new HashSet<>();
        List<Menu> menus = menuMapper.selectMenuListByIdUser(idUser);
        for (Menu menu : menus) {
            if (StringUtils.isNotBlank(menu.getPermission())) {
                permissions.add(menu.getPermission());
            }
        }
        permissions.add("user");
        permissions.addAll(findUserRoleListByIdUser(idUser));
        return permissions;
    }

    @Override
    public Set<String> findUserRoleListByIdUser(Long idUser) {
        List<Role> roles = roleMapper.selectRolesByIdUser(idUser);
        Set<String> permissions = new HashSet<>();
        for (Role role : roles) {
            if (StringUtils.isNotBlank(role.getPermission())) {
                permissions.add(role.getPermission());
            }
        }
        return permissions;
    }

    @Override
    public User findByAccount(String account) {
        if (StringUtils.isBlank(account)) {
            throw new UsernameNotFoundException(ResultCode.UNKNOWN_ACCOUNT.getMessage());
        }
        return mapper.selectOneByQuery(QueryWrapper.create().where(USER.ACCOUNT.eq(account)).or(USER.EMAIL.eq(account)).or(USER.PHONE.eq(account)));
    }

    /**
     * 查询用户
     *
     * @param search 查询条件
     * @return 用户信息列表
     */
    @Override
    public Page<UserInfo> findUsers(Page<UserInfo> page, UserSearch search) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("account", search.getAccount());
        queryParams.put("email", search.getEmail());
        queryParams.put("startDate", search.getStartDate());
        queryParams.put("endDate", search.getEndDate());
        queryParams.put("order", search.getOrder());
        queryParams.put("sort", search.getSort());
        queryParams.put("query", search.getQuery());
        Page<UserInfo> results = mapper.xmlPaginate("com.rymcu.mortise.mapper.UserMapper.selectUsers", page, queryParams);
        results.getRecords().forEach(userInfo -> {
            Avatar avatar = new Avatar();
            avatar.setAlt(userInfo.getNickname());
            avatar.setSrc(userInfo.getPicture());
            userInfo.setAvatar(avatar);
        });
        return results;
    }

    @Override
    public Boolean forgetPassword(String code, String password) {
        String email = stringRedisTemplate.boundValueOps(code).get();
        if (StringUtils.isBlank(email)) {
            throw new CaptchaException();
        } else {
            boolean result = UpdateChain.of(User.class)
                    .set(User::getPassword, passwordEncoder.encode(password))
                    .where(User::getEmail).eq(email)
                    .update();
            if (!result) {
                throw new BusinessException("密码修改失败!");
            }
            return true;
        }
    }

    @Override
    public UserInfo findUserInfoById(Long idUser) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select(USER.ID, USER.NICKNAME, USER.ACCOUNT, USER.PHONE,
                        USER.STATUS, USER.AVATAR, USER.EMAIL, USER.LAST_LOGIN_TIME,
                        USER.LAST_ONLINE_TIME, USER.CREATED_TIME)
                .from(User.class);
        return mapper.selectObjectByQueryAs(queryWrapper, UserInfo.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveUser(UserInfo userInfo) {
        boolean isUpdate = userInfo.getId() != null;
        User user;
        if (isUpdate) {
            user = mapper.selectOneById(userInfo.getId());
            if (Objects.nonNull(user)) {
                // 用户已存在
                user.setEmail(userInfo.getEmail());
                user.setPhone(userInfo.getPhone());
                user.setNickname(checkNickname(userInfo.getNickname()));
                user.setStatus(userInfo.getStatus());
                user.setAvatar(userInfo.getAvatar().getSrc());
                return mapper.update(user) > 0;
            }
            throw new BusinessException("用户不存在");
        } else {
            user = new User();
            user.setEmail(userInfo.getEmail());
            user.setPhone(userInfo.getPhone());
            user.setNickname(checkNickname(userInfo.getNickname()));
            String code = userInfo.getPassword();
            if (StringUtils.isBlank(code)) {
                code = String.valueOf(Utils.genCode());
            }
            user.setPassword(passwordEncoder.encode(code));
            user.setAvatar(Objects.isNull(userInfo.getAvatar()) ? DEFAULT_AVATAR : userInfo.getAvatar().getSrc());
            user.setAccount(nextAccount());
            boolean result = mapper.insert(user) > 0;
            if (result) {
                // 注册成功后执行相关初始化事件
                applicationEventPublisher.publishEvent(new RegisterEvent(user.getId(), user.getEmail(), code));
            }
            return result;
        }
    }

    @Override
    public Boolean updateUserInfo(UserInfo userInfo) {
        User user = mapper.selectOneById(userInfo.getId());
        user.setNickname(checkNickname(userInfo.getNickname()));
        user.setAvatar(userInfo.getAvatar().getSrc());
        user.setEmail(userInfo.getEmail());
        user.setPhone(userInfo.getPhone());
        return mapper.update(user) > 0;
    }

    @Override
    public Boolean bindUserRole(BindUserRoleInfo bindUserRoleInfo) {
        int num = 0;
        // 先删除原有关系
        mapper.deleteUserRole(bindUserRoleInfo.getIdUser());
        for (Long idRole : bindUserRoleInfo.getIdRoles()) {
            num += mapper.insertUserRole(bindUserRoleInfo.getIdUser(), idRole);
        }
        return num == bindUserRoleInfo.getIdRoles().size();
    }

    @Override
    public Boolean updateStatus(Long idUser, Integer status) {
        User user = UpdateEntity.of(User.class, idUser);
        user.setStatus(status);
        return mapper.update(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String resetPassword(Long idUser) {
        User user = mapper.selectOneById(idUser);
        if (Objects.nonNull(user)) {
            String code = String.valueOf(Utils.genCode());
            String password = passwordEncoder.encode(code);
            User u = UpdateEntity.of(User.class, idUser);
            u.setPassword(password);
            int result = mapper.update(u);
            if (result > 0) {
                applicationEventPublisher.publishEvent(new ResetPasswordEvent(user.getEmail(), code));
                return code;
            }
            throw new BusinessException("更新失败");
        }
        throw new BusinessException("用户不存在");
    }

    @Override
    public Boolean updateDelFlag(Long idUser, Integer delFlag) {
        return mapper.deleteById(idUser) > 0;
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
        User user = mapper.selectOneByQuery(QueryWrapper.create()
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
        int result = mapper.insert(newUser);

        if (result > 0) {
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
            mapper.update(existingUser);
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

    @Override
    public Boolean updateUserProfileInfo(UserProfileInfo userProfileInfo, User user) {
        User u = UpdateEntity.of(User.class, user.getId());
        if (!user.getNickname().equals(userProfileInfo.getNickname())) {
            u.setNickname(checkNickname(userProfileInfo.getNickname()));
        }
        u.setAvatar(userProfileInfo.getAvatar());
        return mapper.update(u) > 0;
    }

    @Override
    public void updateLastLoginTimeByAccount(String account) {
        UpdateChain.of(User.class)
                .set(User::getLastLoginTime, LocalDateTime.now())
                .set(User::getLastOnlineTime, LocalDateTime.now())
                .where(User::getAccount).eq(account)
                .update();
    }
}
