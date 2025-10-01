package com.rymcu.mortise.system.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rymcu.mortise.system.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Created on 2025/2/24 21:35.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.model
 */
public class UserDetailInfo implements UserDetails {

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

}
