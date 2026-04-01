package com.rymcu.mortise.system.controller.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 重置密码响应。
 */
@Data
@AllArgsConstructor
public class PasswordResetVO {

    private String password;
}
