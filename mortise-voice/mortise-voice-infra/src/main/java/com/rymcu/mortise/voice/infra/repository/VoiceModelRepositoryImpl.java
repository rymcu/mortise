package com.rymcu.mortise.voice.infra.repository;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.voice.entity.VoiceModel;
import com.rymcu.mortise.voice.infra.persistence.FlexPageMapper;
import com.rymcu.mortise.voice.infra.persistence.entity.VoiceModelPO;
import com.rymcu.mortise.voice.mapper.VoiceModelMapper;
import com.rymcu.mortise.voice.model.VoiceModelSearchCriteria;
import com.rymcu.mortise.voice.repository.VoiceModelRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.rymcu.mortise.voice.infra.persistence.entity.table.VoiceModelPOTableDef.VOICE_MODEL_PO;

/**
 * 语音模型仓储实现。
 */
@Repository
public class VoiceModelRepositoryImpl implements VoiceModelRepository {

    private final VoiceModelMapper voiceModelMapper;

    public VoiceModelRepositoryImpl(VoiceModelMapper voiceModelMapper) {
        this.voiceModelMapper = voiceModelMapper;
    }

    @Override
    public List<VoiceModel> findAll(Boolean enabledOnly) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .orderBy(VOICE_MODEL_PO.ID.desc());
        if (Boolean.TRUE.equals(enabledOnly)) {
            queryWrapper.and(VOICE_MODEL_PO.STATUS.eq(Status.ENABLED.getCode()));
        }
        return voiceModelMapper.selectListByQuery(queryWrapper).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PageResult<VoiceModel> findModels(PageQuery pageQuery, VoiceModelSearchCriteria criteria) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .orderBy(VOICE_MODEL_PO.ID.desc());
        if (criteria != null) {
            if (criteria.status() != null) {
                queryWrapper.and(VOICE_MODEL_PO.STATUS.eq(criteria.status()));
            }
            if (criteria.providerId() != null) {
                queryWrapper.and(VOICE_MODEL_PO.PROVIDER_ID.eq(criteria.providerId()));
            }
            if (StringUtils.hasText(criteria.capability())) {
                queryWrapper.and(VOICE_MODEL_PO.CAPABILITY.eq(criteria.capability()));
            }
            if (StringUtils.hasText(criteria.query())) {
                queryWrapper.and(VOICE_MODEL_PO.NAME.like(criteria.query())
                        .or(VOICE_MODEL_PO.CODE.like(criteria.query()))
                        .or(VOICE_MODEL_PO.RUNTIME_NAME.like(criteria.query()))
                        .or(VOICE_MODEL_PO.VERSION.like(criteria.query()))
                        .or(VOICE_MODEL_PO.LANGUAGE.like(criteria.query()))
                        .or(VOICE_MODEL_PO.REMARK.like(criteria.query())));
            }
        }
        Page<VoiceModelPO> page = voiceModelMapper.paginate(FlexPageMapper.toFlexPage(pageQuery), queryWrapper);
        return FlexPageMapper.toPageResult(page, this::toDomain);
    }

    @Override
    public List<Long> findIdsByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        return voiceModelMapper.selectListByQuery(QueryWrapper.create()
                        .where(VOICE_MODEL_PO.NAME.like(keyword).or(VOICE_MODEL_PO.CODE.like(keyword))))
                .stream()
                .map(VoiceModelPO::getId)
                .toList();
    }

    @Override
    public Optional<VoiceModel> findById(Long id) {
        return Optional.ofNullable(voiceModelMapper.selectOneById(id)).map(this::toDomain);
    }

    @Override
    public Optional<VoiceModel> findByCode(String code) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(VOICE_MODEL_PO.CODE.eq(code));
        return Optional.ofNullable(voiceModelMapper.selectOneByQuery(queryWrapper)).map(this::toDomain);
    }

    @Override
    public boolean existsAnyByProviderId(Long providerId, Integer status) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(VOICE_MODEL_PO.PROVIDER_ID.eq(providerId));
        if (status != null) {
            queryWrapper.and(VOICE_MODEL_PO.STATUS.eq(status));
        }
        return voiceModelMapper.selectCountByQuery(queryWrapper) > 0;
    }

    @Override
    public boolean existsDefaultModel(Long providerId, String capability, Long excludeId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(VOICE_MODEL_PO.PROVIDER_ID.eq(providerId))
                .and(VOICE_MODEL_PO.CAPABILITY.eq(capability))
                .and(VOICE_MODEL_PO.DEFAULT_MODEL.eq(Boolean.TRUE));
        if (excludeId != null) {
            queryWrapper.and(VOICE_MODEL_PO.ID.ne(excludeId));
        }
        return voiceModelMapper.selectCountByQuery(queryWrapper) > 0;
    }

    @Override
    public boolean save(VoiceModel model) {
        VoiceModelPO modelPO = toPersistence(model);
        boolean saved = voiceModelMapper.insertSelective(modelPO) > 0;
        if (saved) {
            model.setId(modelPO.getId());
        }
        return saved;
    }

    @Override
    public boolean update(VoiceModel model) {
        return voiceModelMapper.update(toPersistence(model)) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return voiceModelMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        return UpdateChain.of(VoiceModelPO.class)
                .set(VoiceModelPO::getStatus, status)
                .where(VoiceModelPO::getId).eq(id)
                .update();
    }

    private VoiceModelPO toPersistence(VoiceModel model) {
        VoiceModelPO modelPO = new VoiceModelPO();
        modelPO.setId(model.getId());
        modelPO.setProviderId(model.getProviderId());
        modelPO.setName(model.getName());
        modelPO.setCode(model.getCode());
        modelPO.setCapability(model.getCapability());
        modelPO.setModelType(model.getModelType());
        modelPO.setRuntimeName(model.getRuntimeName());
        modelPO.setVersion(model.getVersion());
        modelPO.setLanguage(model.getLanguage());
        modelPO.setConcurrencyLimit(model.getConcurrencyLimit());
        modelPO.setDefaultModel(model.getDefaultModel());
        modelPO.setStatus(model.getStatus());
        modelPO.setRemark(model.getRemark());
        modelPO.setDelFlag(model.getDelFlag());
        return modelPO;
    }

    private VoiceModel toDomain(VoiceModelPO po) {
        VoiceModel model = new VoiceModel();
        model.setId(po.getId());
        model.setProviderId(po.getProviderId());
        model.setName(po.getName());
        model.setCode(po.getCode());
        model.setCapability(po.getCapability());
        model.setModelType(po.getModelType());
        model.setRuntimeName(po.getRuntimeName());
        model.setVersion(po.getVersion());
        model.setLanguage(po.getLanguage());
        model.setConcurrencyLimit(po.getConcurrencyLimit());
        model.setDefaultModel(po.getDefaultModel());
        model.setStatus(po.getStatus());
        model.setRemark(po.getRemark());
        model.setDelFlag(po.getDelFlag());
        model.setCreatedTime(po.getCreatedTime());
        model.setUpdatedTime(po.getUpdatedTime());
        return model;
    }
}