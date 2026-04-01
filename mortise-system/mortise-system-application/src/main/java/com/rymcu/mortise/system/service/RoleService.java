package com.rymcu.mortise.system.service;

import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.entity.Menu;
import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.model.BindRoleMenuInfo;
import com.rymcu.mortise.system.model.BindRoleUserInfo;
import com.rymcu.mortise.system.model.RoleSearch;

import java.util.List;

/**
 * Created on 2024/4/13 22:06.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service
 */
public interface RoleService {
    List<Role> findRolesByIdUser(Long idUser);

    PageResult<Role> findRoles(PageQuery pageQuery, RoleSearch search);

    Role findById(Long idRole);

    Boolean bindRoleMenu(BindRoleMenuInfo bindRoleMenuInfo);

    Boolean updateStatus(Long idRole, Integer status);

    List<Menu> findMenusByIdRole(Long idRole);

    Boolean deleteRole(Long idRole);

    Boolean batchDeleteRoles(List<Long> idRoleList);

    /**
     * 查找默认角色（用于新用户注册）
     * @return 默认角色
     */
    List<Role> findDefaultRole();

    Boolean bindRoleUser(BindRoleUserInfo bindRoleUserInfo);

    List<User> findUsersByIdRole(Long idRole);

    Long createRole(Role role);

    Boolean updateRole(Role role);

    long count();

    long countEnabled();
}
