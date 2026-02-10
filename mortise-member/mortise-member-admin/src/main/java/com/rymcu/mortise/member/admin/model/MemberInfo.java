package com.rymcu.mortise.member.admin.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员信息VO
 *
 * @author ronger
 */
public record MemberInfo(
        Long id,
        String username,
        String email,
        String phone,
        String name,
        String nickname,
        String avatarUrl,
        String gender,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate birthDate,
        Integer status,
        String memberLevel,
        Integer points,
        BigDecimal balance,
        String registerSource,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime lastLoginTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedTime
) {
}
