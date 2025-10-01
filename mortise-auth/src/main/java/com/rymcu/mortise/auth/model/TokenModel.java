package com.rymcu.mortise.auth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * JWT Token 数据模型
 * Token的Model类，可以增加字段提高安全性，例如时间戳、url签名
 *
 * @author ScienJus
 * @author ronger
 * @since 2015/7/31
 */
@Setter
@Getter
@AllArgsConstructor
public class TokenModel {

    private String username;

    private String token;
}
