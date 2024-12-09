package com.rymcu.mortise.handler.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created on 2024/4/18 8:09.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.handler.event
 */
@Data
@AllArgsConstructor
public class RegisterEvent {

    private Long idUser;

    private String account;

    private String code;

}
