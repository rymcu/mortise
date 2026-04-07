package com.rymcu.mortise.voice.admin.assembler;

import com.rymcu.mortise.voice.admin.contract.query.VoiceModelSearch;
import com.rymcu.mortise.voice.admin.contract.query.VoiceProfileSearch;
import com.rymcu.mortise.voice.admin.contract.query.VoiceProviderSearch;
import com.rymcu.mortise.voice.admin.contract.request.VoiceModelUpsertRequest;
import com.rymcu.mortise.voice.admin.contract.request.VoiceProfileUpsertRequest;
import com.rymcu.mortise.voice.admin.contract.request.VoiceProviderUpsertRequest;
import com.rymcu.mortise.voice.admin.contract.response.VoiceModelInfo;
import com.rymcu.mortise.voice.admin.contract.response.VoiceProfileInfo;
import com.rymcu.mortise.voice.admin.contract.response.VoiceProviderInfo;
import com.rymcu.mortise.voice.admin.contract.response.VoiceRuntimeNodeInfo;
import com.rymcu.mortise.voice.application.command.VoiceModelUpsertCommand;
import com.rymcu.mortise.voice.application.command.VoiceProfileUpsertCommand;
import com.rymcu.mortise.voice.application.command.VoiceProviderUpsertCommand;
import com.rymcu.mortise.voice.application.query.VoiceModelSearchQuery;
import com.rymcu.mortise.voice.application.query.VoiceProfileSearchQuery;
import com.rymcu.mortise.voice.application.query.VoiceProviderSearchQuery;
import com.rymcu.mortise.voice.application.result.VoiceModelResult;
import com.rymcu.mortise.voice.application.result.VoiceProfileResult;
import com.rymcu.mortise.voice.application.result.VoiceProviderResult;
import com.rymcu.mortise.voice.kernel.model.VoiceRuntimeNodeStatus;
import org.springframework.stereotype.Component;

/**
 * 管理端语音目录转换器。
 */
@Component
public class AdminVoiceCatalogAssembler {

    public VoiceProviderSearchQuery toSearchQuery(VoiceProviderSearch search) {
        if (search == null) {
            return null;
        }
        return new VoiceProviderSearchQuery(search.getQuery(), search.getStatus());
    }

    public VoiceModelSearchQuery toSearchQuery(VoiceModelSearch search) {
        if (search == null) {
            return null;
        }
        return new VoiceModelSearchQuery(search.getQuery(), search.getStatus(), search.getProviderId(), search.getCapability());
    }

    public VoiceProfileSearchQuery toSearchQuery(VoiceProfileSearch search) {
        if (search == null) {
            return null;
        }
        return new VoiceProfileSearchQuery(search.getQuery(), search.getStatus());
    }

    public VoiceProviderInfo toInfo(VoiceProviderResult result) {
        return new VoiceProviderInfo(
                result.id(),
                result.name(),
                result.code(),
                result.providerType(),
                result.status(),
                result.sortNo(),
                result.defaultConfig(),
                result.remark(),
                result.createdTime(),
                result.updatedTime()
        );
    }

    public VoiceModelInfo toInfo(VoiceModelResult result) {
        return new VoiceModelInfo(
                result.id(),
                result.providerId(),
                result.name(),
                result.code(),
                result.capability(),
                result.modelType(),
                result.runtimeName(),
                result.version(),
                result.language(),
                result.status(),
                result.concurrencyLimit(),
                result.defaultModel(),
                result.remark(),
                result.createdTime(),
                result.updatedTime()
        );
    }

    public VoiceProfileInfo toInfo(VoiceProfileResult result) {
        return new VoiceProfileInfo(
                result.id(),
                result.name(),
                result.code(),
                result.language(),
                result.asrProviderId(),
                result.asrModelId(),
                result.vadProviderId(),
                result.vadModelId(),
                result.ttsProviderId(),
                result.ttsModelId(),
                result.defaultParams(),
                result.status(),
                result.sortNo(),
                result.remark(),
                result.createdTime(),
                result.updatedTime()
        );
    }

    public VoiceProviderUpsertCommand toCommand(VoiceProviderUpsertRequest request) {
        return new VoiceProviderUpsertCommand(
                request.name(),
                request.code(),
                request.providerType(),
                request.status(),
                request.sortNo(),
                request.defaultConfig(),
                request.remark()
        );
    }

    public VoiceModelUpsertCommand toCommand(VoiceModelUpsertRequest request) {
        return new VoiceModelUpsertCommand(
                request.providerId(),
                request.name(),
                request.code(),
                request.capability(),
                request.modelType(),
                request.runtimeName(),
                request.version(),
                request.language(),
                request.concurrencyLimit(),
                request.defaultModel(),
                request.status(),
                request.remark()
        );
    }

    public VoiceProfileUpsertCommand toCommand(VoiceProfileUpsertRequest request) {
        return new VoiceProfileUpsertCommand(
                request.name(),
                request.code(),
                request.language(),
                request.asrProviderId(),
                request.asrModelId(),
                request.vadProviderId(),
                request.vadModelId(),
                request.ttsProviderId(),
                request.ttsModelId(),
                request.defaultParams(),
                request.status(),
                request.sortNo(),
                request.remark()
        );
    }

    public VoiceRuntimeNodeInfo toInfo(VoiceRuntimeNodeStatus status) {
        return new VoiceRuntimeNodeInfo(
                status.nodeId(),
                status.baseUrl(),
                status.configStatus(),
                status.probeStatus(),
                status.detail(),
                status.latencyMillis(),
                status.checkedTime(),
                status.loadedModels()
        );
    }
}