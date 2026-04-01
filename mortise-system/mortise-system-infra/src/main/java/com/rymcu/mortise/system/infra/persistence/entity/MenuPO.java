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
@Table(value = "mortise_menu", schema = "mortise")
public class MenuPO implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;
    private String label;
    private String permission;
    private String icon;
    private String href;
    private Integer status;

    @Column(isLogicDelete = true)
    private Integer delFlag;

    private Integer menuType;
    private Integer sortNo;
    private Long parentId;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
