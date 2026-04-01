package com.rymcu.mortise.system.infra.persistence.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table(value = "mortise_role", schema = "mortise")
public class RolePO implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;
    private String label;
    private String permission;
    private Integer status;

    @Column(comment = "是否为默认角色")
    private Integer isDefault;

    @Column(isLogicDelete = true)
    private Integer delFlag;

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
