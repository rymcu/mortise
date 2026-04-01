package com.rymcu.mortise.system.repository;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.infra.persistence.FlexPageMapper;
import com.rymcu.mortise.system.infra.persistence.PersistenceObjectMapper;
import com.rymcu.mortise.system.infra.persistence.entity.UserPO;
import com.rymcu.mortise.system.mapper.UserMapper;
import com.rymcu.mortise.system.mapper.UserRoleMapper;
import com.rymcu.mortise.system.model.UserInfo;
import com.rymcu.mortise.system.model.UserProfileInfo;
import com.rymcu.mortise.system.model.UserSearch;
import com.rymcu.mortise.system.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static com.mybatisflex.core.query.QueryMethods.max;
import static com.rymcu.mortise.system.infra.persistence.entity.table.UserPOTableDef.USER_PO;

/**
 * MyBatis-Flex 用户仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository, UserQueryService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public boolean updateLastOnlineTimeByAccount(String account, LocalDateTime lastOnlineTime) {
        return UpdateChain.of(UserPO.class)
                .set(UserPO::getLastOnlineTime, lastOnlineTime)
                .where(UserPO::getAccount).eq(account)
                .update();
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return userMapper.selectCountByQuery(QueryWrapper.create().where(USER_PO.NICKNAME.eq(nickname))) > 0;
    }

    @Override
    public String findMaxAccount() {
        QueryWrapper query = QueryWrapper.create().select(max(USER_PO.ACCOUNT)).from(USER_PO);
        return userMapper.selectObjectByQueryAs(query, String.class);
    }

    @Override
    public List<User> findByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return userMapper.selectListByQueryAs(QueryWrapper.create().where(USER_PO.ID.in(userIds)), User.class);
    }

    @Override
    public User findById(Long userId) {
        return userMapper.selectOneByQueryAs(QueryWrapper.create().where(USER_PO.ID.eq(userId)), User.class);
    }

    @Override
    public User findByAccount(String account) {
        return userMapper.selectOneByQueryAs(QueryWrapper.create().where(USER_PO.ACCOUNT.eq(account)), User.class);
    }

    @Override
    public User findByLoginIdentity(String identity) {
        return userMapper.selectOneByQueryAs(QueryWrapper.create()
                .where(USER_PO.ACCOUNT.eq(identity))
                .or(USER_PO.EMAIL.eq(identity))
                .or(USER_PO.PHONE.eq(identity)), User.class);
    }

    @Override
    public PageResult<UserInfo> findUsers(PageQuery pageQuery, UserSearch search) {
        LocalDateTime startDate = LocalDate.now().atTime(LocalTime.MIN);
        if (StringUtils.isNotBlank(search.getStartDate())) {
            startDate = LocalDate.parse(search.getStartDate()).atTime(LocalTime.MIN);
        }
        LocalDateTime endDate = LocalDate.now().atTime(LocalTime.MAX);
        if (StringUtils.isNotBlank(search.getEndDate())) {
            endDate = LocalDate.parse(search.getEndDate()).atTime(LocalTime.MAX);
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(USER_PO.ID, USER_PO.NICKNAME, USER_PO.ACCOUNT, USER_PO.STATUS, USER_PO.AVATAR, USER_PO.EMAIL, USER_PO.PHONE,
                        USER_PO.LAST_LOGIN_TIME, USER_PO.LAST_ONLINE_TIME, USER_PO.CREATED_TIME)
                .where(USER_PO.ACCOUNT.eq(search.getAccount(), StringUtils::isNotBlank))
                .and(USER_PO.EMAIL.eq(search.getEmail(), StringUtils::isNotBlank))
                .and(USER_PO.ACCOUNT.like(search.getQuery(), StringUtils::isNotBlank)
                        .or(USER_PO.NICKNAME.like(search.getQuery(), StringUtils::isNotBlank)))
                .and(USER_PO.CREATED_TIME.between(startDate, endDate, StringUtils.isNotBlank(search.getStartDate())))
                .from(USER_PO);
        Page<UserInfo> page = userMapper.paginateAs(FlexPageMapper.toFlexPage(pageQuery), queryWrapper, UserInfo.class);
        return FlexPageMapper.toPageResult(page);
    }

    @Override
    public UserInfo findUserInfoById(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(USER_PO.ID, USER_PO.NICKNAME, USER_PO.ACCOUNT, USER_PO.PHONE, USER_PO.STATUS, USER_PO.AVATAR, USER_PO.EMAIL,
                        USER_PO.LAST_LOGIN_TIME, USER_PO.LAST_ONLINE_TIME, USER_PO.CREATED_TIME)
                .from(USER_PO)
                .where(USER_PO.ID.eq(userId));
        return userMapper.selectObjectByQueryAs(queryWrapper, UserInfo.class);
    }

    @Override
    public UserProfileInfo getUserProfileInfo(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(USER_PO.ID, USER_PO.NICKNAME, USER_PO.AVATAR, USER_PO.EMAIL, USER_PO.ACCOUNT)
                .from(USER_PO)
                .where(USER_PO.ID.eq(userId));
        return userMapper.selectObjectByQueryAs(queryWrapper, UserProfileInfo.class);
    }

    @Override
    public List<Role> findRolesByIdUser(Long userId) {
        return userRoleMapper.findRolesByIdUser(userId);
    }

    @Override
    public boolean save(User user) {
        UserPO userPO = PersistenceObjectMapper.copy(user, UserPO::new);
        boolean saved = userMapper.insertSelective(userPO) > 0;
        if (saved) {
            user.setId(userPO.getId());
        }
        return saved;
    }

    @Override
    public boolean update(User user) {
        return userMapper.update(PersistenceObjectMapper.copy(user, UserPO::new)) > 0;
    }

    @Override
    public boolean updateStatus(Long userId, Integer status) {
        return UpdateChain.of(UserPO.class)
                .set(UserPO::getStatus, status)
                .where(UserPO::getId).eq(userId)
                .update();
    }

    @Override
    public boolean updatePasswordById(Long userId, String encodedPassword) {
        return UpdateChain.of(UserPO.class)
                .set(UserPO::getPassword, encodedPassword)
                .where(UserPO::getId).eq(userId)
                .update();
    }

    @Override
    public boolean updatePasswordByEmail(String email, String encodedPassword) {
        return UpdateChain.of(UserPO.class)
                .set(UserPO::getPassword, encodedPassword)
                .where(UserPO::getEmail).eq(email)
                .update();
    }

    @Override
    public boolean updateProfile(Long userId, String nickname, String avatar) {
        UpdateChain<UserPO> chain = UpdateChain.of(UserPO.class);
        if (StringUtils.isNotBlank(nickname)) {
            chain.set(UserPO::getNickname, nickname);
        }
        if (StringUtils.isNotBlank(avatar)) {
            chain.set(UserPO::getAvatar, avatar);
        }
        return chain.where(UserPO::getId).eq(userId).update();
    }

    @Override
    public boolean updateEmail(Long userId, String email) {
        return UpdateChain.of(UserPO.class)
                .set(UserPO::getEmail, email)
                .where(UserPO::getId).eq(userId)
                .update();
    }

    @Override
    public boolean updateLastLoginTimeByAccount(String account, LocalDateTime lastLoginTime, LocalDateTime lastOnlineTime) {
        return UpdateChain.of(UserPO.class)
                .set(UserPO::getLastLoginTime, lastLoginTime)
                .set(UserPO::getLastOnlineTime, lastOnlineTime)
                .where(UserPO::getAccount).eq(account)
                .update();
    }

    @Override
    public boolean deleteById(Long userId) {
        return userMapper.deleteById(userId) > 0;
    }

    @Override
    public boolean deleteByIds(List<Long> userIds) {
        return userMapper.deleteBatchByIds(userIds) > 0;
    }

    @Override
    public long count() {
        return userMapper.selectCountByQuery(QueryWrapper.create());
    }

    @Override
    public long countEnabled() {
        return userMapper.selectCountByQuery(QueryWrapper.create().where(USER_PO.STATUS.eq(Status.ENABLED.getCode())));
    }
}
