package com.rymcu.mortise.system.query;

import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.entity.Menu;
import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.model.RoleSearch;

import java.util.List;

public interface RoleQueryService {

    List<Role> findRolesByIdUser(Long userId);

    PageResult<Role> findRoles(PageQuery pageQuery, RoleSearch search);

    Role findById(Long roleId);

    List<Menu> findMenusByIdRole(Long roleId);

    List<Role> findDefaultRole();

    List<User> findUsersByIdRole(Long roleId);

    long count();

    long countEnabled();
}
