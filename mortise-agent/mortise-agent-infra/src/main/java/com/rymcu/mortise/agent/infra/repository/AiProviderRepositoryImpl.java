package com.rymcu.mortise.agent.infra.repository;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.rymcu.mortise.agent.entity.AiProvider;
import com.rymcu.mortise.agent.infra.persistence.PersistenceObjectMapper;
import com.rymcu.mortise.agent.infra.persistence.FlexPageMapper;
import com.rymcu.mortise.agent.infra.persistence.entity.AiProviderPO;
import com.rymcu.mortise.agent.mapper.AiProviderMapper;
import com.rymcu.mortise.agent.model.AiProviderSearchCriteria;
import com.rymcu.mortise.agent.repository.AiProviderRepository;
import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.rymcu.mortise.agent.infra.persistence.entity.table.AiProviderPOTableDef.AI_PROVIDER_PO;

/**
 * AI 提供商仓储实现。
 */
@Repository
public class AiProviderRepositoryImpl implements AiProviderRepository {

    private final AiProviderMapper aiProviderMapper;

    public AiProviderRepositoryImpl(AiProviderMapper aiProviderMapper) {
        this.aiProviderMapper = aiProviderMapper;
    }

    @Override
    public List<AiProvider> listEnabledProviders() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(AI_PROVIDER_PO.STATUS.eq(Status.ENABLED.getCode()))
                .orderBy(AI_PROVIDER_PO.SORT_NO.asc(), AI_PROVIDER_PO.CREATED_TIME.desc());
        return PersistenceObjectMapper.copyList(aiProviderMapper.selectListByQuery(queryWrapper), AiProvider::new);
    }

    @Override
    public PageResult<AiProvider> findProviders(PageQuery pageQuery, AiProviderSearchCriteria criteria) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .orderBy(AI_PROVIDER_PO.SORT_NO.asc(), AI_PROVIDER_PO.CREATED_TIME.desc());
        if (criteria != null) {
            if (StringUtils.isNotBlank(criteria.name())) {
                queryWrapper.and(AI_PROVIDER_PO.NAME.like(criteria.name()));
            }
            if (StringUtils.isNotBlank(criteria.code())) {
                queryWrapper.and(AI_PROVIDER_PO.CODE.like(criteria.code()));
            }
            if (criteria.status() != null) {
                queryWrapper.and(AI_PROVIDER_PO.STATUS.eq(criteria.status()));
            }
            if (StringUtils.isNotBlank(criteria.query())) {
                queryWrapper.and(AI_PROVIDER_PO.NAME.like(criteria.query()).or(AI_PROVIDER_PO.CODE.like(criteria.query())));
            }
        }
        Page<AiProviderPO> page = aiProviderMapper.paginate(FlexPageMapper.toFlexPage(pageQuery), queryWrapper);
        return FlexPageMapper.toPageResult(page, providerPO -> PersistenceObjectMapper.copy(providerPO, AiProvider::new));
    }

    @Override
    public AiProvider findById(Long id) {
        return PersistenceObjectMapper.copy(aiProviderMapper.selectOneById(id), AiProvider::new);
    }

    @Override
    public boolean save(AiProvider provider) {
        AiProviderPO providerPO = PersistenceObjectMapper.copy(provider, AiProviderPO::new);
        boolean saved = aiProviderMapper.insertSelective(providerPO) > 0;
        if (saved) {
            provider.setId(providerPO.getId());
        }
        return saved;
    }

    @Override
    public boolean update(AiProvider provider) {
        return aiProviderMapper.update(PersistenceObjectMapper.copy(provider, AiProviderPO::new)) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return aiProviderMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        return UpdateChain.of(AiProviderPO.class)
                .set(AiProviderPO::getStatus, status)
                .where(AiProviderPO::getId).eq(id)
                .update();
    }
}
