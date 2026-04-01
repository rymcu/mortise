package com.rymcu.mortise.system.service.command;

import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.model.BindUserRoleInfo;
import com.rymcu.mortise.system.model.UserInfo;
import com.rymcu.mortise.system.model.UserProfileInfo;

import java.util.List;

public interface UserCommandService {

    boolean updateLastOnlineTimeByAccount(String account);

    String checkNickname(String nickname);

    String nextAccount();

    Boolean bindUserRole(BindUserRoleInfo bindUserRoleInfo);

    Boolean updateStatus(Long userId, Integer status);

    String resetPassword(Long userId);

    Boolean deleteUser(Long userId);

    Boolean batchDeleteUsers(List<Long> userIds);

    Boolean bindRoleUser(BindUserRoleInfo bindUserRoleInfo);

    Long createUser(UserInfo userInfo);

    Boolean updateUser(UserInfo userInfo);

    Boolean update(User user);

    Boolean updateUserProfileInfo(UserProfileInfo userProfileInfo, Long userId);

    void sendEmailUpdateCode(Long userId, String newEmail);

    Boolean confirmEmailUpdate(Long userId, String newEmail, String code);

    void updateLastLoginTimeByAccount(String account);
}
