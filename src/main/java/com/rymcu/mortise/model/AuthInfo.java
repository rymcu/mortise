package com.rymcu.mortise.model;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * Created on 2024/4/17 10:22.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.model
 */
@Data
public class AuthInfo {

    private String account;

    private String nickname;

    private String avatar;

    private Set<String> permissions;

    private Set<String> scope;

    private Set<String> role;

    private List<Link> links;
}
