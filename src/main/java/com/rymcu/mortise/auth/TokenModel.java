package com.rymcu.mortise.auth;

import lombok.Getter;
import lombok.Setter;

/**
 * Token的Model类，可以增加字段提高安全性，例如时间戳、url签名
 *
 * @author ScienJus
 * @date 2015/7/31.
 */
@Setter
@Getter
public class TokenModel {

    private String username;

    private String token;

    public TokenModel(String username, String token) {
        this.username = username;
        this.token = token;
    }
}
