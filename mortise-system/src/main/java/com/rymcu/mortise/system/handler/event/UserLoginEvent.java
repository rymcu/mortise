package com.rymcu.mortise.system.handler.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created on 2025/7/22 9:25.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.handler.event
 */
@Data
@AllArgsConstructor
public class UserLoginEvent {

    private String username;

}
