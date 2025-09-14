package com.rymcu.mortise.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.core.exception.ServiceException;
import com.rymcu.mortise.entity.Role;
import com.rymcu.mortise.mapper.RoleMapper;
import com.rymcu.mortise.model.BindRoleMenuInfo;
import com.rymcu.mortise.model.RoleSearch;
import com.rymcu.mortise.service.RoleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created on 2024/4/13 22:06.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service.impl
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public List<Role> findRolesByIdUser(Long idUser) {
        return mapper.selectRolesByIdUser(idUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRole(Role role) {
        boolean isUpdate = role.getId() != null;
        if (isUpdate) {
            Role oldRole = mapper.selectOneById(role.getId());
            if (oldRole == null) {
                throw new ServiceException("数据不存在");
            }
            oldRole.setLabel(role.getLabel());
            oldRole.setPermission(role.getPermission());
            oldRole.setStatus(role.getStatus());
            oldRole.setUpdatedTime(LocalDateTime.now());
            return mapper.update(oldRole) > 0;
        }
        return mapper.insertSelective(role) > 0;
    }

    @Override
    public Page<Role> findRoles(Page<Role> page, RoleSearch search) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select("id", "label", "permission", "status")
                .eq("label", search.getQuery(), StringUtils.isNotBlank(search.getQuery()));
        return mapper.paginate(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean bindRoleMenu(BindRoleMenuInfo bindRoleMenuInfo) {
        int num = 0;
        for (Long idMenu : bindRoleMenuInfo.getIdMenus()) {
            num += mapper.insertRoleMenu(bindRoleMenuInfo.getIdRole(), idMenu);
        }
        return num == bindRoleMenuInfo.getIdMenus().size();
    }

    @Override
    public Boolean updateStatus(Long idRole, Integer status) {
        Role role = UpdateEntity.of(Role.class, idRole);
        role.setStatus(status);
        return mapper.update(role) > 0;
    }

    @Override
    public Set<Long> findRoleMenus(Long idRole) {
        return mapper.selectRoleMenus(idRole);
    }

    @Override
    public Boolean updateDelFlag(Long idRole, Integer delFlag) {
        return mapper.deleteById(idRole) > 0;
    }

    @Override
    public Role findById(Long idRole) {
        return mapper.selectOneById(idRole);
    }
}
