package com.rymcu.mortise.system.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rymcu.mortise.common.model.Avatar;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端用户视图对象。
 */
@Data
public class UserVO {

    private Long id;
    private String account;
    private String nickname;
    private String realName;
    private Avatar avatar;
    private String picture;
    private String email;
    private String phone;
    private Integer status;
    private Integer delFlag;
    private Integer onlineStatus;
    private String roleNames;
    private String openId;
    private String provider;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime lastLoginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOnlineTime;
}
