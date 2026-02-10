package com.rymcu.mortise.member.admin.controller;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.member.admin.model.MemberInfo;
import com.rymcu.mortise.member.admin.model.MemberSearch;
import com.rymcu.mortise.member.admin.service.AdminMemberService;
import com.rymcu.mortise.web.annotation.AdminController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 会员管理控制器
 * 提供会员的列表查询、详情查看、状态管理等功能
 *
 * @author ronger
 */
@Tag(name = "会员管理", description = "会员管理相关接口")
@AdminController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final AdminMemberService adminMemberService;

    @Operation(summary = "获取会员列表", description = "分页查询会员信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping
    @ApiLog(recordParams = false, recordResponseBody = false, value = "查询会员列表")
    public GlobalResult<Page<MemberInfo>> listMembers(@Parameter(description = "会员查询条件") @Valid MemberSearch search) {
        Page<MemberInfo> page = new Page<>(search.getPageNum(), search.getPageSize());
        page = adminMemberService.findMemberList(page, search);
        return GlobalResult.success(page);
    }

    @Operation(summary = "获取会员详情", description = "根据ID获取会员详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "会员不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/{id}")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "获取会员详情")
    public GlobalResult<MemberInfo> getMemberById(@Parameter(description = "会员ID", required = true) @PathVariable("id") Long idMember) {
        return GlobalResult.success(adminMemberService.findMemberInfoById(idMember));
    }

    @Operation(summary = "更新会员状态", description = "启用/禁用会员")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "会员不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PatchMapping("/{id}/status")
    @ApiLog(recordParams = true, recordRequestBody = false, recordResponseBody = false, value = "更新会员状态")
    @OperationLog(module = "会员管理", operation = "更新会员状态", recordParams = true)
    public GlobalResult<Boolean> updateMemberStatus(
            @Parameter(description = "会员ID", required = true) @PathVariable("id") Long idMember,
            @Parameter(description = "会员状态信息", required = true) @Valid @RequestBody MemberInfo memberInfo) {
        return GlobalResult.success(adminMemberService.updateStatus(idMember, memberInfo.status()));
    }

    @Operation(summary = "启用会员", description = "启用指定会员")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "启用成功"),
            @ApiResponse(responseCode = "404", description = "会员不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PutMapping("/{id}/enable")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "启用会员")
    @OperationLog(module = "会员管理", operation = "启用会员", recordParams = true)
    public GlobalResult<Boolean> enableMember(@Parameter(description = "会员ID", required = true) @PathVariable("id") Long idMember) {
        return GlobalResult.success(adminMemberService.enableMember(idMember));
    }

    @Operation(summary = "禁用会员", description = "禁用指定会员")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "禁用成功"),
            @ApiResponse(responseCode = "404", description = "会员不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PutMapping("/{id}/disable")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "禁用会员")
    @OperationLog(module = "会员管理", operation = "禁用会员", recordParams = true)
    public GlobalResult<Boolean> disableMember(@Parameter(description = "会员ID", required = true) @PathVariable("id") Long idMember) {
        return GlobalResult.success(adminMemberService.disableMember(idMember));
    }

}
