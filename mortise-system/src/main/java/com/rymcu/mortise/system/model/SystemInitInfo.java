package com.rymcu.mortise.system.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统初始化信息
 * 
 * @author ronger
 * @since 2025-10-02
 */
@Data
@Schema(description = "系统初始化信息")
public class SystemInitInfo implements Serializable {

    /**
     * 管理员账号
     */
    @Schema(description = "管理员账号", example = "admin", required = true, minLength = 4, maxLength = 32)
    private String adminAccount;

    /**
     * 管理员密码
     */
    @Schema(description = "管理员密码（建议至少8位，包含字母、数字和特殊字符）", example = "Admin@123456", required = true, minLength = 8, maxLength = 128)
    private String adminPassword;

    /**
     * 管理员昵称
     */
    @Schema(description = "管理员昵称", example = "系统管理员", required = true, minLength = 2, maxLength = 32)
    private String adminNickname;

    /**
     * 管理员邮箱
     */
    @Schema(description = "管理员邮箱", example = "admin@example.com", required = true, format = "email")
    private String adminEmail;

    /**
     * 系统名称
     */
    @Schema(description = "系统名称", example = "Mortise系统", required = true, minLength = 2, maxLength = 64)
    private String systemName;

    /**
     * 系统描述
     */
    @Schema(description = "系统描述", example = "企业级管理系统", maxLength = 255)
    private String systemDescription;
}
