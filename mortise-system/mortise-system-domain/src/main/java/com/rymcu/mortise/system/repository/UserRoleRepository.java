package com.rymcu.mortise.system.repository;

import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.system.entity.User;

import java.util.List;

/**
 * 用户角色仓储端口。
 */
public interface UserRoleRepository {

    List<Role> findRolesByUserId(Long userId);

    List<User> findUsersByRoleId(Long roleId);

    boolean replaceRoles(Long userId, List<Long> roleIds);

    boolean replaceUsers(Long roleId, List<Long> userIds);
}
