package com.rymcu.mortise.voice.infra.repository;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.voice.entity.VoiceProvider;
import com.rymcu.mortise.voice.infra.persistence.FlexPageMapper;
import com.rymcu.mortise.voice.infra.persistence.entity.VoiceProviderPO;
import com.rymcu.mortise.voice.mapper.VoiceProviderMapper;
import com.rymcu.mortise.voice.model.VoiceProviderSearchCriteria;
import com.rymcu.mortise.voice.repository.VoiceProviderRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.rymcu.mortise.voice.infra.persistence.entity.table.VoiceProviderPOTableDef.VOICE_PROVIDER_PO;

/**
 * 语音提供商仓储实现。
 */
@Repository
public class VoiceProviderRepositoryImpl implements VoiceProviderRepository {

    private final VoiceProviderMapper voiceProviderMapper;

    public VoiceProviderRepositoryImpl(VoiceProviderMapper voiceProviderMapper) {
        this.voiceProviderMapper = voiceProviderMapper;
    }

    @Override
    public List<VoiceProvider> findAll(Boolean enabledOnly) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .orderBy(VOICE_PROVIDER_PO.SORT_NO.asc(), VOICE_PROVIDER_PO.ID.desc());
        if (Boolean.TRUE.equals(enabledOnly)) {
            queryWrapper.and(VOICE_PROVIDER_PO.STATUS.eq(Status.ENABLED.getCode()));
        }
        return voiceProviderMapper.selectListByQuery(queryWrapper).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PageResult<VoiceProvider> findProviders(PageQuery pageQuery, VoiceProviderSearchCriteria criteria) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .orderBy(VOICE_PROVIDER_PO.SORT_NO.asc(), VOICE_PROVIDER_PO.ID.desc());
        if (criteria != null) {
            if (criteria.status() != null) {
                queryWrapper.and(VOICE_PROVIDER_PO.STATUS.eq(criteria.status()));
            }
            if (StringUtils.hasText(criteria.query())) {
                queryWrapper.and(VOICE_PROVIDER_PO.NAME.like(criteria.query())
                        .or(VOICE_PROVIDER_PO.CODE.like(criteria.query()))
                        .or(VOICE_PROVIDER_PO.PROVIDER_TYPE.like(criteria.query()))
                        .or(VOICE_PROVIDER_PO.REMARK.like(criteria.query())));
            }
        }
        Page<VoiceProviderPO> page = voiceProviderMapper.paginate(FlexPageMapper.toFlexPage(pageQuery), queryWrapper);
        return FlexPageMapper.toPageResult(page, this::toDomain);
    }

    @Override
    public List<Long> findIdsByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        return voiceProviderMapper.selectListByQuery(QueryWrapper.create()
                        .where(VOICE_PROVIDER_PO.NAME.like(keyword).or(VOICE_PROVIDER_PO.CODE.like(keyword))))
                .stream()
                .map(VoiceProviderPO::getId)
                .toList();
    }

    @Override
    public Optional<VoiceProvider> findById(Long id) {
        return Optional.ofNullable(voiceProviderMapper.selectOneById(id)).map(this::toDomain);
    }

    @Override
    public Optional<VoiceProvider> findByCode(String code) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(VOICE_PROVIDER_PO.CODE.eq(code));
        return Optional.ofNullable(voiceProviderMapper.selectOneByQuery(queryWrapper)).map(this::toDomain);
    }

    @Override
    public boolean save(VoiceProvider provider) {
        VoiceProviderPO providerPO = toPersistence(provider);
        boolean saved = voiceProviderMapper.insertSelective(providerPO) > 0;
        if (saved) {
            provider.setId(providerPO.getId());
        }
        return saved;
    }

    @Override
    public boolean update(VoiceProvider provider) {
        return voiceProviderMapper.update(toPersistence(provider)) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return voiceProviderMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        return UpdateChain.of(VoiceProviderPO.class)
                .set(VoiceProviderPO::getStatus, status)
                .where(VoiceProviderPO::getId).eq(id)
                .update();
    }

    private VoiceProviderPO toPersistence(VoiceProvider provider) {
        VoiceProviderPO providerPO = new VoiceProviderPO();
        providerPO.setId(provider.getId());
        providerPO.setName(provider.getName());
        providerPO.setCode(provider.getCode());
        providerPO.setProviderType(provider.getProviderType());
        providerPO.setStatus(provider.getStatus());
        providerPO.setSortNo(provider.getSortNo());
        providerPO.setDefaultConfig(provider.getDefaultConfig());
        providerPO.setRemark(provider.getRemark());
        providerPO.setDelFlag(provider.getDelFlag());
        return providerPO;
    }

    private VoiceProvider toDomain(VoiceProviderPO po) {
        VoiceProvider provider = new VoiceProvider();
        provider.setId(po.getId());
        provider.setName(po.getName());
        provider.setCode(po.getCode());
        provider.setProviderType(po.getProviderType());
        provider.setStatus(po.getStatus());
        provider.setSortNo(po.getSortNo());
        provider.setDefaultConfig(po.getDefaultConfig());
        provider.setRemark(po.getRemark());
        provider.setDelFlag(po.getDelFlag());
        provider.setCreatedTime(po.getCreatedTime());
        provider.setUpdatedTime(po.getUpdatedTime());
        return provider;
    }
}