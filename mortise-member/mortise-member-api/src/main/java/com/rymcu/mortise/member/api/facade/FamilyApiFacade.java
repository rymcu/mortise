package com.rymcu.mortise.member.api.facade;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.member.api.model.FamilyApiModels;

import java.util.List;

public interface FamilyApiFacade {

    GlobalResult<FamilyApiModels.FamilySummaryResponse> createFamily(FamilyApiModels.CreateFamilyRequest request);

    GlobalResult<FamilyApiModels.FamilySummaryResponse> updateFamily(Long id, FamilyApiModels.UpdateFamilyRequest request);

    GlobalResult<Void> dissolveFamily(Long id);

    GlobalResult<FamilyApiModels.FamilyListResponse> listFamilies();

    GlobalResult<FamilyApiModels.FamilySummaryResponse> currentFamily();

    GlobalResult<FamilyApiModels.FamilySummaryResponse> switchCurrentFamily(FamilyApiModels.SwitchCurrentFamilyRequest request);

    GlobalResult<List<FamilyApiModels.FamilyMemberResponse>> listFamilyMembers(Long id);

    GlobalResult<FamilyApiModels.FamilyInvitationResponse> inviteFamilyMember(Long id, FamilyApiModels.InviteFamilyMemberRequest request);

    GlobalResult<List<FamilyApiModels.FamilyInvitationResponse>> listInvitations();

    GlobalResult<FamilyApiModels.FamilyInvitationResponse> acceptInvitation(Long id);

    GlobalResult<FamilyApiModels.FamilyInvitationResponse> rejectInvitation(Long id);

    GlobalResult<FamilyApiModels.FamilyInvitationResponse> cancelInvitation(Long id);

    GlobalResult<Void> removeFamilyMember(Long id, Long memberId);

    GlobalResult<Void> leaveFamily(Long id);
}
