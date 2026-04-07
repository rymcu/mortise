package com.rymcu.mortise.voice.repository;

import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.voice.model.VoiceProviderSearchCriteria;
import com.rymcu.mortise.voice.entity.VoiceProvider;

import java.util.List;
import java.util.Optional;

/**
 * 语音提供商仓储端口。
 */
public interface VoiceProviderRepository {

    List<VoiceProvider> findAll(Boolean enabledOnly);

    PageResult<VoiceProvider> findProviders(PageQuery pageQuery, VoiceProviderSearchCriteria criteria);

    List<Long> findIdsByKeyword(String keyword);

    Optional<VoiceProvider> findById(Long id);

    Optional<VoiceProvider> findByCode(String code);

    boolean save(VoiceProvider provider);

    boolean update(VoiceProvider provider);

    boolean deleteById(Long id);

    boolean updateStatus(Long id, Integer status);
}