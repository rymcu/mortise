package com.rymcu.mortise.member.service;

import com.rymcu.mortise.core.model.FamilyInfo;
import com.rymcu.mortise.member.entity.Family;
import com.rymcu.mortise.member.entity.FamilyInvitation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FamilyService {

    Family createFamily(CreateFamilyCommand command);

    Family updateFamily(UpdateFamilyCommand command);

    void dissolveFamily(Long familyId, Long operatorMemberId);

    List<FamilyInfo> listMemberFamilies(Long memberId);

    Optional<FamilyInfo> getFamily(Long familyId);

    Optional<FamilyInfo> getCurrentFamily(Long memberId);

    FamilyInfo switchCurrentFamily(Long memberId, Long familyId);

    List<FamilyMemberView> listFamilyMembers(Long familyId, Long operatorMemberId);

    FamilyInvitation inviteMember(InviteMemberCommand command);

    List<FamilyInvitationView> listPendingInvitations(Long memberId);

    Optional<FamilyInvitationView> getInvitationView(Long invitationId);

    FamilyInvitation acceptInvitation(Long invitationId, Long memberId);

    FamilyInvitation rejectInvitation(Long invitationId, Long memberId);

    FamilyInvitation cancelInvitation(Long invitationId, Long operatorMemberId);

    void removeMember(RemoveFamilyMemberCommand command);

    void leaveFamily(Long familyId, Long memberId);

    boolean isFamilyMember(Long memberId, Long familyId);

    boolean isFamilyAdmin(Long memberId, Long familyId);

    record CreateFamilyCommand(Long ownerMemberId, String familyName, String description) {
    }

    record UpdateFamilyCommand(Long familyId, Long operatorMemberId, String familyName, String description) {
    }

    record InviteMemberCommand(Long familyId, Long inviterMemberId, Long inviteeMemberId, String inviteMessage) {
    }

    record RemoveFamilyMemberCommand(Long familyId, Long operatorMemberId, Long targetMemberId) {
    }

    record FamilyMemberView(
            Long memberId,
            String nickname,
            String avatarUrl,
            String roleCode,
            LocalDateTime joinedTime
    ) {
    }

    record FamilyInvitationView(
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
}
