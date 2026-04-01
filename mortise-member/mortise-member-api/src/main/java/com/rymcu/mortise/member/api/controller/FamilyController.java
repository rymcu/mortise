package com.rymcu.mortise.member.api.controller;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.member.api.facade.FamilyApiFacade;
import com.rymcu.mortise.member.api.model.FamilyApiModels;
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

    private final FamilyApiFacade familyApiFacade;

    @PostMapping
    @Operation(summary = "创建家庭")
    @ApiLog(recordParams = false, recordRequestBody = false, value = "创建家庭")
    @OperationLog(module = "家庭协作", operation = "创建家庭", recordParams = false, recordResult = true)
    public GlobalResult<FamilyApiModels.FamilySummaryResponse> createFamily(
            @Valid @RequestBody FamilyApiModels.CreateFamilyRequest request) {
        return familyApiFacade.createFamily(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新家庭")
    @ApiLog(recordParams = false, recordRequestBody = false, value = "更新家庭")
    @OperationLog(module = "家庭协作", operation = "更新家庭", recordParams = false, recordResult = true)
    public GlobalResult<FamilyApiModels.FamilySummaryResponse> updateFamily(
            @PathVariable Long id,
            @Valid @RequestBody FamilyApiModels.UpdateFamilyRequest request) {
        return familyApiFacade.updateFamily(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "解散家庭")
    @ApiLog(recordParams = false, value = "解散家庭")
    @OperationLog(module = "家庭协作", operation = "解散家庭", recordParams = false, recordResult = true)
    public GlobalResult<Void> dissolveFamily(@PathVariable Long id) {
        return familyApiFacade.dissolveFamily(id);
    }

    @GetMapping
    @Operation(summary = "获取我的家庭列表")
    public GlobalResult<FamilyApiModels.FamilyListResponse> listFamilies() {
        return familyApiFacade.listFamilies();
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前家庭")
    public GlobalResult<FamilyApiModels.FamilySummaryResponse> currentFamily() {
        return familyApiFacade.currentFamily();
    }

    @PutMapping("/current")
    @Operation(summary = "切换当前家庭")
    @ApiLog(recordParams = false, recordRequestBody = false, value = "切换当前家庭")
    @OperationLog(module = "家庭协作", operation = "切换当前家庭", recordParams = false, recordResult = true)
    public GlobalResult<FamilyApiModels.FamilySummaryResponse> switchCurrentFamily(
            @Valid @RequestBody FamilyApiModels.SwitchCurrentFamilyRequest request) {
        return familyApiFacade.switchCurrentFamily(request);
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "获取家庭成员列表")
    public GlobalResult<List<FamilyApiModels.FamilyMemberResponse>> listFamilyMembers(@PathVariable Long id) {
        return familyApiFacade.listFamilyMembers(id);
    }

    @PostMapping("/{id}/invitations")
    @Operation(summary = "邀请成员加入家庭")
    @ApiLog(recordParams = false, recordRequestBody = false, value = "邀请家庭成员")
    @OperationLog(module = "家庭协作", operation = "邀请家庭成员", recordParams = false, recordResult = true)
    public GlobalResult<FamilyApiModels.FamilyInvitationResponse> inviteFamilyMember(
            @PathVariable Long id,
            @Valid @RequestBody FamilyApiModels.InviteFamilyMemberRequest request) {
        return familyApiFacade.inviteFamilyMember(id, request);
    }

    @GetMapping("/invitations")
    @Operation(summary = "获取我的待处理家庭邀请")
    public GlobalResult<List<FamilyApiModels.FamilyInvitationResponse>> listInvitations() {
        return familyApiFacade.listInvitations();
    }

    @PostMapping("/invitations/{id}/accept")
    @Operation(summary = "接受家庭邀请")
    @ApiLog(recordParams = false, value = "接受家庭邀请")
    @OperationLog(module = "家庭协作", operation = "接受家庭邀请", recordParams = false, recordResult = true)
    public GlobalResult<FamilyApiModels.FamilyInvitationResponse> acceptInvitation(@PathVariable Long id) {
        return familyApiFacade.acceptInvitation(id);
    }

    @PostMapping("/invitations/{id}/reject")
    @Operation(summary = "拒绝家庭邀请")
    @ApiLog(recordParams = false, value = "拒绝家庭邀请")
    @OperationLog(module = "家庭协作", operation = "拒绝家庭邀请", recordParams = false, recordResult = true)
    public GlobalResult<FamilyApiModels.FamilyInvitationResponse> rejectInvitation(@PathVariable Long id) {
        return familyApiFacade.rejectInvitation(id);
    }

    @PostMapping("/invitations/{id}/cancel")
    @Operation(summary = "取消家庭邀请")
    @ApiLog(recordParams = false, value = "取消家庭邀请")
    @OperationLog(module = "家庭协作", operation = "取消家庭邀请", recordParams = false, recordResult = true)
    public GlobalResult<FamilyApiModels.FamilyInvitationResponse> cancelInvitation(@PathVariable Long id) {
        return familyApiFacade.cancelInvitation(id);
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @Operation(summary = "移除家庭成员")
    @ApiLog(recordParams = false, value = "移除家庭成员")
    @OperationLog(module = "家庭协作", operation = "移除家庭成员", recordParams = false, recordResult = true)
    public GlobalResult<Void> removeFamilyMember(@PathVariable Long id, @PathVariable Long memberId) {
        return familyApiFacade.removeFamilyMember(id, memberId);
    }

    @PostMapping("/{id}/leave")
    @Operation(summary = "退出家庭")
    @ApiLog(recordParams = false, value = "退出家庭")
    @OperationLog(module = "家庭协作", operation = "退出家庭", recordParams = false, recordResult = true)
    public GlobalResult<Void> leaveFamily(@PathVariable Long id) {
        return familyApiFacade.leaveFamily(id);
    }
}
