package com.rymcu.mortise.service.impl;

import com.rymcu.mortise.core.result.ResultCode;
import com.rymcu.mortise.entity.User;
import com.rymcu.mortise.model.UserDetailInfo;
import com.rymcu.mortise.service.UserService;
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
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByAccount(username);
        if (Objects.nonNull(user)) {
            Set<String> roles = userService.findUserRoleListByIdUser(user.getId());
            Set<GrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
            return new UserDetailInfo(user.getAccount(), user.getPassword(), user.getStatus(), authorities);
        }
        throw new UsernameNotFoundException(ResultCode.UNKNOWN_ACCOUNT.getMessage());
    }

}
