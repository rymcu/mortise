package com.rymcu.mortise.voice.infra.repository;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.voice.entity.VoiceProfile;
import com.rymcu.mortise.voice.infra.persistence.FlexPageMapper;
import com.rymcu.mortise.voice.infra.persistence.entity.VoiceProfilePO;
import com.rymcu.mortise.voice.mapper.VoiceProfileMapper;
import com.rymcu.mortise.voice.model.VoiceProfileSearchCriteria;
import com.rymcu.mortise.voice.repository.VoiceProfileRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.rymcu.mortise.voice.infra.persistence.entity.table.VoiceProfilePOTableDef.VOICE_PROFILE_PO;

/**
 * 语音配置仓储实现。
 */
@Repository
public class VoiceProfileRepositoryImpl implements VoiceProfileRepository {

    private final VoiceProfileMapper voiceProfileMapper;

    public VoiceProfileRepositoryImpl(VoiceProfileMapper voiceProfileMapper) {
        this.voiceProfileMapper = voiceProfileMapper;
    }

    @Override
    public List<VoiceProfile> findAll(Boolean enabledOnly) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .orderBy(VOICE_PROFILE_PO.SORT_NO.asc(), VOICE_PROFILE_PO.ID.desc());
        if (Boolean.TRUE.equals(enabledOnly)) {
            queryWrapper.and(VOICE_PROFILE_PO.STATUS.eq(Status.ENABLED.getCode()));
        }
        return voiceProfileMapper.selectListByQuery(queryWrapper).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PageResult<VoiceProfile> findProfiles(PageQuery pageQuery, VoiceProfileSearchCriteria criteria) {
        QueryWrapper queryWrapper = QueryWrapper.create()
            .orderBy(VOICE_PROFILE_PO.SORT_NO.asc(), VOICE_PROFILE_PO.ID.desc());
        if (criteria != null) {
            if (criteria.status() != null) {
            queryWrapper.and(VOICE_PROFILE_PO.STATUS.eq(criteria.status()));
            }
            if (StringUtils.hasText(criteria.query())) {
            queryWrapper.and(VOICE_PROFILE_PO.NAME.like(criteria.query())
                .or(VOICE_PROFILE_PO.CODE.like(criteria.query()))
                .or(VOICE_PROFILE_PO.LANGUAGE.like(criteria.query()))
                .or(VOICE_PROFILE_PO.REMARK.like(criteria.query()))
                        .or(hasProviderReference(criteria.providerIds())
                    ? VOICE_PROFILE_PO.ASR_PROVIDER_ID.in(criteria.providerIds())
                    .or(VOICE_PROFILE_PO.VAD_PROVIDER_ID.in(criteria.providerIds()))
                    .or(VOICE_PROFILE_PO.TTS_PROVIDER_ID.in(criteria.providerIds()))
                    : VOICE_PROFILE_PO.ID.isNull())
                        .or(hasModelReference(criteria.modelIds())
                    ? VOICE_PROFILE_PO.ASR_MODEL_ID.in(criteria.modelIds())
                    .or(VOICE_PROFILE_PO.VAD_MODEL_ID.in(criteria.modelIds()))
                    .or(VOICE_PROFILE_PO.TTS_MODEL_ID.in(criteria.modelIds()))
                    : VOICE_PROFILE_PO.ID.isNull()));
            }
        }
        Page<VoiceProfilePO> page = voiceProfileMapper.paginate(FlexPageMapper.toFlexPage(pageQuery), queryWrapper);
        return FlexPageMapper.toPageResult(page, this::toDomain);
    }

    @Override
    public Optional<VoiceProfile> findById(Long id) {
        return Optional.ofNullable(voiceProfileMapper.selectOneById(id)).map(this::toDomain);
    }

    @Override
    public Optional<VoiceProfile> findByCode(String code) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(VOICE_PROFILE_PO.CODE.eq(code));
        return Optional.ofNullable(voiceProfileMapper.selectOneByQuery(queryWrapper)).map(this::toDomain);
    }

    @Override
    public boolean existsAnyByProviderId(Long providerId, Integer status) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(VOICE_PROFILE_PO.ASR_PROVIDER_ID.eq(providerId)
                        .or(VOICE_PROFILE_PO.VAD_PROVIDER_ID.eq(providerId))
                        .or(VOICE_PROFILE_PO.TTS_PROVIDER_ID.eq(providerId)));
        if (status != null) {
            queryWrapper.and(VOICE_PROFILE_PO.STATUS.eq(status));
        }
        return voiceProfileMapper.selectCountByQuery(queryWrapper) > 0;
    }

    @Override
    public boolean existsAnyByModelId(Long modelId, Integer status) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(VOICE_PROFILE_PO.ASR_MODEL_ID.eq(modelId)
                        .or(VOICE_PROFILE_PO.VAD_MODEL_ID.eq(modelId))
                        .or(VOICE_PROFILE_PO.TTS_MODEL_ID.eq(modelId)));
        if (status != null) {
            queryWrapper.and(VOICE_PROFILE_PO.STATUS.eq(status));
        }
        return voiceProfileMapper.selectCountByQuery(queryWrapper) > 0;
    }

    @Override
    public boolean save(VoiceProfile profile) {
        VoiceProfilePO profilePO = toPersistence(profile);
        boolean saved = voiceProfileMapper.insertSelective(profilePO) > 0;
        if (saved) {
            profile.setId(profilePO.getId());
        }
        return saved;
    }

    @Override
    public boolean update(VoiceProfile profile) {
        return voiceProfileMapper.update(toPersistence(profile)) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return voiceProfileMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        return UpdateChain.of(VoiceProfilePO.class)
                .set(VoiceProfilePO::getStatus, status)
                .where(VoiceProfilePO::getId).eq(id)
                .update();
    }

    private boolean hasProviderReference(List<Long> providerIds) {
        return providerIds != null && !providerIds.isEmpty();
    }

    private boolean hasModelReference(List<Long> modelIds) {
        return modelIds != null && !modelIds.isEmpty();
    }

    private VoiceProfilePO toPersistence(VoiceProfile profile) {
        VoiceProfilePO profilePO = new VoiceProfilePO();
        profilePO.setId(profile.getId());
        profilePO.setName(profile.getName());
        profilePO.setCode(profile.getCode());
        profilePO.setLanguage(profile.getLanguage());
        profilePO.setAsrProviderId(profile.getAsrProviderId());
        profilePO.setAsrModelId(profile.getAsrModelId());
        profilePO.setVadProviderId(profile.getVadProviderId());
        profilePO.setVadModelId(profile.getVadModelId());
        profilePO.setTtsProviderId(profile.getTtsProviderId());
        profilePO.setTtsModelId(profile.getTtsModelId());
        profilePO.setDefaultParams(profile.getDefaultParams());
        profilePO.setStatus(profile.getStatus());
        profilePO.setSortNo(profile.getSortNo());
        profilePO.setRemark(profile.getRemark());
        profilePO.setDelFlag(profile.getDelFlag());
        return profilePO;
    }

    private VoiceProfile toDomain(VoiceProfilePO po) {
        VoiceProfile profile = new VoiceProfile();
        profile.setId(po.getId());
        profile.setName(po.getName());
        profile.setCode(po.getCode());
        profile.setLanguage(po.getLanguage());
        profile.setAsrProviderId(po.getAsrProviderId());
        profile.setAsrModelId(po.getAsrModelId());
        profile.setVadProviderId(po.getVadProviderId());
        profile.setVadModelId(po.getVadModelId());
        profile.setTtsProviderId(po.getTtsProviderId());
        profile.setTtsModelId(po.getTtsModelId());
        profile.setDefaultParams(po.getDefaultParams());
        profile.setStatus(po.getStatus());
        profile.setSortNo(po.getSortNo());
        profile.setRemark(po.getRemark());
        profile.setDelFlag(po.getDelFlag());
        profile.setCreatedTime(po.getCreatedTime());
        profile.setUpdatedTime(po.getUpdatedTime());
        return profile;
    }
}