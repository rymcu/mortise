package com.rymcu.mortise.agent.infra.repository;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.rymcu.mortise.agent.entity.AiModel;
import com.rymcu.mortise.agent.infra.persistence.PersistenceObjectMapper;
import com.rymcu.mortise.agent.infra.persistence.FlexPageMapper;
import com.rymcu.mortise.agent.infra.persistence.entity.AiModelPO;
import com.rymcu.mortise.agent.mapper.AiModelMapper;
import com.rymcu.mortise.agent.model.AiModelSearchCriteria;
import com.rymcu.mortise.agent.repository.AiModelRepository;
import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.rymcu.mortise.agent.infra.persistence.entity.table.AiModelPOTableDef.AI_MODEL_PO;

/**
 * AI 模型仓储实现。
 */
@Repository
public class AiModelRepositoryImpl implements AiModelRepository {

    private final AiModelMapper aiModelMapper;

    public AiModelRepositoryImpl(AiModelMapper aiModelMapper) {
        this.aiModelMapper = aiModelMapper;
    }

    @Override
    public List<AiModel> listEnabledModelsByProviderId(Long providerId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(AI_MODEL_PO.PROVIDER_ID.eq(providerId))
                .and(AI_MODEL_PO.STATUS.eq(Status.ENABLED.getCode()))
                .orderBy(AI_MODEL_PO.SORT_NO.asc(), AI_MODEL_PO.CREATED_TIME.desc());
        return PersistenceObjectMapper.copyList(aiModelMapper.selectListByQuery(queryWrapper), AiModel::new);
    }

    @Override
    public PageResult<AiModel> findModels(PageQuery pageQuery, AiModelSearchCriteria criteria) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .orderBy(AI_MODEL_PO.SORT_NO.asc(), AI_MODEL_PO.CREATED_TIME.desc());
        if (criteria != null) {
            if (criteria.providerId() != null) {
                queryWrapper.and(AI_MODEL_PO.PROVIDER_ID.eq(criteria.providerId()));
            }
            if (StringUtils.isNotBlank(criteria.modelName())) {
                queryWrapper.and(AI_MODEL_PO.MODEL_NAME.like(criteria.modelName()));
            }
            if (criteria.status() != null) {
                queryWrapper.and(AI_MODEL_PO.STATUS.eq(criteria.status()));
            }
            if (StringUtils.isNotBlank(criteria.query())) {
                queryWrapper.and(AI_MODEL_PO.MODEL_NAME.like(criteria.query())
                        .or(AI_MODEL_PO.DISPLAY_NAME.like(criteria.query())));
            }
        }
        Page<AiModelPO> page = aiModelMapper.paginate(FlexPageMapper.toFlexPage(pageQuery), queryWrapper);
        return FlexPageMapper.toPageResult(page, modelPO -> PersistenceObjectMapper.copy(modelPO, AiModel::new));
    }

    @Override
    public AiModel findById(Long id) {
        return PersistenceObjectMapper.copy(aiModelMapper.selectOneById(id), AiModel::new);
    }

    @Override
    public boolean save(AiModel model) {
        AiModelPO modelPO = PersistenceObjectMapper.copy(model, AiModelPO::new);
        boolean saved = aiModelMapper.insertSelective(modelPO) > 0;
        if (saved) {
            model.setId(modelPO.getId());
        }
        return saved;
    }

    @Override
    public boolean update(AiModel model) {
        return aiModelMapper.update(PersistenceObjectMapper.copy(model, AiModelPO::new)) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return aiModelMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        return UpdateChain.of(AiModelPO.class)
                .set(AiModelPO::getStatus, status)
                .where(AiModelPO::getId).eq(id)
                .update();
    }
}
