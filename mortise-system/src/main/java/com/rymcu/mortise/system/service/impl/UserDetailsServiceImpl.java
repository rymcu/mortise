package com.rymcu.mortise.system.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.core.result.ResultCode;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.mapper.UserMapper;
import com.rymcu.mortise.system.model.auth.UserDetailInfo;
import com.rymcu.mortise.system.service.PermissionService;
import jakarta.annotation.Resource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.rymcu.mortise.system.entity.table.UserTableDef.USER;

/**
 * Created on 2025/2/24 21:39.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service.impl
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private PermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOneByQuery(QueryWrapper.create()
                .select(USER.ID, USER.ACCOUNT, USER.PASSWORD, USER.EMAIL, USER.STATUS)
                .where(USER.ACCOUNT.eq(username))
                .or(USER.EMAIL.eq(username))
                .or(USER.PHONE.eq(username)));
        if (Objects.nonNull(user)) {
            // 使用 PermissionService 获取用户权限，避免循环依赖
            Set<String> permissions = permissionService.findUserPermissionsByIdUser(user.getId());
            Set<GrantedAuthority> authorities = permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
            // 使用新的构造方法，传入完整的 User 对象
            return new UserDetailInfo(user, authorities);
        }
        throw new UsernameNotFoundException(ResultCode.UNKNOWN_ACCOUNT.getMessage());
    }

}
