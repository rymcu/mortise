package com.rymcu.mortise.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色菜单关联实体
 * 
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2025/9/29
 */
@Table(value = "mortise_role_menu", schema = "mortise")
@Data
public class RoleMenu implements Serializable {

    /**
     * 角色ID
     */
    private Long idMortiseRole;

    /**
     * 菜单ID
     */
    private Long idMortiseMenu;

    /**
     * 默认构造函数
     */
    public RoleMenu() {
    }

    /**
     * 带参构造函数
     * 
     * @param idMortiseRole 角色ID
     * @param idMortiseMenu 菜单ID
     */
    public RoleMenu(Long idMortiseRole, Long idMortiseMenu) {
        this.idMortiseRole = idMortiseRole;
        this.idMortiseMenu = idMortiseMenu;
    }
}