package com.rymcu.mortise.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * Created on 2025/7/23 10:04.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.entity
 */
@Table(value = "mortise_user_role", schema = "mortise")
@Data
public class UserRole {

    private Long idMortiseUser;

    private Long idMortiseRole;

}
