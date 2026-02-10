package com.rymcu.mortise.member.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * 会员信息更新请求
 *
 * @author ronger
 */
public record MemberUpdateRequest(
        String nickname,
        String avatarUrl,
        String gender,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate birthDate
) {
}
