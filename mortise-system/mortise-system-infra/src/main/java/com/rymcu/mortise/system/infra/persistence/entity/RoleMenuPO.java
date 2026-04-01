package com.rymcu.mortise.system.infra.persistence.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;

@Data
@Table(value = "mortise_role_menu", schema = "mortise")
public class RoleMenuPO implements Serializable {

    @Id
    private Long idMortiseRole;

    @Id
    private Long idMortiseMenu;

    public RoleMenuPO() {
    }

    public RoleMenuPO(Long idMortiseRole, Long idMortiseMenu) {
        this.idMortiseRole = idMortiseRole;
        this.idMortiseMenu = idMortiseMenu;
    }
}
