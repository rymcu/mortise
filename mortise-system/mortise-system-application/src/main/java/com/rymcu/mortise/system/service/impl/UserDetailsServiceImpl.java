package com.rymcu.mortise.system.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.auth.enumerate.UserType;
import com.rymcu.mortise.auth.service.CustomUserDetailsService;
import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.core.result.ResultCode;
import com.rymcu.mortise.system.entity.User;
import com.rymcu.mortise.system.mapper.UserMapper;
import com.rymcu.mortise.system.model.UserDetailInfo;
import com.rymcu.mortise.system.service.PermissionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.rymcu.mortise.system.entity.table.UserTableDef.USER;

/**
 * Spring Security 系统用户详情服务实现
 * <p>
 * 负责从数据库加载系统用户信息并转换为 Spring Security 所需的 UserDetails 对象
 * </p>
 * <p>
 * 支持多用户表登录场景，通过 supports 方法指定只处理系统用户（"system"）类型
 * </p>
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2025/2/24
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements CustomUserDetailsService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private PermissionService permissionService;

    /**
     * 根据用户名加载用户信息
     * <p>
     * 支持通过账号、邮箱或手机号登录
     *
     * @param username 用户名（可以是账号、邮箱或手机号）
     * @return UserDetails 对象
     * @throws UsernameNotFoundException 用户不存在时抛出
     * @throws DisabledException 用户已被禁用时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 参数校验
        if (StringUtils.isBlank(username)) {
            log.warn("登录失败: 用户名为空");
            throw new UsernameNotFoundException("用户名不能为空");
        }

        log.debug("尝试加载用户信息: {}", username);

        // 2. 查询用户信息（支持账号、邮箱、手机号登录）
        User user = userMapper.selectOneByQuery(QueryWrapper.create()
                .select(USER.ID, USER.ACCOUNT, USER.PASSWORD, USER.EMAIL, USER.PHONE, USER.STATUS, USER.NICKNAME)
                .where(USER.ACCOUNT.eq(username))
                .or(USER.EMAIL.eq(username))
                .or(USER.PHONE.eq(username)));

        // 3. 用户存在性检查
        if (Objects.isNull(user)) {
            log.warn("登录失败: 用户不存在 - {}", username);
            throw new UsernameNotFoundException(ResultCode.UNKNOWN_ACCOUNT.getMessage());
        }

        log.debug("成功查询到用户: id={}, account={}", user.getId(), user.getAccount());

        // 4. 用户状态检查
        if (Objects.nonNull(user.getStatus()) && user.getStatus() == Status.DISABLED.ordinal()) {
            log.warn("登录失败: 用户已被禁用 - userId={}, account={}", user.getId(), user.getAccount());
            throw new DisabledException("账号已被禁用，请联系管理员");
        }

        // 5. 加载用户权限（包含菜单权限和角色权限）
        Set<String> permissions;
        try {
            permissions = permissionService.findUserPermissionsByIdUser(user.getId());
            log.debug("成功加载用户权限: userId={}, permissionCount={}", user.getId(), permissions.size());
        } catch (Exception e) {
            log.error("加载用户权限失败: userId={}", user.getId(), e);
            // 权限加载失败时给予基础权限
            permissions = Set.of("ROLE_USER");
        }

        // 6. 转换为 Spring Security 的 GrantedAuthority
        Set<GrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        log.debug("用户认证信息构建完成: userId={}, account={}, authorityCount={}",
                  user.getId(), user.getAccount(), authorities.size());

        // 7. 构建并返回 UserDetailInfo 对象
        return new UserDetailInfo(user, authorities);
    }

    /**
     * 判断是否支持指定的用户类型
     * <p>
     * 该实现仅支持系统用户（"system"）类型
     * </p>
     *
     * @param userType 用户类型标识
     * @return true 表示支持系统用户，false 表示不支持
     */
    @Override
    public Boolean supports(String userType) {
        boolean supported = UserType.SYSTEM.getCode().equals(userType);
        log.debug("用户类型检查: type={}, supported={}", userType, supported);
        return supported;
    }
}
