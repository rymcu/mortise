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
@Table(value = "mortise_user", schema = "mortise")
public class UserPO implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;
    private String account;
    private String password;
    private String nickname;
    private String realName;
    private String avatar;
    private String email;
    private String phone;
    private Integer status;

    @Column(isLogicDelete = true)
    private Integer delFlag;

    private LocalDateTime lastLoginTime;
    private LocalDateTime createdTime;
    private LocalDateTime lastOnlineTime;
}
