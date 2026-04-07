package com.rymcu.mortise.voice.api.controller;

import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.voice.api.contract.request.VoiceTtsSynthesizeRequest;
import com.rymcu.mortise.voice.api.contract.response.VoiceTtsSynthesizeResponse;
import com.rymcu.mortise.voice.api.facade.VoiceTtsFacade;
import com.rymcu.mortise.web.annotation.ApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 用户端同步语音合成控制器。
 */
@Validated
@Tag(name = "语音合成", description = "用户端同步语音合成接口")
@ApiController
@RequestMapping("/voice/tts")
public class VoiceTtsController {

    private final VoiceTtsFacade voiceTtsFacade;

    public VoiceTtsController(VoiceTtsFacade voiceTtsFacade) {
        this.voiceTtsFacade = voiceTtsFacade;
    }

    @Operation(summary = "同步语音合成")
    @PostMapping("/synthesize")
    @PreAuthorize("isAuthenticated()")
    @ApiLog(recordRequestBody = true, recordResponseBody = false, value = "同步语音合成")
    public GlobalResult<VoiceTtsSynthesizeResponse> synthesize(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody VoiceTtsSynthesizeRequest request
    ) {
        Long userId = currentUser != null ? currentUser.getUserId() : null;
        return GlobalResult.success(voiceTtsFacade.synthesize(userId, request));
    }
}