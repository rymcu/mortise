package com.rymcu.mortise.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
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

    @Override
    public int updateLastOnlineTimeByAccount(String account) {
        return baseMapper.updateLastOnlineTimeByAccount(account);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean register(String email, String nickname, String password, String code) throws AccountExistsException {
        String validateCodeKey = ProjectConstant.REDIS_REGISTER + email;
        String validateCode = stringRedisTemplate.boundValueOps(validateCodeKey).get();
        if (StringUtils.isNotBlank(validateCode)) {
            if (validateCode.equals(code)) {
                User user = baseMapper.selectByAccount(email);
                if (user != null) {
                    throw new AccountExistsException("该邮箱已被注册！");
                } else {
                    user = new User();
                    user.setNickname(checkNickname(nickname));
                    user.setAccount(nextAccount());
                    user.setEmail(email);
                    user.setPassword(passwordEncoder.encode(password));
                    user.setAvatar(DEFAULT_AVATAR);
                    int result = baseMapper.insert(user);
                    if (result > 0) {
                        // 注册成功后执行相关初始化事件
                        applicationEventPublisher.publishEvent(new RegisterEvent(user.getIdUser(), user.getAccount(), ""));
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
        int result = baseMapper.selectCountByNickname(nickname);
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
            // 查询数据库
            currentAccount = baseMapper.selectMaxAccount();
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
                baseMapper.updateLastLoginTime(user.getUsername());
                baseMapper.updateLastOnlineTimeByAccount(user.getUsername());
                TokenUser tokenUser = new TokenUser();
                tokenUser.setToken(tokenManager.createToken(user.getUsername()));
                tokenUser.setRefreshToken(UlidCreator.getUlid().toString());
                stringRedisTemplate.boundValueOps(tokenUser.getRefreshToken()).set(user.getUsername(), JwtConstants.REFRESH_TOKEN_EXPIRES_HOUR, TimeUnit.HOURS);
                // 保存登录日志
                //                loginRecordService.saveLoginRecord(user.getIdUser());
                return tokenUser;
            }
        }
        throw new UsernameNotFoundException(ResultCode.UNKNOWN_ACCOUNT.getMessage());
    }

    @Override
    public TokenUser refreshToken(String refreshToken) {
        String account = stringRedisTemplate.boundValueOps(refreshToken).get();
        if (StringUtils.isNotBlank(account)) {
            User user = baseMapper.selectByAccount(account);
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
        return baseMapper.selectByAccount(account);
    }

    /**
     * 查询用户
     *
     * @param search 查询条件
     * @return 用户信息列表
     */
    @Override
    public List<UserInfo> findUsers(Page<UserInfo> page, UserSearch search) {
        List<UserInfo> list = baseMapper.selectUsers(page, search.getAccount(), search.getEmail(), search.getStartDate(), search.getEndDate(), search.getOrder(), search.getSort(), search.getQuery());
        list.forEach(userInfo -> {
            Avatar avatar = new Avatar();
            avatar.setAlt(userInfo.getNickname());
            avatar.setSrc(userInfo.getAvatarUrl());
            userInfo.setAvatar(avatar);
        });
        return list;
    }

    @Override
    public Boolean forgetPassword(String code, String password) {
        String email = stringRedisTemplate.boundValueOps(code).get();
        if (StringUtils.isBlank(email)) {
            throw new CaptchaException();
        } else {
            int result = baseMapper.updatePasswordByEmail(email, passwordEncoder.encode(password));
            if (result == 0) {
                throw new BusinessException("密码修改失败!");
            }
            return true;
        }
    }

    @Override
    public UserInfo findUserInfoById(Long idUser) {
        return baseMapper.selectUserInfoById(idUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveUser(UserInfo userInfo) {
        boolean isUpdate = userInfo.getIdUser() != null;
        User user;
        if (isUpdate) {
            user = baseMapper.selectById(userInfo.getIdUser());
            if (Objects.nonNull(user)) {
                // 用户已存在
                user.setEmail(userInfo.getEmail());
                user.setPhone(userInfo.getPhone());
                user.setNickname(checkNickname(userInfo.getNickname()));
                user.setStatus(userInfo.getStatus());
                user.setAvatar(userInfo.getAvatar().getSrc());
                return baseMapper.updateById(user) > 0;
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
            user.setCreatedTime(LocalDateTime.now());
            boolean result = baseMapper.insertOrUpdate(user);
            if (result) {
                // 注册成功后执行相关初始化事件
                applicationEventPublisher.publishEvent(new RegisterEvent(user.getIdUser(), user.getEmail(), code));
            }
            return result;
        }
    }

    @Override
    public Boolean updateUserInfo(UserInfo userInfo) {
        User user = baseMapper.selectById(userInfo.getIdUser());
        user.setNickname(checkNickname(userInfo.getNickname()));
        user.setAvatar(userInfo.getAvatar().getSrc());
        user.setEmail(userInfo.getEmail());
        user.setPhone(userInfo.getPhone());
        return baseMapper.updateById(user) > 0;
    }

    @Override
    public Boolean bindUserRole(BindUserRoleInfo bindUserRoleInfo) {
        int num = 0;
        // 先删除原有关系
        baseMapper.deleteUserRole(bindUserRoleInfo.getIdUser());
        for (Long idRole : bindUserRoleInfo.getIdRoles()) {
            num += baseMapper.insertUserRole(bindUserRoleInfo.getIdUser(), idRole);
        }
        return num == bindUserRoleInfo.getIdRoles().size();
    }

    @Override
    public Boolean updateStatus(Long idUser, Integer status) {
        return baseMapper.updateStatus(idUser, status) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String resetPassword(Long idUser) {
        User user = baseMapper.selectById(idUser);
        if (Objects.nonNull(user)) {
            String code = String.valueOf(Utils.genCode());
            String password = passwordEncoder.encode(code);
            int result = baseMapper.updatePasswordById(idUser, password);
            if (result > 0) {
                applicationEventPublisher.publishEvent(new ResetPasswordEvent(user.getEmail(), code));
            }
            return code;
        }
        throw new BusinessException("用户不存在");
    }

    @Override
    public Boolean updateDelFlag(Long idUser, Integer delFlag) {
        return baseMapper.updateDelFlag(idUser, delFlag) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenUser oauth2Login(OidcUser oidcUser, String registrationId) {
        // 提取 OIDC 用户信息
        String email = oidcUser.getEmail();
        String openId = oidcUser.getSubject();  // 唯一标识
        String nickname = oidcUser.getName();
        String picture = oidcUser.getPicture();
        User user = baseMapper.selectByOpenId(registrationId, openId);
        if (Objects.isNull(user)) {
            user = new User();
            user.setNickname(checkNickname(nickname));
            user.setEmail(email);
            user.setAvatar(StringUtils.isNotBlank(picture) ? picture : DEFAULT_AVATAR);
            user.setOpenId(openId);
            user.setProvider(registrationId);
            String code = UlidCreator.getUlid().toString();
            user.setPassword(passwordEncoder.encode(code));
            user.setAccount(nextAccount());
            user.setCreatedTime(LocalDateTime.now());
            boolean result = baseMapper.insertOrUpdate(user);
            if (result) {
                // 注册成功后执行相关初始化事件
                applicationEventPublisher.publishEvent(new RegisterEvent(user.getIdUser(), user.getEmail(), code));
            }
        } else {
            user.setNickname(checkNickname(nickname));
            user.setEmail(email);
            user.setAvatar(StringUtils.isNotBlank(picture) ? picture : user.getAvatar());
            baseMapper.insertOrUpdate(user);
        }
        TokenUser tokenUser = new TokenUser();
        tokenUser.setToken(tokenManager.createToken(user.getAccount()));
        tokenUser.setRefreshToken(UlidCreator.getUlid().toString());
        stringRedisTemplate.boundValueOps(tokenUser.getRefreshToken()).set(user.getAccount(), JwtConstants.REFRESH_TOKEN_EXPIRES_HOUR, TimeUnit.HOURS);
        return tokenUser;
    }
}
