package com.rymcu.mortise.system.service.impl;

import com.rymcu.mortise.common.exception.BusinessException;
import com.rymcu.mortise.common.model.Avatar;
import com.rymcu.mortise.common.util.Utils;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.core.result.ResultCode;
import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.handler.event.RegisterEvent;
import com.rymcu.mortise.system.handler.event.ResetPasswordEvent;
import com.rymcu.mortise.system.model.BindUserRoleInfo;
import com.rymcu.mortise.system.model.UserInfo;
import com.rymcu.mortise.system.model.UserProfileInfo;
import com.rymcu.mortise.system.model.UserSearch;
import com.rymcu.mortise.system.query.UserQueryService;
import com.rymcu.mortise.system.repository.UserRepository;
import com.rymcu.mortise.system.repository.UserRoleRepository;
import com.rymcu.mortise.system.service.PermissionService;
import com.rymcu.mortise.system.service.SystemCacheService;
import com.rymcu.mortise.system.service.SystemNotificationService;
import com.rymcu.mortise.system.service.UserService;
import com.rymcu.mortise.system.service.command.UserCommandService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created on 2024/4/13 21:25.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service.impl
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService, UserCommandService {

    @Resource
    private PermissionService permissionService;
    @Resource
    private SystemCacheService systemCacheService;
    @Resource
    private SystemNotificationService systemNotificationService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;
    @Resource
    private UserRepository userRepository;
    @Resource
    private UserRoleRepository userRoleRepository;
    @Resource
    private UserQueryService userQueryService;

    private static final String DEFAULT_AVATAR = "https://static.rymcu.com/article/1578475481946.png";
    private static final String DEFAULT_ACCOUNT = "1411780000";

    @Override
    public boolean updateLastOnlineTimeByAccount(String account) {
        return userRepository.updateLastOnlineTimeByAccount(account, LocalDateTime.now());
    }

    @Override
    public String checkNickname(String nickname) {
        nickname = formatNickname(nickname);
        if (userRepository.existsByNickname(nickname)) {
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
        String currentAccount = systemCacheService.getCurrentAccount();
        BigDecimal account;
        if (StringUtils.isNotBlank(currentAccount)) {
            account = BigDecimal.valueOf(Long.parseLong(currentAccount));
        } else {
            currentAccount = userRepository.findMaxAccount();
            if (StringUtils.isNotBlank(currentAccount)) {
                account = BigDecimal.valueOf(Long.parseLong(currentAccount));
            } else {
                account = BigDecimal.valueOf(Long.parseLong(DEFAULT_ACCOUNT));
            }
        }
        currentAccount = account.add(BigDecimal.ONE).toString();
        systemCacheService.storeCurrentAccount(currentAccount);
        return currentAccount;
    }

    @Override
    public List<Role> findRolesByIdUser(Long idUser) {
        return userQueryService.findRolesByIdUser(idUser);
    }

    @Override
    public Boolean bindRoleUser(BindUserRoleInfo bindUserRoleInfo) {
        return userRoleRepository.replaceRoles(
                bindUserRoleInfo.getIdUser(),
                bindUserRoleInfo.getIdRoles() == null ? null : new ArrayList<>(bindUserRoleInfo.getIdRoles())
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserInfo userInfo) {
        User user = new User();
        user.setEmail(userInfo.getEmail());
        user.setPhone(userInfo.getPhone());
        user.setNickname(checkNickname(userInfo.getNickname()));
        String code = userInfo.getPassword();
        if (StringUtils.isBlank(code)) {
            code = Utils.genKey();
        }
        user.setPassword(passwordEncoder.encode(code));
        user.setAvatar(Objects.isNull(userInfo.getAvatar()) ? DEFAULT_AVATAR : userInfo.getAvatar().getSrc());
        user.setAccount(nextAccount());
        boolean result = userRepository.save(user);
        if (result) {
            systemCacheService.cacheUserCount(userRepository.count());
            applicationEventPublisher.publishEvent(new RegisterEvent(user.getId(), user.getEmail(), code));
        }
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUser(UserInfo userInfo) {
        User user = userRepository.findById(userInfo.getId());
        if (Objects.nonNull(user)) {
            user.setEmail(userInfo.getEmail());
            user.setPhone(userInfo.getPhone());
            user.setNickname(checkNickname(userInfo.getNickname()));
            user.setStatus(userInfo.getStatus());
            user.setAvatar(userInfo.getAvatar().getSrc());
            return userRepository.update(user);
        }
        throw new BusinessException(ResultCode.UNKNOWN_ACCOUNT.getMessage());
    }

    @Override
    public Boolean update(User user) {
        return userRepository.update(user);
    }

    @Override
    public Set<String> findUserPermissionsByIdUser(Long idUser) {
        return permissionService.findUserPermissionsByIdUser(idUser);
    }

    @Override
    public Set<String> findUserRoleListByIdUser(Long idUser) {
        return permissionService.findUserRolePermissionsByIdUser(idUser);
    }

    @Override
    public User findByAccount(String account) {
        if (StringUtils.isBlank(account)) {
            throw new UsernameNotFoundException(ResultCode.UNKNOWN_ACCOUNT.getMessage());
        }
        return userRepository.findByLoginIdentity(account);
    }

    @Override
    public User findById(Long idUser) {
        return userRepository.findById(idUser);
    }

    @Override
    public PageResult<UserInfo> findUsers(PageQuery pageQuery, UserSearch search) {
        PageResult<UserInfo> results = userQueryService.findUsers(pageQuery, search);
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
        return userQueryService.findUserInfoById(idUser);
    }

    @Override
    public Boolean bindUserRole(BindUserRoleInfo bindUserRoleInfo) {
        return userRoleRepository.replaceRoles(
                bindUserRoleInfo.getIdUser(),
                bindUserRoleInfo.getIdRoles() == null ? null : new ArrayList<>(bindUserRoleInfo.getIdRoles())
        );
    }

    @Override
    public Boolean updateStatus(Long idUser, Integer status) {
        return userRepository.updateStatus(idUser, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String resetPassword(Long idUser) {
        User user = userRepository.findById(idUser);
        if (Objects.nonNull(user)) {
            String code = String.valueOf(Utils.genCode());
            String password = passwordEncoder.encode(code);
            if (userRepository.updatePasswordById(idUser, password)) {
                applicationEventPublisher.publishEvent(new ResetPasswordEvent(user.getEmail(), code));
                return code;
            }
            throw new BusinessException(ResultCode.BAD_REQUEST.getMessage());
        }
        throw new UsernameNotFoundException(ResultCode.UNKNOWN_ACCOUNT.getMessage());
    }

    @Override
    public Boolean deleteUser(Long idUser) {
        boolean result = userRepository.deleteById(idUser);
        if (result) {
            systemCacheService.cacheUserCount(userRepository.count());
        }
        return result;
    }

    @Override
    public Boolean batchDeleteUsers(List<Long> idUserList) {
        if (idUserList == null || idUserList.isEmpty()) {
            return false;
        }
        boolean result = userRepository.deleteByIds(idUserList);
        if (result) {
            systemCacheService.cacheUserCount(userRepository.count());
        }
        return result;
    }

    @Override
    public Boolean updateUserProfileInfo(UserProfileInfo userProfileInfo, Long userId) {
        User current = userRepository.findById(userId);
        String nickname = null;
        if (current == null || !current.getNickname().equals(userProfileInfo.getNickname())) {
            nickname = checkNickname(userProfileInfo.getNickname());
        }
        return userRepository.updateProfile(userId, nickname, userProfileInfo.getAvatar());
    }

    @Override
    public UserProfileInfo getUserProfileInfo(Long userId) {
        UserProfileInfo profileInfo = userQueryService.getUserProfileInfo(userId);
        if (profileInfo == null) {
            throw new UsernameNotFoundException(ResultCode.UNKNOWN_ACCOUNT.getMessage());
        }
        return profileInfo;
    }

    @Override
    public void sendEmailUpdateCode(Long userId, String newEmail) {
        User existing = findByAccount(newEmail);
        if (existing != null) {
            throw new BusinessException(ResultCode.EMAIL_EXISTS.getMessage());
        }
        String code = String.valueOf(Utils.genCode());
        systemCacheService.storeEmailUpdateCode(userId, newEmail, code);
        boolean sent = systemNotificationService.sendVerificationCodeEmail(newEmail, code);
        if (!sent) {
            systemCacheService.removeEmailUpdateCode(userId, newEmail);
            throw new BusinessException(ResultCode.SEND_EMAIL_FAIL.getMessage());
        }
        log.info("邮箱更换验证码已发送: userId={}, newEmail={}", userId, newEmail);
    }

    @Override
    public Boolean confirmEmailUpdate(Long userId, String newEmail, String code) {
        String cachedCode = systemCacheService.getEmailUpdateCode(userId, newEmail);
        if (cachedCode == null || !cachedCode.equals(code)) {
            throw new BusinessException(ResultCode.INVALID_VERIFICATION_CODE.getMessage());
        }
        boolean updated = userRepository.updateEmail(userId, newEmail);
        if (updated) {
            systemCacheService.removeEmailUpdateCode(userId, newEmail);
            log.info("邮箱更换成功: userId={}, newEmail={}", userId, newEmail);
        }
        return updated;
    }

    @Override
    public void updateLastLoginTimeByAccount(String account) {
        LocalDateTime now = LocalDateTime.now();
        userRepository.updateLastLoginTimeByAccount(account, now, now);
    }

    @Override
    public long count() {
        return userQueryService.count();
    }

    @Override
    public long countEnabled() {
        return userQueryService.countEnabled();
    }
}
