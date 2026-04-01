package com.rymcu.mortise.member.api.facade.impl;

import com.rymcu.mortise.core.model.FamilyInfo;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.member.api.facade.FamilyApiFacade;
import com.rymcu.mortise.member.api.model.FamilyApiModels;
import com.rymcu.mortise.member.api.service.MemberContextService;
import com.rymcu.mortise.member.entity.Family;
import com.rymcu.mortise.member.entity.FamilyInvitation;
import com.rymcu.mortise.member.service.FamilyService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FamilyApiFacadeImpl implements FamilyApiFacade {

    private final FamilyService familyService;
    private final MemberContextService memberContextService;

    public FamilyApiFacadeImpl(FamilyService familyService, MemberContextService memberContextService) {
        this.familyService = familyService;
        this.memberContextService = memberContextService;
    }

    @Override
    public GlobalResult<FamilyApiModels.FamilySummaryResponse> createFamily(FamilyApiModels.CreateFamilyRequest request) {
        Family family = familyService.createFamily(new FamilyService.CreateFamilyCommand(
                currentMemberId(),
                request.familyName(),
                request.description()
        ));
        return GlobalResult.success(toSummaryResponse(
                familyService.getFamily(family.getId()).orElseThrow(),
                family.getId()
        ));
    }

    @Override
    public GlobalResult<FamilyApiModels.FamilySummaryResponse> updateFamily(Long id, FamilyApiModels.UpdateFamilyRequest request) {
        familyService.updateFamily(new FamilyService.UpdateFamilyCommand(
                id,
                currentMemberId(),
                request.familyName(),
                request.description()
        ));
        Long currentFamilyId = familyService.getCurrentFamily(currentMemberId()).map(FamilyInfo::id).orElse(null);
        return GlobalResult.success(toSummaryResponse(familyService.getFamily(id).orElseThrow(), currentFamilyId));
    }

    @Override
    public GlobalResult<Void> dissolveFamily(Long id) {
        familyService.dissolveFamily(id, currentMemberId());
        return GlobalResult.success();
    }

    @Override
    public GlobalResult<FamilyApiModels.FamilyListResponse> listFamilies() {
        Long memberId = currentMemberId();
        FamilyApiModels.FamilySummaryResponse current = familyService.getCurrentFamily(memberId)
                .map(info -> toSummaryResponse(info, info.id()))
                .orElse(null);
        Long currentFamilyId = current != null ? current.id() : null;
        List<FamilyApiModels.FamilySummaryResponse> families = familyService.listMemberFamilies(memberId).stream()
                .map(info -> toSummaryResponse(info, currentFamilyId))
                .toList();
        return GlobalResult.success(new FamilyApiModels.FamilyListResponse(families, current));
    }

    @Override
    public GlobalResult<FamilyApiModels.FamilySummaryResponse> currentFamily() {
        return GlobalResult.success(familyService.getCurrentFamily(currentMemberId())
                .map(info -> toSummaryResponse(info, info.id()))
                .orElse(null));
    }

    @Override
    public GlobalResult<FamilyApiModels.FamilySummaryResponse> switchCurrentFamily(FamilyApiModels.SwitchCurrentFamilyRequest request) {
        FamilyInfo family = familyService.switchCurrentFamily(currentMemberId(), request.familyId());
        return GlobalResult.success(toSummaryResponse(family, family.id()));
    }

    @Override
    public GlobalResult<List<FamilyApiModels.FamilyMemberResponse>> listFamilyMembers(Long id) {
        return GlobalResult.success(familyService.listFamilyMembers(id, currentMemberId()).stream()
                .map(this::toFamilyMemberResponse)
                .toList());
    }

    @Override
    public GlobalResult<FamilyApiModels.FamilyInvitationResponse> inviteFamilyMember(Long id, FamilyApiModels.InviteFamilyMemberRequest request) {
        FamilyInvitation invitation = familyService.inviteMember(new FamilyService.InviteMemberCommand(
                id,
                currentMemberId(),
                request.inviteeMemberId(),
                request.inviteMessage()
        ));
        return GlobalResult.success(loadInvitationResponse(invitation.getId()));
    }

    @Override
    public GlobalResult<List<FamilyApiModels.FamilyInvitationResponse>> listInvitations() {
        return GlobalResult.success(familyService.listPendingInvitations(currentMemberId()).stream()
                .map(this::toInvitationResponse)
                .toList());
    }

    @Override
    public GlobalResult<FamilyApiModels.FamilyInvitationResponse> acceptInvitation(Long id) {
        FamilyInvitation invitation = familyService.acceptInvitation(id, currentMemberId());
        return GlobalResult.success(loadInvitationResponse(invitation.getId()));
    }

    @Override
    public GlobalResult<FamilyApiModels.FamilyInvitationResponse> rejectInvitation(Long id) {
        FamilyInvitation invitation = familyService.rejectInvitation(id, currentMemberId());
        return GlobalResult.success(loadInvitationResponse(invitation.getId()));
    }

    @Override
    public GlobalResult<FamilyApiModels.FamilyInvitationResponse> cancelInvitation(Long id) {
        FamilyInvitation invitation = familyService.cancelInvitation(id, currentMemberId());
        return GlobalResult.success(loadInvitationResponse(invitation.getId()));
    }

    @Override
    public GlobalResult<Void> removeFamilyMember(Long id, Long memberId) {
        familyService.removeMember(new FamilyService.RemoveFamilyMemberCommand(id, currentMemberId(), memberId));
        return GlobalResult.success();
    }

    @Override
    public GlobalResult<Void> leaveFamily(Long id) {
        familyService.leaveFamily(id, currentMemberId());
        return GlobalResult.success();
    }

    private Long currentMemberId() {
        Long memberId = memberContextService.getCurrentMemberId();
        if (memberId == null) {
            throw new IllegalStateException("未登录");
        }
        return memberId;
    }

    private FamilyApiModels.FamilySummaryResponse toSummaryResponse(FamilyInfo info, Long currentFamilyId) {
        return new FamilyApiModels.FamilySummaryResponse(
                info.id(),
                info.familyName(),
                info.description(),
                info.ownerMemberId(),
                info.memberCount(),
                info.id().equals(currentFamilyId)
        );
    }

    private FamilyApiModels.FamilyMemberResponse toFamilyMemberResponse(FamilyService.FamilyMemberView view) {
        return new FamilyApiModels.FamilyMemberResponse(
                view.memberId(),
                view.nickname(),
                view.avatarUrl(),
                view.roleCode(),
                view.joinedTime()
        );
    }

    private FamilyApiModels.FamilyInvitationResponse toInvitationResponse(FamilyService.FamilyInvitationView view) {
        return new FamilyApiModels.FamilyInvitationResponse(
                view.id(),
                view.familyId(),
                view.familyName(),
                view.inviterMemberId(),
                view.inviterNickname(),
                view.inviteeMemberId(),
                view.inviteMessage(),
                view.statusCode(),
                view.expiresTime(),
                view.createdTime()
        );
    }

    private FamilyApiModels.FamilyInvitationResponse loadInvitationResponse(Long invitationId) {
        return familyService.getInvitationView(invitationId)
                .map(this::toInvitationResponse)
                .orElse(null);
    }
}
