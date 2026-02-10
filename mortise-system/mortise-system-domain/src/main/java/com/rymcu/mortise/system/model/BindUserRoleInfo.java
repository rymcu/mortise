package com.rymcu.mortise.system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Created on 2024/5/5 10:52.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BindUserRoleInfo {

    private Long idUser;

    private Set<Long> idRoles;

}
