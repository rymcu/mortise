package com.rymcu.mortise.system.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

/**
 * Created on 2024/5/5 10:52.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.model
 */
@Data
@AllArgsConstructor
public class BindRoleUserInfo {

    private Long idRole;

    private Set<Long> idUsers;

}
