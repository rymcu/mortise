package com.rymcu.mortise.auth.enumerate;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created on 2025/11/11 13:57.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.auth.enumerate
 */
@Getter
@AllArgsConstructor
public enum UserType {
    SYSTEM("system"),
    MEMBER("member");

    private final String code;
}
