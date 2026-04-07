package com.rymcu.mortise.voice.api.facade;

import com.rymcu.mortise.voice.api.contract.request.VoiceTtsSynthesizeRequest;
import com.rymcu.mortise.voice.api.contract.response.VoiceTtsSynthesizeResponse;

/**
 * 用户端同步语音合成门面。
 */
public interface VoiceTtsFacade {

    VoiceTtsSynthesizeResponse synthesize(Long userId, VoiceTtsSynthesizeRequest request);
}