package com.rymcu.mortise.voice.repository;

import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.voice.model.VoiceModelSearchCriteria;
import com.rymcu.mortise.voice.entity.VoiceModel;

import java.util.List;
import java.util.Optional;

/**
 * 语音模型仓储端口。
 */
public interface VoiceModelRepository {

    List<VoiceModel> findAll(Boolean enabledOnly);

    PageResult<VoiceModel> findModels(PageQuery pageQuery, VoiceModelSearchCriteria criteria);

    List<Long> findIdsByKeyword(String keyword);

    Optional<VoiceModel> findById(Long id);

    Optional<VoiceModel> findByCode(String code);

    boolean existsAnyByProviderId(Long providerId, Integer status);

    boolean existsDefaultModel(Long providerId, String capability, Long excludeId);

    boolean save(VoiceModel model);

    boolean update(VoiceModel model);

    boolean deleteById(Long id);

    boolean updateStatus(Long id, Integer status);
}