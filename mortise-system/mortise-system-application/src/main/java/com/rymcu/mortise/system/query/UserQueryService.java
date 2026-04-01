package com.rymcu.mortise.system.query;

import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.model.UserInfo;
import com.rymcu.mortise.system.model.UserProfileInfo;
import com.rymcu.mortise.system.model.UserSearch;

import java.util.List;

public interface UserQueryService {

    PageResult<UserInfo> findUsers(PageQuery pageQuery, UserSearch search);

    User findByLoginIdentity(String identity);

    User findById(Long userId);

    UserInfo findUserInfoById(Long userId);

    UserProfileInfo getUserProfileInfo(Long userId);

    List<Role> findRolesByIdUser(Long userId);

    long count();

    long countEnabled();
}
