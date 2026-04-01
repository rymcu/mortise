package com.rymcu.mortise.system.controller.vo;

import com.rymcu.mortise.system.model.AuthInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 当前用户会话响应。
 */
@Data
@AllArgsConstructor
public class UserSessionVO {

    private AuthInfo user;
}
