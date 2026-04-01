package com.rymcu.mortise.persistence.systemconfig.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.core.spi.SystemConfigStorage;
import com.rymcu.mortise.persistence.systemconfig.entity.SystemConfig;
import com.rymcu.mortise.persistence.systemconfig.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.rymcu.mortise.persistence.systemconfig.entity.table.SystemConfigTableDef.SYSTEM_CONFIG;

/**
 * 系统配置存储的数据库实现。
 */
@Service
@RequiredArgsConstructor
public class SystemConfigStorageService implements SystemConfigStorage {

    private final SystemConfigMapper systemConfigMapper;

    public Map<String, String> loadGroupValues(String group) {
        return systemConfigMapper.selectListByQuery(
                        QueryWrapper.create().where(SYSTEM_CONFIG.CONFIG_GROUP.eq(group)))
                .stream()
                .collect(Collectors.toMap(
                        SystemConfig::getConfigKey,
                        config -> Objects.toString(config.getConfigValue(), ""),
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
    }

    public void upsertValue(String group, String key, String value) {
        LocalDateTime now = LocalDateTime.now();
        SystemConfig existing = systemConfigMapper.selectOneByQuery(
                QueryWrapper.create().where(
                        SYSTEM_CONFIG.CONFIG_GROUP.eq(group)
                                .and(SYSTEM_CONFIG.CONFIG_KEY.eq(key))));
        if (existing == null) {
            SystemConfig config = new SystemConfig();
            config.setConfigGroup(group);
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setCreatedTime(now);
            config.setUpdatedTime(now);
            systemConfigMapper.insert(config);
            return;
        }
        existing.setConfigValue(value);
        existing.setUpdatedTime(now);
        systemConfigMapper.update(existing);
    }
}
