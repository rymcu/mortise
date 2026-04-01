package com.rymcu.mortise.agent.admin.assembler;

import com.rymcu.mortise.agent.admin.contract.query.AiProviderSearch;
import com.rymcu.mortise.agent.admin.contract.request.AiProviderUpsertRequest;
import com.rymcu.mortise.agent.admin.contract.response.AiProviderInfo;
import com.rymcu.mortise.agent.application.command.AiProviderUpsertCommand;
import com.rymcu.mortise.agent.application.query.AiProviderSearchQuery;
import com.rymcu.mortise.agent.application.result.AiProviderResult;
import org.springframework.stereotype.Component;

@Component
public class AdminAiProviderAssembler {

    public AiProviderSearchQuery toSearchQuery(AiProviderSearch search) {
        return new AiProviderSearchQuery(
                search.getName(),
                search.getCode(),
                search.getQuery(),
                search.getStatus()
        );
    }

    public AiProviderUpsertCommand toCommand(AiProviderUpsertRequest request) {
        return new AiProviderUpsertCommand(
                request.name(),
                request.code(),
                request.apiKey(),
                request.baseUrl(),
                request.defaultModelName(),
                request.status(),
                request.sortNo(),
                request.remark()
        );
    }

    public AiProviderInfo toInfo(AiProviderResult provider) {
        if (provider == null) {
            return null;
        }
        return new AiProviderInfo(
                provider.id(),
                provider.name(),
                provider.code(),
                provider.baseUrl(),
                provider.defaultModelName(),
                provider.status(),
                provider.sortNo(),
                provider.remark(),
                provider.createdTime(),
                provider.updatedTime()
        );
    }
}
