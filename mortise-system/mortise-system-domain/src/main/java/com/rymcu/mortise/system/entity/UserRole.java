package com.rymcu.mortise.system.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;

/**
 * Created on 2025/7/22 22:33.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.entity
 */
@Table(value = "mortise_user_role", schema = "mortise")
@Data
public class UserRole implements Serializable {

    @Id
    private Long idMortiseUser;

    @Id
    private Long idMortiseRole;

    public UserRole() {

    }

    public UserRole(Long idMortiseUser, Long idMortiseRole) {
        this.idMortiseUser = idMortiseUser;
        this.idMortiseRole = idMortiseRole;
    }

}
