package com.rymcu.mortise.voice.admin.controller;

import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.voice.admin.contract.query.VoiceJobSearch;
import com.rymcu.mortise.voice.admin.contract.response.VoiceJobInfo;
import com.rymcu.mortise.voice.admin.facade.AdminVoiceJobFacade;
import com.rymcu.mortise.web.annotation.AdminController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端语音任务控制器。
 */
@Tag(name = "语音任务管理", description = "语音任务历史与详情查询")
@AdminController
@RequestMapping("/voice/jobs")
@RequiredArgsConstructor
public class VoiceAdminJobController {

    private final AdminVoiceJobFacade adminVoiceJobFacade;

    @Operation(summary = "获取语音任务列表")
    @GetMapping
    @PreAuthorize("hasAuthority('voice:job:list')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "查询语音任务列表")
    public GlobalResult<PageResult<VoiceJobInfo>> listJobs(
            @Parameter(description = "查询条件") @Valid VoiceJobSearch search
    ) {
        return GlobalResult.success(adminVoiceJobFacade.findJobPage(
                PageQuery.of(search.getPageNum(), search.getPageSize()),
                search
        ));
    }

    @Operation(summary = "获取语音任务详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('voice:job:query')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "查询语音任务详情")
    public GlobalResult<VoiceJobInfo> getJobById(@PathVariable("id") Long id) {
        return GlobalResult.success(adminVoiceJobFacade.findJobById(id));
    }
}