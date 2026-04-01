package com.rymcu.mortise.system.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * Created on 2025/7/22 22:33.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.entity
 */
@Data
public class UserRole implements Serializable {

    private Long idMortiseUser;

    private Long idMortiseRole;

    public UserRole() {

    }

    public UserRole(Long idMortiseUser, Long idMortiseRole) {
        this.idMortiseUser = idMortiseUser;
        this.idMortiseRole = idMortiseRole;
    }

}
