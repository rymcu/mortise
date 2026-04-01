package com.rymcu.mortise.system.service.impl;

import com.rymcu.mortise.core.spi.SystemConfigStorage;
import com.rymcu.mortise.system.constant.SiteConfigSchema;
import com.rymcu.mortise.system.constant.SystemCacheConstant;
import com.rymcu.mortise.system.model.SiteConfigFieldDef;
import com.rymcu.mortise.system.model.SiteConfigGroupVO;
import com.rymcu.mortise.system.model.SiteConfigPublicVO;
import com.rymcu.mortise.system.model.SiteConfigSaveRequest;
import com.rymcu.mortise.system.service.SiteConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 网站配置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SiteConfigServiceImpl implements SiteConfigService {

    private final SystemConfigStorage systemConfigStorage;

    @Override
    public List<SiteConfigGroupVO> listAllGroups() {
        return Arrays.stream(SiteConfigSchema.values())
                .map(schema -> buildGroupVO(schema, loadFromDb(schema.getGroup())))
                .toList();
    }

    @Override
    @Cacheable(cacheNames = SystemCacheConstant.SITE_CONFIG_GROUP_CACHE, key = "#group")
    public SiteConfigGroupVO getGroup(String group) {
        SiteConfigSchema schema = SiteConfigSchema.ofGroup(group);
        return buildGroupVO(schema, loadFromDb(group));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(cacheNames = SystemCacheConstant.SITE_CONFIG_GROUP_CACHE, key = "#group"),
            @CacheEvict(cacheNames = SystemCacheConstant.SITE_CONFIG_PUBLIC_CACHE, allEntries = true)
    })
    public void saveGroup(String group, SiteConfigSaveRequest request) {
        SiteConfigSchema schema = SiteConfigSchema.ofGroup(group);
        Map<String, String> incoming = request.values() != null ? request.values() : Map.of();
        for (SiteConfigFieldDef field : schema.getFields()) {
            String value = incoming.get(field.key());
            if (value == null) {
                continue;
            }
            systemConfigStorage.upsertValue(group, field.key(), value);
        }
        log.info("保存网站配置成功: group={}", group);
    }

    @Override
    @Cacheable(cacheNames = SystemCacheConstant.SITE_CONFIG_PUBLIC_CACHE, key = "'public'")
    public SiteConfigPublicVO getPublicConfig() {
        Map<String, String> allValues = Arrays.stream(SiteConfigSchema.values())
                .flatMap(schema -> loadFromDb(schema.getGroup()).entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b));
        return new SiteConfigPublicVO(allValues);
    }

    private Map<String, String> loadFromDb(String group) {
        return systemConfigStorage.loadGroupValues(group);
    }

    private SiteConfigGroupVO buildGroupVO(SiteConfigSchema schema, Map<String, String> dbValues) {
        Map<String, String> values = schema.getFields().stream()
                .collect(Collectors.toMap(
                        SiteConfigFieldDef::key,
                        f -> dbValues.getOrDefault(f.key(), Objects.toString(f.defaultValue(), ""))
                ));
        return new SiteConfigGroupVO(schema.getGroup(), schema.getLabel(), schema.getFields(), values);
    }
}
