package com.rymcu.mortise.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    private Long idUser;

    private String nickname;

    private String account;

    private Avatar avatar;

    private String avatarUrl;

    private Integer status;

    private String email;

    private String password;

    private String phone;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime lastLoginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime lastOnlineTime;

    private LocalDateTime createdTime;

    private Integer onlineStatus;

    private String roleNames;

    private String openId;

    private String provider;
}
