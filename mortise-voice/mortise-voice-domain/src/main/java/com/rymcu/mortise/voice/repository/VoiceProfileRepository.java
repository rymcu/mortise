package com.rymcu.mortise.voice.repository;

import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.voice.model.VoiceProfileSearchCriteria;
import com.rymcu.mortise.voice.entity.VoiceProfile;

import java.util.List;
import java.util.Optional;

/**
 * 语音配置仓储端口。
 */
public interface VoiceProfileRepository {

    List<VoiceProfile> findAll(Boolean enabledOnly);

    PageResult<VoiceProfile> findProfiles(PageQuery pageQuery, VoiceProfileSearchCriteria criteria);

    Optional<VoiceProfile> findById(Long id);

    Optional<VoiceProfile> findByCode(String code);

    boolean existsAnyByProviderId(Long providerId, Integer status);

    boolean existsAnyByModelId(Long modelId, Integer status);

    boolean save(VoiceProfile profile);

    boolean update(VoiceProfile profile);

    boolean deleteById(Long id);

    boolean updateStatus(Long id, Integer status);
}