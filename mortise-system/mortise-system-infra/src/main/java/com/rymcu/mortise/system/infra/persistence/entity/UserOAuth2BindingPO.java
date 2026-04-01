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
@Table(value = "mortise_user_oauth2_binding", schema = "mortise")
public class UserOAuth2BindingPO implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;
    private Long userId;
    private String provider;
    private String openId;
    private String unionId;
    private String nickname;
    private String avatar;
    private String email;

    @Column(isLarge = true)
    private String accessToken;

    @Column(isLarge = true)
    private String refreshToken;

    private LocalDateTime expiresAt;

    @Column(isLarge = true)
    private String rawData;

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
