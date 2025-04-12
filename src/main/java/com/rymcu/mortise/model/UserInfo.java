package com.rymcu.mortise.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rymcu.mortise.annotation.DictFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created on 2024/4/19 9:15.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.model
 */
@Data
public class UserInfo {

    private Long id;

    private String nickname;

    private String account;

    private Avatar avatar;

    private String picture;

    @DictFormat(value = "Status")
    private Integer status;

    private String email;

    private String password;

    private String phone;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime lastLoginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime lastOnlineTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    private Integer onlineStatus;

    private String roleNames;

    private String openId;

    private String provider;
}
