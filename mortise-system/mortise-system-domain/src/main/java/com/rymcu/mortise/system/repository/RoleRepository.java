package com.rymcu.mortise.system.repository;

import com.rymcu.mortise.system.entity.Role;

import java.util.List;

/**
 * 角色仓储端口。
 */
public interface RoleRepository {

    List<Role> findRolesByUserId(Long userId);

    List<Role> findDefaultRoles();

    Role findById(Long roleId);

    Role findByPermission(String permission);

    boolean save(Role role);

    boolean saveAll(List<Role> roles);

    boolean update(Role role);

    boolean updateStatus(Long roleId, Integer status);

    boolean deleteById(Long roleId);

    boolean deleteByIds(List<Long> roleIds);

    long count();

    long countEnabled();
}
