package com.rymcu.mortise.member.enumerate;

import lombok.Getter;

@Getter
public enum FamilyInvitationStatus {

    PENDING("pending", "待处理"),
    ACCEPTED("accepted", "已接受"),
    REJECTED("rejected", "已拒绝"),
    CANCELLED("cancelled", "已取消"),
    EXPIRED("expired", "已过期");

    private final String code;
    private final String description;

    FamilyInvitationStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
