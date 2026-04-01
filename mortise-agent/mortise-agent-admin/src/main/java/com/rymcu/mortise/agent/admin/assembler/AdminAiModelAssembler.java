package com.rymcu.mortise.agent.admin.assembler;

import com.rymcu.mortise.agent.admin.contract.query.AiModelSearch;
import com.rymcu.mortise.agent.admin.contract.request.AiModelUpsertRequest;
import com.rymcu.mortise.agent.admin.contract.response.AiModelInfo;
import com.rymcu.mortise.agent.application.command.AiModelUpsertCommand;
import com.rymcu.mortise.agent.application.query.AiModelSearchQuery;
import com.rymcu.mortise.agent.application.result.AiModelResult;
import org.springframework.stereotype.Component;

@Component
public class AdminAiModelAssembler {

    public AiModelSearchQuery toSearchQuery(AiModelSearch search) {
        return new AiModelSearchQuery(
                search.getProviderId(),
                search.getModelName(),
                search.getQuery(),
                search.getStatus()
        );
    }

    public AiModelUpsertCommand toCommand(AiModelUpsertRequest request) {
        return new AiModelUpsertCommand(
                request.providerId(),
                request.modelName(),
                request.displayName(),
                request.status(),
                request.sortNo(),
                request.remark()
        );
    }

    public AiModelInfo toInfo(AiModelResult model) {
        if (model == null) {
            return null;
        }
        return new AiModelInfo(
                model.id(),
                model.providerId(),
                model.providerName(),
                model.modelName(),
                model.displayName(),
                model.status(),
                model.sortNo(),
                model.remark(),
                model.createdTime(),
                model.updatedTime()
        );
    }
}
