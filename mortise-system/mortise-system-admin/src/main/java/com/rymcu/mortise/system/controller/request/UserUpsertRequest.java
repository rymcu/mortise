package com.rymcu.mortise.system.controller.request;

import lombok.Data;

/**
 * 用户新增/更新请求。
 */
@Data
public class UserUpsertRequest {

    private String nickname;
    private String email;
    private String password;
    private String phone;
    private String avatar;
    private Integer status;
}
