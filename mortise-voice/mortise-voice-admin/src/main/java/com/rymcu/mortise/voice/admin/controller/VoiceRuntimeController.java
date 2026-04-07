package com.rymcu.mortise.voice.admin.controller;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.voice.admin.contract.response.VoiceRuntimeNodeInfo;
import com.rymcu.mortise.voice.admin.facade.AdminVoiceCatalogFacade;
import com.rymcu.mortise.web.annotation.AdminController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 管理端运行时控制器。
 */
@Tag(name = "语音运行时管理", description = "运行时节点状态查询")
@AdminController
@RequestMapping("/voice/runtime")
@RequiredArgsConstructor
public class VoiceRuntimeController {

    private final AdminVoiceCatalogFacade adminVoiceCatalogFacade;

    @Operation(summary = "获取语音运行时节点列表")
    @GetMapping("/nodes")
    @PreAuthorize("hasAuthority('voice:runtime:list')")
    @ApiLog(recordParams = false, recordResponseBody = false, value = "查询语音运行时节点")
    public GlobalResult<List<VoiceRuntimeNodeInfo>> listRuntimeNodes() {
        return GlobalResult.success(adminVoiceCatalogFacade.listRuntimeNodes());
    }
}