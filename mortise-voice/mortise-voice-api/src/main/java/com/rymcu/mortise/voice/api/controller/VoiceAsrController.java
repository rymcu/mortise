package com.rymcu.mortise.voice.api.controller;

import com.rymcu.mortise.core.model.CurrentUser;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.voice.api.contract.response.VoiceAsrRecognizeResponse;
import com.rymcu.mortise.voice.api.facade.VoiceAsrFacade;
import com.rymcu.mortise.web.annotation.ApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户端短音频识别控制器。
 */
@Validated
@Tag(name = "短音频识别", description = "用户端短音频同步识别接口")
@ApiController
@RequestMapping("/voice/asr")
public class VoiceAsrController {

    private final VoiceAsrFacade voiceAsrFacade;

    public VoiceAsrController(VoiceAsrFacade voiceAsrFacade) {
        this.voiceAsrFacade = voiceAsrFacade;
    }

    @Operation(summary = "短音频同步识别")
    @PostMapping(value = "/recognize-once", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @ApiLog(recordRequestBody = false, recordResponseBody = false, value = "短音频同步识别")
    public GlobalResult<VoiceAsrRecognizeResponse> recognizeOnce(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Parameter(description = "语音配置编码", required = true) @NotBlank @RequestParam String profileCode,
            @Parameter(description = "音频文件", required = true) @NotNull @RequestParam("file") MultipartFile file,
            @Parameter(description = "来源模块，可选") @RequestParam(required = false) String sourceModule
    ) {
        Long userId = currentUser != null ? currentUser.getUserId() : null;
        return GlobalResult.success(voiceAsrFacade.recognizeOnce(userId, profileCode, file, sourceModule));
    }
}