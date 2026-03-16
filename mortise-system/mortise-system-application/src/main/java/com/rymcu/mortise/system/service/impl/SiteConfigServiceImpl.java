package com.rymcu.mortise.system.service.impl;

import com.rymcu.mortise.persistence.systemconfig.service.SystemConfigStorageService;
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
 *
 * @author ronger
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SiteConfigServiceImpl implements SiteConfigService {

    private final SystemConfigStorageService systemConfigStorageService;

    // ─── SiteConfigService ────────────────────────────────────────────────────

    @Override
    public List<SiteConfigGroupVO> listAllGroups() {
        return Arrays.stream(SiteConfigSchema.values())
                .map(schema -> buildGroupVO(schema, loadFromDb(schema.getGroup())))
                .toList();
    }

    @Override
    @Cacheable(cacheNames = SystemCacheConstant.SITE_CONFIG_GROUP_CACHE, key = "#group")
    public SiteConfigGroupVO getGroup(String group) {
        var schema = SiteConfigSchema.ofGroup(group);
        return buildGroupVO(schema, loadFromDb(group));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(cacheNames = SystemCacheConstant.SITE_CONFIG_GROUP_CACHE, key = "#group"),
            @CacheEvict(cacheNames = SystemCacheConstant.SITE_CONFIG_PUBLIC_CACHE, allEntries = true)
    })
    public void saveGroup(String group, SiteConfigSaveRequest request) {
        // 校验分组合法性
        var schema = SiteConfigSchema.ofGroup(group);
        var incoming = request.values() != null ? request.values() : Map.<String, String>of();

        // 按 Schema 字段顺序逐一 upsert
        for (SiteConfigFieldDef field : schema.getFields()) {
            var value = incoming.get(field.key());
            if (value == null) {
                continue; // 未传该字段则保持原值不变
            }
            systemConfigStorageService.upsertValue(group, field.key(), value);
        }

        log.info("保存网站配置成功: group={}", group);
    }

    @Override
    @Cacheable(cacheNames = SystemCacheConstant.SITE_CONFIG_PUBLIC_CACHE, key = "'public'")
    public SiteConfigPublicVO getPublicConfig() {
        // 聚合所有分组的所有配置项
        var allValues = Arrays.stream(SiteConfigSchema.values())
                .flatMap(schema -> loadFromDb(schema.getGroup()).entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b));
        return new SiteConfigPublicVO(allValues);
    }

    // ─── 私有辅助方法 ──────────────────────────────────────────────────────────

    /**
     * 从数据库加载指定分组的所有配置项（key → value）
     */
    private Map<String, String> loadFromDb(String group) {
        return systemConfigStorageService.loadGroupValues(group);
    }

    /**
     * 构建配置分组视图对象
     */
    private SiteConfigGroupVO buildGroupVO(SiteConfigSchema schema, Map<String, String> dbValues) {
        // 用 defaultValue 填充数据库中不存在的字段
        var values = schema.getFields().stream()
                .collect(Collectors.toMap(
                        SiteConfigFieldDef::key,
                        f -> dbValues.getOrDefault(f.key(),
                                Objects.toString(f.defaultValue(), ""))
                ));
        return new SiteConfigGroupVO(schema.getGroup(), schema.getLabel(), schema.getFields(), values);
    }

}
