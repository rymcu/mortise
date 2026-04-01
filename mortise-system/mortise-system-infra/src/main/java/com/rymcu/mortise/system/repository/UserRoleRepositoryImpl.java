package com.rymcu.mortise.system.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.infra.persistence.entity.UserRolePO;
import com.rymcu.mortise.system.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.rymcu.mortise.system.infra.persistence.entity.table.UserRolePOTableDef.USER_ROLE_PO;

/**
 * MyBatis-Flex 用户角色仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class UserRoleRepositoryImpl implements UserRoleRepository {

    private final UserRoleMapper userRoleMapper;

    @Override
    public List<Role> findRolesByUserId(Long userId) {
        return userRoleMapper.findRolesByIdUser(userId);
    }

    @Override
    public List<User> findUsersByRoleId(Long roleId) {
        return userRoleMapper.findUsersByIdRole(roleId);
    }

    @Override
    public boolean replaceRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteByQuery(QueryWrapper.create().where(USER_ROLE_PO.ID_MORTISE_USER.eq(userId)));
        if (roleIds == null || roleIds.isEmpty()) {
            return true;
        }
        List<UserRolePO> userRoles = roleIds.stream()
                .map(roleId -> new UserRolePO(userId, roleId))
                .toList();
        int inserted = userRoleMapper.insertBatch(userRoles);
        return inserted == roleIds.size();
    }

    @Override
    public boolean replaceUsers(Long roleId, List<Long> userIds) {
        userRoleMapper.deleteByQuery(QueryWrapper.create().where(USER_ROLE_PO.ID_MORTISE_ROLE.eq(roleId)));
        if (userIds == null || userIds.isEmpty()) {
            return true;
        }
        List<UserRolePO> userRoles = userIds.stream()
                .map(userId -> new UserRolePO(userId, roleId))
                .toList();
        int inserted = userRoleMapper.insertBatch(userRoles);
        return inserted == userIds.size();
    }
}
