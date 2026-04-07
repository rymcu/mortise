package com.rymcu.mortise.voice.api.facade;

import com.rymcu.mortise.voice.api.contract.response.VoiceProfileOption;

import java.util.List;

/**
 * 用户端语音目录门面。
 */
public interface VoiceCatalogFacade {

    List<VoiceProfileOption> listProfiles();
}