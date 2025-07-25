package com.rymcu.mortise.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.core.util.CollectionUtil;
import com.mybatisflex.core.util.UpdateEntity;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.core.exception.BusinessException;
import com.rymcu.mortise.core.result.ResultCode;
import com.rymcu.mortise.entity.Menu;
import com.rymcu.mortise.entity.Role;
import com.rymcu.mortise.entity.User;
import com.rymcu.mortise.entity.UserRole;
import com.rymcu.mortise.handler.event.RegisterEvent;
import com.rymcu.mortise.handler.event.ResetPasswordEvent;
import com.rymcu.mortise.mapper.MenuMapper;
import com.rymcu.mortise.mapper.RoleMapper;
import com.rymcu.mortise.mapper.UserMapper;
import com.rymcu.mortise.model.*;
import com.rymcu.mortise.service.UserService;
import com.rymcu.mortise.util.Utils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.mybatisflex.core.query.QueryMethods.max;
import static com.rymcu.mortise.entity.table.UserRoleTableDef.USER_ROLE;
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
    private RoleMapper roleMapper;
    @Resource
    private MenuMapper menuMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    private final static String DEFAULT_AVATAR = "https://static.rymcu.com/article/1578475481946.png";
    private final static String DEFAULT_ACCOUNT = "1411780000";
    private final static String CURRENT_ACCOUNT_KEY = "current:account";

    @Override
    public boolean updateLastOnlineTimeByAccount(String account) {
        return UpdateChain.of(User.class)
                .set(User::getLastOnlineTime, LocalDateTime.now())
                .where(User::getAccount).eq(account)
                .update();
    }

    @Override
    public String checkNickname(String nickname) {
        nickname = formatNickname(nickname);
        long result = mapper.selectCountByQuery(QueryWrapper.create().where(USER.NICKNAME.eq(nickname)));
        if (result > 0) {
            StringBuilder stringBuilder = new StringBuilder(nickname);
            return checkNickname(stringBuilder.append("_").append(System.currentTimeMillis()).toString());
        }
        return nickname;
    }

    private String formatNickname(String nickname) {
        return nickname.replaceAll("\\.", "");
    }

    @Override
    public String nextAccount() {
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
        LocalDateTime startDate = LocalDate.now().atTime(LocalTime.MIN);
        if (StringUtils.isNotBlank(search.getStartDate())) {
            startDate = LocalDate.parse(search.getStartDate()).atTime(LocalTime.MIN);
        }
        LocalDateTime endDate = LocalDate.now().atTime(LocalTime.MAX);
        if (StringUtils.isNotBlank(search.getEndDate())) {
            endDate = LocalDate.parse(search.getEndDate()).atTime(LocalTime.MAX);
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(USER.ID, USER.NICKNAME, USER.ACCOUNT, USER.STATUS, USER.AVATAR, USER.EMAIL, USER.PHONE, USER.LAST_LOGIN_TIME, USER.LAST_ONLINE_TIME, USER.CREATED_TIME)
                .where(USER.ACCOUNT.eq(search.getAccount(), StringUtils::isNotBlank))
                .and(USER.EMAIL.eq(search.getEmail(), StringUtils::isNotBlank))
                .and(USER.ACCOUNT.like(search.getQuery(), StringUtils::isNotBlank)
                        .or(USER.NICKNAME.like(search.getQuery(), StringUtils::isNotBlank)))
                .and(USER.CREATED_TIME.between(startDate, endDate, StringUtils.isNotBlank(search.getStartDate())))
                .from(USER);
        Page<UserInfo> results = mapper.paginateAs(page, queryWrapper, UserInfo.class);
        results.getRecords().forEach(userInfo -> {
            Avatar avatar = new Avatar();
            avatar.setAlt(userInfo.getNickname());
            avatar.setSrc(userInfo.getPicture());
            userInfo.setAvatar(avatar);
        });
        return results;
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
            user = mapper.selectOneByQuery(QueryWrapper.create()
                    .select(USER.ID, USER.EMAIL, USER.PHONE, USER.NICKNAME, USER.STATUS, USER.AVATAR)
                    .where(USER.ID.eq(userInfo.getId())));
            if (Objects.nonNull(user)) {
                // 用户已存在
                user.setEmail(userInfo.getEmail());
                user.setPhone(userInfo.getPhone());
                user.setNickname(checkNickname(userInfo.getNickname()));
                user.setStatus(userInfo.getStatus());
                user.setAvatar(userInfo.getAvatar().getSrc());
                return mapper.update(user) > 0;
            }
            throw new BusinessException(ResultCode.UNKNOWN_ACCOUNT.getMessage());
        } else {
            user = new User();
            user.setEmail(userInfo.getEmail());
            user.setPhone(userInfo.getPhone());
            user.setNickname(checkNickname(userInfo.getNickname()));
            String code = userInfo.getPassword();
            if (StringUtils.isBlank(code)) {
                code = Utils.genPassword();
            }
            user.setPassword(passwordEncoder.encode(code));
            user.setAvatar(Objects.isNull(userInfo.getAvatar()) ? DEFAULT_AVATAR : userInfo.getAvatar().getSrc());
            user.setAccount(nextAccount());
            boolean result = mapper.insertSelective(user) > 0;
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
        QueryWrapper queryWrapper = QueryWrapper.create().where(UserRole::getIdMortiseUser).eq(bindUserRoleInfo.getIdUser());
        Db.deleteByQuery(USER_ROLE.getTableName(), queryWrapper);
        if (CollectionUtil.isNotEmpty(bindUserRoleInfo.getIdRoles())) {
            List<Row> userRoles = bindUserRoleInfo.getIdRoles().stream().map(roleId -> {
                Row row = new Row();
                row.set(USER_ROLE.ID_MORTISE_USER, bindUserRoleInfo.getIdUser());
                row.set(USER_ROLE.ID_MORTISE_ROLE, roleId);
                return row;
            }).toList();
            int[] result = Db.insertBatch(USER_ROLE.getTableName(), userRoles);
            for (int i : result) {
                num += i;
            }
            return num == bindUserRoleInfo.getIdRoles().size();
        }
        return true;
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
        User user = mapper.selectOneByQuery(QueryWrapper.create()
                .select(USER.ID, USER.PASSWORD)
                .where(USER.ID.eq(idUser)));
        if (Objects.nonNull(user)) {
            String code = String.valueOf(Utils.genCode());
            String password = passwordEncoder.encode(code);
            user.setPassword(password);
            int result = mapper.update(user);
            if (result > 0) {
                applicationEventPublisher.publishEvent(new ResetPasswordEvent(user.getEmail(), code));
                return code;
            }
            throw new BusinessException(ResultCode.FAIL.getMessage());
        }
        throw new UsernameNotFoundException(ResultCode.UNKNOWN_ACCOUNT.getMessage());
    }

    @Override
    public Boolean updateDelFlag(Long idUser, Integer delFlag) {
        return mapper.deleteById(idUser) > 0;
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
