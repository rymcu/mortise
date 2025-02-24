package com.rymcu.mortise.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Set;

/**
 * Created on 2025/2/24 21:35.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.model
 */
@Getter
@Setter
public class UserDetailInfo extends User {

    private String username;

    private String password;

    private Set<GrantedAuthority> authorities;

    public UserDetailInfo(String username, String password, Set<GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

}
