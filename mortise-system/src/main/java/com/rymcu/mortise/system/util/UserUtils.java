package com.rymcu.mortise.system.util;

import com.rymcu.mortise.auth.constant.JwtConstants;
import com.rymcu.mortise.auth.util.JwtTokenUtil;
import com.rymcu.mortise.common.util.BeanCopierUtil;
import com.rymcu.mortise.common.util.ContextHolderUtils;
import com.rymcu.mortise.common.util.SpringContextHolder;
import com.rymcu.mortise.core.result.ResultCode;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.model.auth.TokenUser;
import com.rymcu.mortise.system.service.UserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Objects;

/**
 * Created on 2024/4/13 15:28.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.util
 */
public class UserUtils {

    private static final UserService userService = SpringContextHolder.getBean(UserService.class);

    private static final JwtTokenUtil jwtTokenUtil = SpringContextHolder.getBean(JwtTokenUtil.class);
    /**
     * 通过token获取当前用户的信息
     *
     * @return User
     */
    public static User getCurrentUserByToken() {
        String authHeader = ContextHolderUtils.getRequest().getHeader(JwtConstants.AUTHORIZATION);
        String username = jwtTokenUtil.getUsernameFromToken(authHeader);
        User user = userService.findByAccount(username);
        if (Objects.nonNull(user)) {
            return user;
        }
        throw new UsernameNotFoundException(ResultCode.UNKNOWN_ACCOUNT.getMessage());
    }

    public static TokenUser getTokenUser(String token) {
        String username = jwtTokenUtil.getUsernameFromToken(token);
        User user = userService.findByAccount(username);
        if (Objects.nonNull(user)) {
            TokenUser tokenUser = new TokenUser();
            BeanCopierUtil.copy(user, tokenUser);
            tokenUser.setAccount(user.getEmail());
            tokenUser.setToken(token);
            return tokenUser;
        }
        throw new UsernameNotFoundException(ResultCode.UNKNOWN_ACCOUNT.getMessage());
    }
}

