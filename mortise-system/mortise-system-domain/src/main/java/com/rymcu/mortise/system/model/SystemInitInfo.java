package com.rymcu.mortise.system.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "管理员密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度必须在8-32个字符之间")
    private String adminPassword;

    /**
     * 管理员昵称
     */
    @NotBlank(message = "管理员昵称不能为空")
    @Size(max = 30, message = "昵称最多30个字符")
    private String adminNickname;

    /**
     * 管理员邮箱
     */
    @NotBlank(message = "管理员邮箱不能为空")
    @Email(message = "请输入有效的邮箱地址")
    private String adminEmail;

    /**
     * 系统名称
     */
    @Size(max = 50, message = "系统名称最多50个字符")
    private String systemName;

    /**
     * 系统描述
     */
    @Size(max = 200, message = "系统描述最多200个字符")
    private String systemDescription;
}
