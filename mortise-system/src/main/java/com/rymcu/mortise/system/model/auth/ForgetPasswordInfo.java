package com.rymcu.mortise.system.model.auth;

import lombok.Data;

/**
 * Created on 2024/4/24 10:24.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.model
 */
@Data
public class ForgetPasswordInfo {

    private String password;

    private String code;

}
