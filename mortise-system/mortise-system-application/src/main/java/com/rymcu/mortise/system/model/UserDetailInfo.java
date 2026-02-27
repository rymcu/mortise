package com.rymcu.mortise.system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.system.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 用户详细信息
 * <p>
 * 实现了 Spring Security 的 UserDetails 接口和 mortise-core 的 CurrentUser 接口。
 * 用于在 Spring Security 认证流程中传递用户信息。
 * </p>
 *
 * @author ronger
 * @email ronger-x@outlook.com
 */
public class UserDetailInfo implements UserDetails, CurrentUser {

    @Getter
    private final Long id;

    private final String username;

    private final String password;

    private final Integer state;

    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * 存储完整的 User 对象，方便直接获取用户信息
     * -- GETTER --
     *  获取完整的 User 对象
     */
    @Getter
    private final User user;

    /**
     * 新增构造方法，支持传入完整的 User 对象
     */
    public UserDetailInfo(User user, Collection<? extends GrantedAuthority> authorities) {
        this.id = user.getId();
        this.username = user.getAccount();
        this.password = user.getPassword();
        this.state = user.getStatus();
        this.authorities = authorities;
        this.user = user;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return password;
    }

    @JsonIgnore
    public Integer getState() {
        return state;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    //账户是否未过期
    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    //账户是否未被锁
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }


    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }


    //是否启用
    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }

    // ==================== CurrentUser 接口实现 ====================

    /**
     * 获取用户ID
     * <p>实现 CurrentUser 接口方法</p>
     *
     * @return 用户ID
     */
    @Override
    public Long getUserId() {
        return user != null ? user.getId() : id;
    }

    /**
     * 获取用户昵称
     * <p>实现 CurrentUser 接口方法</p>
     *
     * @return 用户昵称
     */
    @Override
    public String getNickname() {
        return user != null ? user.getNickname() : null;
    }

    /**
     * 判断用户是否已认证
     * <p>实现 CurrentUser 接口方法</p>
     *
     * @return true 表示已认证
     */
    @Override
    public boolean isAuthenticated() {
        return user != null && id != null;
    }

    /**
     * 获取用户邮箱
     * <p>实现 CurrentUser 接口方法</p>
     *
     * @return 用户邮箱
     */
    @Override
    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }

    /**
     * 获取用户头像URL
     * <p>实现 CurrentUser 接口方法</p>
     *
     * @return 用户头像URL
     */
    @Override
    public String getAvatarUrl() {
        return user != null ? user.getAvatar() : null;
    }
}
