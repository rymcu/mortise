package com.rymcu.mortise.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created on 2024/4/13 15:01.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.entity
 */
@Data
@TableName(value = "mortise_user", schema = "mortise")
public class User implements Serializable {

    @TableId(value = "id")
    @TableField(value = "id")
    private Long idUser;
    /**
     * 登录账号
     */
    private String account;

    /**
     * 密码
     */
    @JsonIgnore
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 头像路径
     */
    private String avatar;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 状态
     */
    private Integer status;
    /**
     * 删除标记
     */
    private Integer delFlag;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 最后在线时间
     */
    private LocalDateTime lastOnlineTime;

    private String openId;

    private String provider;
}
