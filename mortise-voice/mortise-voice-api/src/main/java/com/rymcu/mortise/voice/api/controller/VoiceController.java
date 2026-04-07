package com.rymcu.mortise.voice.api.controller;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.voice.api.contract.response.VoiceProfileOption;
import com.rymcu.mortise.voice.api.facade.VoiceCatalogFacade;
import com.rymcu.mortise.web.annotation.ApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 用户端语音控制器。
 */
@Tag(name = "语音服务", description = "用户端语音配置查询")
@ApiController
@RequestMapping("/voice")
@RequiredArgsConstructor
public class VoiceController {

    private final VoiceCatalogFacade voiceCatalogFacade;

    @Operation(summary = "获取可用语音配置")
    @GetMapping("/profiles")
    @PreAuthorize("isAuthenticated()")
    @ApiLog(recordParams = false, recordResponseBody = false, value = "查询可用语音配置")
    public GlobalResult<List<VoiceProfileOption>> listProfiles() {
        return GlobalResult.success(voiceCatalogFacade.listProfiles());
    }
}