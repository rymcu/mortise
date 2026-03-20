package com.rymcu.mortise.agent.admin.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.agent.admin.model.AiModelInfo;
import com.rymcu.mortise.agent.admin.model.AiModelSearch;
import com.rymcu.mortise.agent.admin.service.AdminAiModelService;
import com.rymcu.mortise.agent.entity.AiModel;
import com.rymcu.mortise.agent.entity.AiProvider;
import com.rymcu.mortise.agent.service.AiProviderService;
import com.rymcu.mortise.agent.service.impl.AiModelServiceImpl;
import com.rymcu.mortise.common.enumerate.Status;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.rymcu.mortise.agent.entity.table.AiModelTableDef.AI_MODEL;

/**
 * AI 模型管理服务实现
 *
 * @author ronger
 */
@Service
public class AdminAiModelServiceImpl extends AiModelServiceImpl implements AdminAiModelService {

    private final AiProviderService aiProviderService;

    public AdminAiModelServiceImpl(@Qualifier("aiProviderServiceImpl") AiProviderService aiProviderService) {
        this.aiProviderService = aiProviderService;
    }

    @Override
    public Page<AiModelInfo> findModelList(Page<AiModelInfo> page, AiModelSearch search) {
        QueryWrapper queryWrapper = buildQueryWrapper(search);
        Page<AiModel> modelPage = new Page<>(page.getPageNumber(), page.getPageSize());
        modelPage = this.page(modelPage, queryWrapper);

        Page<AiModelInfo> resultPage = new Page<>(page.getPageNumber(), page.getPageSize());
        resultPage.setTotalRow(modelPage.getTotalRow());
        resultPage.setRecords(modelPage.getRecords().stream()
                .map(this::convertToInfo)
                .toList());
        return resultPage;
    }

    @Override
    public AiModelInfo findModelInfoById(Long id) {
        AiModel model = this.getById(id);
        if (model == null) {
            return null;
        }
        return convertToInfo(model);
    }

    @Override
    public Boolean enableModel(Long id) {
        return updateStatus(id, Status.ENABLED.getCode());
    }

    @Override
    public Boolean disableModel(Long id) {
        return updateStatus(id, Status.DISABLED.getCode());
    }

    @Override
    public Boolean updateStatus(Long id, Integer status) {
        AiModel model = new AiModel();
        model.setId(id);
        model.setStatus(status);
        return this.updateById(model);
    }

    private QueryWrapper buildQueryWrapper(AiModelSearch search) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .orderBy(AI_MODEL.SORT_NO.asc(), AI_MODEL.CREATED_TIME.desc());

        if (Objects.nonNull(search.getProviderId())) {
            queryWrapper.and(AI_MODEL.PROVIDER_ID.eq(search.getProviderId()));
        }
        if (StringUtils.isNotBlank(search.getModelName())) {
            queryWrapper.and(AI_MODEL.MODEL_NAME.like(search.getModelName()));
        }
        if (Objects.nonNull(search.getStatus())) {
            queryWrapper.and(AI_MODEL.STATUS.eq(search.getStatus()));
        }
        if (StringUtils.isNotBlank(search.getQuery())) {
            queryWrapper.and(AI_MODEL.MODEL_NAME.like(search.getQuery())
                    .or(AI_MODEL.DISPLAY_NAME.like(search.getQuery())));
        }
        return queryWrapper;
    }

    private AiModelInfo convertToInfo(AiModel model) {
        String providerName = null;
        if (model.getProviderId() != null) {
            AiProvider provider = aiProviderService.getById(model.getProviderId());
            if (provider != null) {
                providerName = provider.getName();
            }
        }
        return new AiModelInfo(
                model.getId(),
                model.getProviderId(),
                providerName,
                model.getModelName(),
                model.getDisplayName(),
                model.getStatus(),
                model.getSortNo(),
                model.getRemark(),
                model.getCreatedTime(),
                model.getUpdatedTime()
        );
    }
}
