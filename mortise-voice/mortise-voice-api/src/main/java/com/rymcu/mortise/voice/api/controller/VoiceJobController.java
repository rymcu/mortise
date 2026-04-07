package com.rymcu.mortise.voice.api.controller;

import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.voice.api.contract.query.VoiceJobSearch;
import com.rymcu.mortise.voice.api.contract.response.VoiceJobDetailResponse;
import com.rymcu.mortise.voice.api.contract.response.VoiceJobSummaryResponse;
import com.rymcu.mortise.voice.api.facade.VoiceJobFacade;
import com.rymcu.mortise.web.annotation.ApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 用户端语音任务控制器。
 */
@Tag(name = "语音任务", description = "用户端语音任务历史与详情查询")
@ApiController
@RequestMapping("/voice/jobs")
@RequiredArgsConstructor
public class VoiceJobController {

    private final VoiceJobFacade voiceJobFacade;

    @Operation(summary = "获取我的语音任务列表")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "查询我的语音任务列表")
    public GlobalResult<PageResult<VoiceJobSummaryResponse>> listJobs(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Parameter(description = "查询条件") @Valid VoiceJobSearch search
    ) {
        Long userId = currentUser != null ? currentUser.getUserId() : null;
        return GlobalResult.success(voiceJobFacade.listJobs(
                userId,
                PageQuery.of(search.getPageNum(), search.getPageSize()),
                search
        ));
    }

    @Operation(summary = "获取我的语音任务详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "查询我的语音任务详情")
    public GlobalResult<VoiceJobDetailResponse> getJobById(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable("id") Long id
    ) {
        Long userId = currentUser != null ? currentUser.getUserId() : null;
        return GlobalResult.success(voiceJobFacade.findJobById(userId, id));
    }
}