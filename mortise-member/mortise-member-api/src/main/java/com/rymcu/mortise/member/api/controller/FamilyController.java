package com.rymcu.mortise.member.api.controller;

import com.rymcu.mortise.core.model.FamilyInfo;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.member.api.model.FamilyApiModels;
import com.rymcu.mortise.member.api.service.MemberContextService;
import com.rymcu.mortise.member.entity.Family;
import com.rymcu.mortise.member.entity.FamilyInvitation;
import com.rymcu.mortise.member.service.FamilyService;
import com.rymcu.mortise.web.annotation.ApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@ApiController
@RequestMapping("/app/families")
@RequiredArgsConstructor
@Tag(name = "家庭协作", description = "家庭创建、切换、邀请与成员协作接口")
public class FamilyController {

    private final FamilyService familyService;
    private final MemberContextService memberContextService;

    @PostMapping
    @Operation(summary = "创建家庭")
    @ApiLog(recordParams = false, recordRequestBody = false, value = "创建家庭")
    @OperationLog(module = "家庭协作", operation = "创建家庭", recordParams = false, recordResult = true)
    public GlobalResult<FamilyApiModels.FamilySummaryResponse> createFamily(
            @Valid @RequestBody FamilyApiModels.CreateFamilyRequest request) {
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

    @PutMapping("/{id}")
    @Operation(summary = "更新家庭")
    @ApiLog(recordParams = false, recordRequestBody = false, value = "更新家庭")
    @OperationLog(module = "家庭协作", operation = "更新家庭", recordParams = false, recordResult = true)
    public GlobalResult<FamilyApiModels.FamilySummaryResponse> updateFamily(
            @PathVariable Long id,
            @Valid @RequestBody FamilyApiModels.UpdateFamilyRequest request) {
        familyService.updateFamily(new FamilyService.UpdateFamilyCommand(
                id,
                currentMemberId(),
                request.familyName(),
                request.description()
        ));
        Long currentFamilyId = familyService.getCurrentFamily(currentMemberId()).map(FamilyInfo::id).orElse(null);
        return GlobalResult.success(toSummaryResponse(familyService.getFamily(id).orElseThrow(), currentFamilyId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "解散家庭")
    @ApiLog(recordParams = false, value = "解散家庭")
    @OperationLog(module = "家庭协作", operation = "解散家庭", recordParams = false, recordResult = true)
    public GlobalResult<Void> dissolveFamily(@PathVariable Long id) {
        familyService.dissolveFamily(id, currentMemberId());
        return GlobalResult.success();
    }

    @GetMapping
    @Operation(summary = "获取我的家庭列表")
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

    @GetMapping("/current")
    @Operation(summary = "获取当前家庭")
    public GlobalResult<FamilyApiModels.FamilySummaryResponse> currentFamily() {
        return GlobalResult.success(familyService.getCurrentFamily(currentMemberId())
                .map(info -> toSummaryResponse(info, info.id()))
                .orElse(null));
    }

    @PutMapping("/current")
    @Operation(summary = "切换当前家庭")
    @ApiLog(recordParams = false, recordRequestBody = false, value = "切换当前家庭")
    @OperationLog(module = "家庭协作", operation = "切换当前家庭", recordParams = false, recordResult = true)
    public GlobalResult<FamilyApiModels.FamilySummaryResponse> switchCurrentFamily(
            @Valid @RequestBody FamilyApiModels.SwitchCurrentFamilyRequest request) {
        FamilyInfo family = familyService.switchCurrentFamily(currentMemberId(), request.familyId());
        return GlobalResult.success(toSummaryResponse(family, family.id()));
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "获取家庭成员列表")
    public GlobalResult<List<FamilyApiModels.FamilyMemberResponse>> listFamilyMembers(@PathVariable Long id) {
        return GlobalResult.success(familyService.listFamilyMembers(id, currentMemberId()).stream()
                .map(this::toFamilyMemberResponse)
                .toList());
    }

    @PostMapping("/{id}/invitations")
    @Operation(summary = "邀请成员加入家庭")
    @ApiLog(recordParams = false, recordRequestBody = false, value = "邀请家庭成员")
    @OperationLog(module = "家庭协作", operation = "邀请家庭成员", recordParams = false, recordResult = true)
    public GlobalResult<FamilyApiModels.FamilyInvitationResponse> inviteFamilyMember(
            @PathVariable Long id,
            @Valid @RequestBody FamilyApiModels.InviteFamilyMemberRequest request) {
        FamilyInvitation invitation = familyService.inviteMember(new FamilyService.InviteMemberCommand(
                id,
                currentMemberId(),
                request.inviteeMemberId(),
                request.inviteMessage()
        ));
        return GlobalResult.success(loadInvitationResponse(invitation.getId()));
    }

    @GetMapping("/invitations")
    @Operation(summary = "获取我的待处理家庭邀请")
    public GlobalResult<List<FamilyApiModels.FamilyInvitationResponse>> listInvitations() {
        return GlobalResult.success(familyService.listPendingInvitations(currentMemberId()).stream()
                .map(this::toInvitationResponse)
                .toList());
    }

    @PostMapping("/invitations/{id}/accept")
    @Operation(summary = "接受家庭邀请")
    @ApiLog(recordParams = false, value = "接受家庭邀请")
    @OperationLog(module = "家庭协作", operation = "接受家庭邀请", recordParams = false, recordResult = true)
    public GlobalResult<FamilyApiModels.FamilyInvitationResponse> acceptInvitation(@PathVariable Long id) {
        FamilyInvitation invitation = familyService.acceptInvitation(id, currentMemberId());
        return GlobalResult.success(loadInvitationResponse(invitation.getId()));
    }

    @PostMapping("/invitations/{id}/reject")
    @Operation(summary = "拒绝家庭邀请")
    @ApiLog(recordParams = false, value = "拒绝家庭邀请")
    @OperationLog(module = "家庭协作", operation = "拒绝家庭邀请", recordParams = false, recordResult = true)
    public GlobalResult<FamilyApiModels.FamilyInvitationResponse> rejectInvitation(@PathVariable Long id) {
        FamilyInvitation invitation = familyService.rejectInvitation(id, currentMemberId());
        return GlobalResult.success(loadInvitationResponse(invitation.getId()));
    }

    @PostMapping("/invitations/{id}/cancel")
    @Operation(summary = "取消家庭邀请")
    @ApiLog(recordParams = false, value = "取消家庭邀请")
    @OperationLog(module = "家庭协作", operation = "取消家庭邀请", recordParams = false, recordResult = true)
    public GlobalResult<FamilyApiModels.FamilyInvitationResponse> cancelInvitation(@PathVariable Long id) {
        FamilyInvitation invitation = familyService.cancelInvitation(id, currentMemberId());
        return GlobalResult.success(loadInvitationResponse(invitation.getId()));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @Operation(summary = "移除家庭成员")
    @ApiLog(recordParams = false, value = "移除家庭成员")
    @OperationLog(module = "家庭协作", operation = "移除家庭成员", recordParams = false, recordResult = true)
    public GlobalResult<Void> removeFamilyMember(@PathVariable Long id, @PathVariable Long memberId) {
        familyService.removeMember(new FamilyService.RemoveFamilyMemberCommand(id, currentMemberId(), memberId));
        return GlobalResult.success();
    }

    @PostMapping("/{id}/leave")
    @Operation(summary = "退出家庭")
    @ApiLog(recordParams = false, value = "退出家庭")
    @OperationLog(module = "家庭协作", operation = "退出家庭", recordParams = false, recordResult = true)
    public GlobalResult<Void> leaveFamily(@PathVariable Long id) {
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
