package com.rymcu.mortise.system.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 系统初始化信息
 *
 * @author ronger
 * @since 2025-10-02
 */
@Data
public class SystemInitInfo implements Serializable {

    /**
     * 管理员密码
     */
    private String adminPassword;

    /**
     * 管理员昵称
     */
    private String adminNickname;

    /**
     * 管理员邮箱
     */
    private String adminEmail;

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 系统描述
     */
    private String systemDescription;
}
