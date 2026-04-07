package com.rymcu.mortise.voice.kernel.spi;

import com.rymcu.mortise.voice.kernel.model.AsrRequest;
import com.rymcu.mortise.voice.kernel.model.AsrResponse;
import com.rymcu.mortise.voice.kernel.model.TtsRequest;
import com.rymcu.mortise.voice.kernel.model.TtsResponse;
import com.rymcu.mortise.voice.kernel.model.VoiceRuntimeNodeStatus;

import java.util.List;

/**
 * Java 到语音运行时的统一客户端契约。
 */
public interface VoiceRuntimeClient {

    List<VoiceRuntimeNodeStatus> listNodes();

    boolean warmupModel(String runtimeName);

    AsrResponse recognizeOnce(AsrRequest request);

    TtsResponse synthesize(TtsRequest request);
}