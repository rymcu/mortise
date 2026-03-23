package com.rymcu.mortise.member.enumerate;

import lombok.Getter;

@Getter
public enum FamilyMemberRole {

    ADMIN("admin", "管理员"),
    MEMBER("member", "普通成员");

    private final String code;
    private final String description;

    FamilyMemberRole(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
