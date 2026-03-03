package com.rymcu.mortise.notification.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.notification.constant.NotificationChannelSchema;
import com.rymcu.mortise.notification.entity.NotificationChannelConfig;
import com.rymcu.mortise.common.enumerate.FormFieldType;
import com.rymcu.mortise.notification.enums.NotificationType;
import com.rymcu.mortise.notification.mapper.NotificationChannelConfigMapper;
import com.rymcu.mortise.notification.model.ChannelConfigSaveRequest;
import com.rymcu.mortise.notification.model.ChannelConfigVO;
import com.rymcu.mortise.notification.service.NotificationChannelConfigService;
import com.rymcu.mortise.notification.spi.NotificationChannelConfigProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.rymcu.mortise.notification.entity.table.NotificationChannelConfigTableDef.NOTIFICATION_CHANNEL_CONFIG;

/**
 * 通知渠道配置服务实现
 * <p>
 * 同时实现 {@link NotificationChannelConfigService}（管理端操作）
 * 与 {@link NotificationChannelConfigProvider}（Sender 运行时读取配置）。
 * 通过 Spring Cache 对 DB 查询结果进行缓存，降低频繁发通知时的 DB 压力。
 *
 * @author ronger
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationChannelConfigServiceImpl
        implements NotificationChannelConfigService, NotificationChannelConfigProvider {

    private final NotificationChannelConfigMapper mapper;

    // ─── NotificationChannelConfigProvider ────────────────────────────────────

    @Override
    @Cacheable(cacheNames = "notification:channel:config", key = "#type.code")
    public Map<String, String> getConfig(NotificationType type) {
        return loadFromDb(type.getCode());
    }

    @Override
    public Optional<String> getValue(NotificationType type, String key) {
        return Optional.ofNullable(getConfig(type).get(key));
    }

    @Override
    public boolean isEnabled(NotificationType type) {
        return "true".equalsIgnoreCase(getConfig(type).getOrDefault("enabled", "false"));
    }

    @Override
    @CacheEvict(cacheNames = "notification:channel:config", key = "#type.code")
    public void refreshCache(NotificationType type) {
        log.info("刷新通知渠道配置缓存: channel={}", type.getCode());
    }

    // ─── NotificationChannelConfigService ─────────────────────────────────────

    @Override
    public List<ChannelConfigVO> listAllChannels() {
        return Arrays.stream(NotificationChannelSchema.values())
                .map(this::buildVO)
                .toList();
    }

    @Override
    public ChannelConfigVO getChannel(String channel) {
        NotificationType type = NotificationType.fromCode(channel)
                .orElseThrow(() -> new IllegalArgumentException("未知渠道: " + channel));
        return buildVO(NotificationChannelSchema.fromType(type));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveChannel(String channel, ChannelConfigSaveRequest request) {
        NotificationType type = NotificationType.fromCode(channel)
                .orElseThrow(() -> new IllegalArgumentException("未知渠道: " + channel));
        String channelCode = type.getCode();
        LocalDateTime now = LocalDateTime.now();

        // 先删除该渠道所有旧配置行
        QueryWrapper deleteWrapper = QueryWrapper.create()
                .where(NOTIFICATION_CHANNEL_CONFIG.CHANNEL.eq(channelCode));
        mapper.deleteByQuery(deleteWrapper);

        // 写入 enabled 标记
        mapper.insert(buildConfigRow(channelCode, "enabled", String.valueOf(request.enabled()), now));

        // 写入各字段值
        if (request.values() != null) {
            request.values().forEach((key, value) ->
                    mapper.insert(buildConfigRow(channelCode, key, value, now)));
        }

        // 主动刷新缓存
        refreshCache(type);
        log.info("保存通知渠道配置成功: channel={}, enabled={}", channelCode, request.enabled());
    }

    // ─── 私有辅助方法 ──────────────────────────────────────────────────────────

    /**
     * 从数据库加载指定渠道的配置，聚合为 key → value Map
     */
    private Map<String, String> loadFromDb(String channelCode) {
        QueryWrapper qw = QueryWrapper.create()
                .where(NOTIFICATION_CHANNEL_CONFIG.CHANNEL.eq(channelCode));
        return mapper.selectListByQuery(qw).stream()
                .collect(Collectors.toMap(
                        NotificationChannelConfig::getConfigKey,
                        c -> Objects.toString(c.getConfigValue(), "")
                ));
    }

    /**
     * 将 Schema + DB 值聚合为前端所需的 VO（密码字段脱敏）
     */
    private ChannelConfigVO buildVO(NotificationChannelSchema schema) {
        NotificationType type = schema.getType();
        Map<String, String> rawValues = loadFromDb(type.getCode());

        boolean enabled = "true".equalsIgnoreCase(rawValues.getOrDefault("enabled", "false"));

        // 过滤内部字段，不暴露给前端
        Map<String, String> displayValues = new HashMap<>(rawValues);
        displayValues.remove("enabled");

        // 密码字段脱敏：仅当 DB 中已存有值时才替换为 "***"
        schema.getFields().stream()
                .filter(f -> f.type() == FormFieldType.PASSWORD
                        && displayValues.containsKey(f.key())
                        && !displayValues.get(f.key()).isBlank())
                .forEach(f -> displayValues.put(f.key(), "***"));

        return new ChannelConfigVO(
                type.getCode(),
                type.getDescription(),
                enabled,
                schema.getFields(),
                displayValues
        );
    }

    /**
     * 构造一条配置记录
     */
    private NotificationChannelConfig buildConfigRow(
            String channel, String key, String value, LocalDateTime now) {
        NotificationChannelConfig config = new NotificationChannelConfig();
        config.setChannel(channel);
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setCreatedTime(now);
        config.setUpdatedTime(now);
        return config;
    }
}
