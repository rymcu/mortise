package com.rymcu.mortise.notification.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.notification.constant.NotificationChannelSchema;
import com.rymcu.mortise.notification.entity.NotificationChannelConfig;
import com.rymcu.mortise.common.enumerate.FormFieldType;
import com.rymcu.mortise.notification.enums.NotificationType;
import com.rymcu.mortise.notification.mapper.NotificationChannelConfigMapper;
import com.rymcu.mortise.notification.model.ChannelConfigSaveRequest;
import com.rymcu.mortise.notification.model.ChannelConfigVO;
import com.rymcu.mortise.notification.model.ChannelFieldDef;
import com.rymcu.mortise.notification.service.NotificationChannelConfigService;
import com.rymcu.mortise.notification.spi.NotificationChannelConfigProvider;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.rymcu.mortise.notification.entity.table.NotificationChannelConfigTableDef.NOTIFICATION_CHANNEL_CONFIG;

/**
 * 通知渠道配置服务实现
 * <p>
 * 同时实现 {@link NotificationChannelConfigService}（管理端操作）
 * 与 {@link NotificationChannelConfigProvider}（Sender 运行时读取配置）。
 * <p>
 * 敏感数据处理规则（与 OAuth2 客户端配置保持一致）：
 * <ul>
 *   <li>PASSWORD 类型字段写入 DB 时使用 {@code jasyptStringEncryptor} 加密</li>
 *   <li>{@code getConfig()} 返回值已解密，Sender 可直接使用</li>
 *   <li>{@code buildVO()} 返回给前端时统一脱敏为 {@code "***"}，不暴露加密串</li>
 *   <li>保存时若 PASSWORD 字段值为空白或 {@code "***"}，保留数据库中已有的加密值，不覆盖</li>
 * </ul>
 *
 * @author ronger
 */
@Slf4j
@Service
public class NotificationChannelConfigServiceImpl
        implements NotificationChannelConfigService, NotificationChannelConfigProvider {

    private final NotificationChannelConfigMapper mapper;
    private final StringEncryptor stringEncryptor;

    /**
     * 自我注入，解决 Spring AOP 代理的 self-invocation 问题。
     * {@link Cacheable} / {@link CacheEvict} 注解在直接调用 {@code this.method()} 时会被忽略，
     * 通过代理对象调用才能触发缓存拦截。
     */
    @Lazy
    @Autowired
    private NotificationChannelConfigServiceImpl self;

    public NotificationChannelConfigServiceImpl(
            NotificationChannelConfigMapper mapper,
            @Qualifier("jasyptStringEncryptor") StringEncryptor stringEncryptor) {
        this.mapper = mapper;
        this.stringEncryptor = stringEncryptor;
    }

    // ─── NotificationChannelConfigProvider ────────────────────────────────────

    /**
     * 获取渠道配置供 Sender 运行时使用，PASSWORD 字段已解密为明文。
     */
    @Override
    @Cacheable(cacheNames = "notification:channel:config", key = "#type.code")
    public Map<String, String> getConfig(NotificationType type) {
        Map<String, String> raw = loadFromDb(type.getCode());
        Set<String> passwordKeys = passwordKeysOf(type);
        // PASSWORD 字段解密，使 Sender 直接获得明文
        passwordKeys.forEach(key -> {
            String encrypted = raw.get(key);
            if (StringUtils.hasText(encrypted)) {
                raw.put(key, decryptValue(encrypted));
            }
        });
        return raw;
    }

    @Override
    public Optional<String> getValue(NotificationType type, String key) {
        // 通过 self 代理调用，确保 @Cacheable 生效
        return Optional.ofNullable(self.getConfig(type).get(key));
    }

    @Override
    public boolean isEnabled(NotificationType type) {
        // 通过 self 代理调用，确保 @Cacheable 生效
        return "true".equalsIgnoreCase(self.getConfig(type).getOrDefault("enabled", "false"));
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
        NotificationChannelSchema schema = NotificationChannelSchema.fromType(type);
        String channelCode = type.getCode();
        LocalDateTime now = LocalDateTime.now();

        // 1. 保存前先备份数据库中现有的 PASSWORD 加密值（delete 之前读取）
        Map<String, String> existingRaw = loadFromDb(channelCode);
        Set<String> passwordKeys = passwordKeysOf(schema);

        // 2. 删除该渠道所有旧配置行
        mapper.deleteByQuery(QueryWrapper.create()
                .where(NOTIFICATION_CHANNEL_CONFIG.CHANNEL.eq(channelCode)));

        // 3. 写入 enabled 标记
        mapper.insert(buildConfigRow(channelCode, "enabled", String.valueOf(request.enabled()), now));

        // 4. 按 Schema 字段顺序逐一写入，确保密码处理逻辑一致
        Map<String, String> incomingValues = request.values() != null ? request.values() : Map.of();
        for (ChannelFieldDef field : schema.getFields()) {
            String key = field.key();
            String incomingValue = incomingValues.get(key);

            if (passwordKeys.contains(key)) {
                // PASSWORD 字段：空白或 "***" 表示「不修改」，还原旧加密值
                if (!StringUtils.hasText(incomingValue) || "***".equals(incomingValue)) {
                    String oldEncrypted = existingRaw.get(key);
                    if (StringUtils.hasText(oldEncrypted)) {
                        // 保留历史加密值（无需再次加密，DB 中本就存加密串）
                        mapper.insert(buildConfigRow(channelCode, key, oldEncrypted, now));
                    }
                    // 若历史也无值则跳过，保持字段不存在
                } else {
                    // 新密码：加密后写入
                    mapper.insert(buildConfigRow(channelCode, key, encryptValue(incomingValue), now));
                }
            } else {
                // 非敏感字段：直接写入（允许写入空字符串以清除旧值）
                if (incomingValue != null) {
                    mapper.insert(buildConfigRow(channelCode, key, incomingValue, now));
                }
            }
        }

        // 5. 主动刷新缓存（通过 self 代理调用，确保 @CacheEvict 生效）
        self.refreshCache(type);
        log.info("保存通知渠道配置成功: channel={}, enabled={}", channelCode, request.enabled());
    }

    // ─── 私有辅助方法 ──────────────────────────────────────────────────────────

    /**
     * 从数据库加载指定渠道的原始配置（PASSWORD 字段为加密串）
     */
    private Map<String, String> loadFromDb(String channelCode) {
        QueryWrapper qw = QueryWrapper.create()
                .where(NOTIFICATION_CHANNEL_CONFIG.CHANNEL.eq(channelCode));
        // 使用 HashMap 保证 Map 可修改（后续 getConfig 需要 put 解密值）
        return mapper.selectListByQuery(qw).stream()
                .collect(Collectors.toMap(
                        NotificationChannelConfig::getConfigKey,
                        c -> Objects.toString(c.getConfigValue(), ""),
                        (a, b) -> b,
                        HashMap::new
                ));
    }

    /**
     * 将 Schema + DB 原始值聚合为前端所需的 VO
     * <p>
     * PASSWORD 字段只要 DB 中有值（加密串非空）即脱敏为 "***"，
     * 前端无法反推任何明文信息。
     */
    private ChannelConfigVO buildVO(NotificationChannelSchema schema) {
        NotificationType type = schema.getType();
        Map<String, String> rawValues = loadFromDb(type.getCode());

        boolean enabled = "true".equalsIgnoreCase(rawValues.getOrDefault("enabled", "false"));

        Map<String, String> displayValues = new HashMap<>(rawValues);
        displayValues.remove("enabled");

        // PASSWORD 字段：DB 存加密串（非空）→ 统一展示 "***"
        schema.getFields().stream()
                .filter(f -> f.type() == FormFieldType.PASSWORD
                        && StringUtils.hasText(displayValues.get(f.key())))
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
     * 从 Schema 枚举提取 PASSWORD 字段 key 集合（按 NotificationType）
     */
    private Set<String> passwordKeysOf(NotificationType type) {
        try {
            return passwordKeysOf(NotificationChannelSchema.fromType(type));
        } catch (IllegalArgumentException e) {
            return Set.of();
        }
    }

    /**
     * 从 Schema 枚举提取 PASSWORD 字段 key 集合
     */
    private Set<String> passwordKeysOf(NotificationChannelSchema schema) {
        return schema.getFields().stream()
                .filter(f -> f.type() == FormFieldType.PASSWORD)
                .map(ChannelFieldDef::key)
                .collect(Collectors.toSet());
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

    /**
     * 加密敏感值（与 OAuth2 客户端配置加密方式一致）
     */
    private String encryptValue(String value) {
        if (value == null) {
            return null;
        }
        try {
            return stringEncryptor.encrypt(value);
        } catch (Exception e) {
            log.error("加密通知渠道配置失败，将使用原值: {}", e.getMessage());
            return value;
        }
    }

    /**
     * 解密敏感值
     */
    private String decryptValue(String value) {
        if (value == null) {
            return null;
        }
        try {
            return stringEncryptor.decrypt(value);
        } catch (Exception e) {
            log.error("解密通知渠道配置失败，将使用原值: {}", e.getMessage());
            return value;
        }
    }
}
