package com.rymcu.mortise.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;

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

    @JSONField(format = "yyyy-MM-dd HH:mm")
    private Date lastLoginTime;

    @JSONField(format = "yyyy-MM-dd HH:mm")
    private Date lastOnlineTime;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;

    private Integer onlineStatus;

    private String roleNames;
}
