package com.rymcu.mortise.system.repository;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.rymcu.mortise.common.enumerate.DefaultFlag;
import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.entity.Menu;
import com.rymcu.mortise.system.entity.Role;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.infra.persistence.FlexPageMapper;
import com.rymcu.mortise.system.infra.persistence.PersistenceObjectMapper;
import com.rymcu.mortise.system.infra.persistence.entity.RolePO;
import com.rymcu.mortise.system.mapper.RoleMenuMapper;
import com.rymcu.mortise.system.mapper.RoleMapper;
import com.rymcu.mortise.system.mapper.UserRoleMapper;
import com.rymcu.mortise.system.model.RoleSearch;
import com.rymcu.mortise.system.query.RoleQueryService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.rymcu.mortise.system.infra.persistence.entity.table.RolePOTableDef.ROLE_PO;
import static com.rymcu.mortise.system.infra.persistence.entity.table.UserRolePOTableDef.USER_ROLE_PO;

/**
 * MyBatis-Flex 角色仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository, RoleQueryService {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public List<Role> findRolesByUserId(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(ROLE_PO.ID, ROLE_PO.LABEL, ROLE_PO.PERMISSION)
                .from(ROLE_PO.as("tr"))
                .join(USER_ROLE_PO.as("tur")).on(USER_ROLE_PO.ID_MORTISE_ROLE.eq(ROLE_PO.ID))
                .where(USER_ROLE_PO.ID_MORTISE_USER.eq(userId));
        return roleMapper.selectListByQueryAs(queryWrapper, Role.class);
    }

    @Override
    public List<Role> findRolesByIdUser(Long userId) {
        return findRolesByUserId(userId);
    }

    @Override
    public PageResult<Role> findRoles(PageQuery pageQuery, RoleSearch search) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(ROLE_PO.ID, ROLE_PO.LABEL, ROLE_PO.PERMISSION, ROLE_PO.STATUS, ROLE_PO.IS_DEFAULT, ROLE_PO.CREATED_TIME)
                .and(ROLE_PO.LABEL.eq(search.getQuery(), StringUtils.isNotBlank(search.getQuery())));
        Page<Role> page = roleMapper.paginateAs(FlexPageMapper.toFlexPage(pageQuery), queryWrapper, Role.class);
        return FlexPageMapper.toPageResult(page);
    }

    @Override
    public List<Role> findDefaultRoles() {
        return roleMapper.selectListByQueryAs(QueryWrapper.create()
                .select(ROLE_PO.ID, ROLE_PO.LABEL, ROLE_PO.PERMISSION)
                .where(ROLE_PO.IS_DEFAULT.eq(DefaultFlag.YES.getValue())), Role.class);
    }

    @Override
    public Role findById(Long roleId) {
        return roleMapper.selectOneByQueryAs(QueryWrapper.create().where(ROLE_PO.ID.eq(roleId)), Role.class);
    }

    @Override
    public List<Menu> findMenusByIdRole(Long roleId) {
        return roleMenuMapper.findMenusByIdRole(roleId);
    }

    @Override
    public List<Role> findDefaultRole() {
        return findDefaultRoles();
    }

    @Override
    public List<User> findUsersByIdRole(Long roleId) {
        return userRoleMapper.findUsersByIdRole(roleId);
    }

    @Override
    public Role findByPermission(String permission) {
        return roleMapper.selectOneByQueryAs(
                QueryWrapper.create().where(ROLE_PO.PERMISSION.eq(permission)),
                Role.class
        );
    }

    @Override
    public boolean save(Role role) {
        RolePO rolePO = PersistenceObjectMapper.copy(role, RolePO::new);
        boolean saved = roleMapper.insertSelective(rolePO) > 0;
        if (saved) {
            role.setId(rolePO.getId());
        }
        return saved;
    }

    @Override
    public boolean saveAll(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return true;
        }
        return roleMapper.insertBatchSelective(PersistenceObjectMapper.copyList(roles, RolePO::new)) == roles.size();
    }

    @Override
    public boolean update(Role role) {
        return roleMapper.update(PersistenceObjectMapper.copy(role, RolePO::new)) > 0;
    }

    @Override
    public boolean updateStatus(Long roleId, Integer status) {
        return UpdateChain.of(RolePO.class)
                .set(RolePO::getStatus, status)
                .where(RolePO::getId).eq(roleId)
                .update();
    }

    @Override
    public boolean deleteById(Long roleId) {
        return roleMapper.deleteById(roleId) > 0;
    }

    @Override
    public boolean deleteByIds(List<Long> roleIds) {
        return roleMapper.deleteBatchByIds(roleIds) > 0;
    }

    @Override
    public long count() {
        return roleMapper.selectCountByQuery(QueryWrapper.create());
    }

    @Override
    public long countEnabled() {
        return roleMapper.selectCountByQuery(QueryWrapper.create().where(ROLE_PO.STATUS.eq(Status.ENABLED.getCode())));
    }
}
