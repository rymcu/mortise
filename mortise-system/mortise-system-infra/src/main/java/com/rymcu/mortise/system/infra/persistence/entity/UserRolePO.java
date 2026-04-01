package com.rymcu.mortise.system.infra.persistence.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;

@Data
@Table(value = "mortise_user_role", schema = "mortise")
public class UserRolePO implements Serializable {

    @Id
    private Long idMortiseUser;

    @Id
    private Long idMortiseRole;

    public UserRolePO() {
    }

    public UserRolePO(Long idMortiseUser, Long idMortiseRole) {
        this.idMortiseUser = idMortiseUser;
        this.idMortiseRole = idMortiseRole;
    }
}
