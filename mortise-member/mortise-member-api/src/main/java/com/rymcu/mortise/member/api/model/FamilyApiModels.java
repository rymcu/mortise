package com.rymcu.mortise.member.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public final class FamilyApiModels {

    private FamilyApiModels() {
    }

    public record CreateFamilyRequest(
            @NotBlank(message = "家庭名称不能为空")
            String familyName,
            String description
    ) {
    }

    public record UpdateFamilyRequest(
            @NotBlank(message = "家庭名称不能为空")
            String familyName,
            String description
    ) {
    }

    public record SwitchCurrentFamilyRequest(
            @NotNull(message = "家庭ID不能为空")
            Long familyId
    ) {
    }

    public record InviteFamilyMemberRequest(
            @NotNull(message = "受邀会员ID不能为空")
            Long inviteeMemberId,
            String inviteMessage
    ) {
    }

    public record FamilySummaryResponse(
            Long id,
            String familyName,
            String description,
            Long ownerMemberId,
            int memberCount,
            boolean current
    ) {
    }

    public record FamilyMemberResponse(
            Long memberId,
            String nickname,
            String avatarUrl,
            String roleCode,
            LocalDateTime joinedTime
    ) {
    }

    public record FamilyInvitationResponse(
            Long id,
            Long familyId,
            String familyName,
            Long inviterMemberId,
            String inviterNickname,
            Long inviteeMemberId,
            String inviteMessage,
            String statusCode,
            LocalDateTime expiresTime,
            LocalDateTime createdTime
    ) {
    }

    public record FamilyListResponse(
            List<FamilySummaryResponse> families,
            FamilySummaryResponse currentFamily
    ) {
    }
}
