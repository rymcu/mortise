package com.rymcu.mortise.agent.admin.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.agent.admin.model.AiProviderInfo;
import com.rymcu.mortise.agent.admin.model.AiProviderSearch;
import com.rymcu.mortise.agent.admin.service.AdminAiProviderService;
import com.rymcu.mortise.agent.entity.AiProvider;
import com.rymcu.mortise.agent.service.impl.AiProviderServiceImpl;
import com.rymcu.mortise.common.enumerate.Status;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.rymcu.mortise.agent.entity.table.AiProviderTableDef.AI_PROVIDER;

/**
 * AI 提供商管理服务实现
 *
 * @author ronger
 */
@Service
public class AdminAiProviderServiceImpl extends AiProviderServiceImpl implements AdminAiProviderService {

    @Override
    public Page<AiProviderInfo> findProviderList(Page<AiProviderInfo> page, AiProviderSearch search) {
        QueryWrapper queryWrapper = buildQueryWrapper(search);
        Page<AiProvider> providerPage = new Page<>(page.getPageNumber(), page.getPageSize());
        providerPage = this.page(providerPage, queryWrapper);

        Page<AiProviderInfo> resultPage = new Page<>(page.getPageNumber(), page.getPageSize());
        resultPage.setTotalRow(providerPage.getTotalRow());
        resultPage.setRecords(providerPage.getRecords().stream()
                .map(this::convertToInfo)
                .toList());
        return resultPage;
    }

    @Override
    public AiProviderInfo findProviderInfoById(Long id) {
        AiProvider provider = this.getById(id);
        if (provider == null) {
            return null;
        }
        return convertToInfo(provider);
    }

    @Override
    public Boolean enableProvider(Long id) {
        return updateStatus(id, Status.ENABLED.getCode());
    }

    @Override
    public Boolean disableProvider(Long id) {
        return updateStatus(id, Status.DISABLED.getCode());
    }

    @Override
    public Boolean updateStatus(Long id, Integer status) {
        AiProvider provider = new AiProvider();
        provider.setId(id);
        provider.setStatus(status);
        return this.updateById(provider);
    }

    private QueryWrapper buildQueryWrapper(AiProviderSearch search) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .orderBy(AI_PROVIDER.SORT_NO.asc(), AI_PROVIDER.CREATED_TIME.desc());

        if (StringUtils.isNotBlank(search.getName())) {
            queryWrapper.and(AI_PROVIDER.NAME.like(search.getName()));
        }
        if (StringUtils.isNotBlank(search.getCode())) {
            queryWrapper.and(AI_PROVIDER.CODE.like(search.getCode()));
        }
        if (Objects.nonNull(search.getStatus())) {
            queryWrapper.and(AI_PROVIDER.STATUS.eq(search.getStatus()));
        }
        if (StringUtils.isNotBlank(search.getQuery())) {
            queryWrapper.and(AI_PROVIDER.NAME.like(search.getQuery())
                    .or(AI_PROVIDER.CODE.like(search.getQuery())));
        }
        return queryWrapper;
    }

    private AiProviderInfo convertToInfo(AiProvider provider) {
        return new AiProviderInfo(
                provider.getId(),
                provider.getName(),
                provider.getCode(),
                provider.getBaseUrl(),
                provider.getDefaultModelName(),
                provider.getStatus(),
                provider.getSortNo(),
                provider.getRemark(),
                provider.getCreatedTime(),
                provider.getUpdatedTime()
        );
    }
}
