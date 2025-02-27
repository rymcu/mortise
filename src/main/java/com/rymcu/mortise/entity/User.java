package com.rymcu.mortise.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

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

    @TableId
    @TableField(value = "id")
    private Long idUser;
    /**
     * 登录账号
     */
    private String account;

    /**
     * 密码
     */
    @JSONField(serialize = false)
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
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;

    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;

    /**
     * 最后在线时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date lastOnlineTime;

    private String openId;

    private String provider;
}
