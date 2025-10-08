package com.rymcu.mortise.auth.repository;

import com.rymcu.mortise.auth.entity.Oauth2ClientConfig;
import com.rymcu.mortise.auth.service.Oauth2ClientConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态的、数据库驱动的 ClientRegistrationRepository。
 * <p>
 * 该实现会覆盖 Spring Boot 的默认实现，实现 OAuth2 客户端的动态管理。
 * 支持运行时动态添加、修改、删除 OAuth2 客户端配置，无需重启应用。
 * <p>
 * 核心特性：
 * <ul>
 *   <li>按需加载：只有当请求 /oauth2/authorization/{registrationId} 发生时才查询数据库</li>
 *   <li>内存缓存：使用 ConcurrentHashMap 缓存已加载的配置，避免重复查询</li>
 *   <li>动态构建：实时将数据库记录转换为 Spring Security 的 ClientRegistration 对象</li>
 *   <li>缓存失效：提供 clearCache 方法，支持配置更新后清除缓存</li>
 * </ul>
 * <p>
 * 该 Bean 是可选的，通过配置项 mortise.oauth2.dynamic-client-enabled 控制是否启用（默认启用）
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Repository("dynamicClientRegistrationRepository")
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "mortise.oauth2",
        name = "dynamic-client-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class DynamicClientRegistrationRepository implements ClientRegistrationRepository {

    private final Oauth2ClientConfigService clientConfigService;

    /**
     * 内存缓存，避免每次请求都查库
     * key: registrationId
     * value: ClientRegistration
     */
    private final Map<String, ClientRegistration> registrationCache = new ConcurrentHashMap<>();

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        log.debug("尝试查找客户端注册配置: registrationId={}", registrationId);

        // 1. 优先从缓存中获取
        ClientRegistration registration = registrationCache.get(registrationId);
        if (registration != null) {
            log.debug("从缓存中找到客户端配置: registrationId={}", registrationId);
            return registration;
        }

        // 2. 缓存未命中，从数据库中查找配置
        Oauth2ClientConfig config = clientConfigService.loadOauth2ClientConfigByRegistrationId(registrationId);

        if (config != null) {
            log.info("从数据库中找到客户端配置: registrationId={}, clientName={}",
                    registrationId, config.getClientName());

            // 3. 将数据库实体动态构建成 ClientRegistration 对象
            ClientRegistration newRegistration = buildClientRegistration(config);

            // 4. 放入缓存
            registrationCache.put(registrationId, newRegistration);
            log.debug("客户端配置已缓存: registrationId={}", registrationId);

            return newRegistration;
        }

        log.warn("未找到客户端配置: registrationId={}", registrationId);
        return null;
    }

    /**
     * 将数据库配置模型转换为 Spring Security 的 ClientRegistration
     *
     * @param config 数据库中的客户端配置
     * @return ClientRegistration 对象
     */
    private ClientRegistration buildClientRegistration(Oauth2ClientConfig config) {
        log.debug("构建 ClientRegistration: registrationId={}", config.getRegistrationId());
        log.debug("配置详情: clientId={}, redirectUri={}, authMethod={}, grantType={}",
                config.getClientId(), config.getRedirectUriTemplate(),
                config.getClientAuthenticationMethod(), config.getAuthorizationGrantType());

        // 处理默认值，避免空字符串导致异常
        String authMethod = StringUtils.hasText(config.getClientAuthenticationMethod())
                ? config.getClientAuthenticationMethod()
                : "client_secret_basic";

        String grantType = StringUtils.hasText(config.getAuthorizationGrantType())
                ? config.getAuthorizationGrantType()
                : "authorization_code";

        // 处理 redirectUri，确保使用正确的模板格式
        // 注意：必须使用 {baseUrl} 占位符，Spring Security 会在运行时自动替换
        String redirectUri = config.getRedirectUriTemplate();
        if (!StringUtils.hasText(redirectUri)) {
            // 如果数据库中没有配置，使用默认模板
            redirectUri = "{baseUrl}/login/oauth2/code/{registrationId}";
        }

        log.debug("使用 redirectUri 模板: {}", redirectUri);

        ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(config.getRegistrationId())
                .clientId(config.getClientId())
                .clientSecret(config.getClientSecret())
                .clientName(config.getClientName())
                .scope(StringUtils.commaDelimitedListToSet(config.getScopes()))
                .redirectUri(redirectUri)  // 直接使用模板，Spring Security 会自动解析
                .clientAuthenticationMethod(new ClientAuthenticationMethod(authMethod))
                .authorizationGrantType(new AuthorizationGrantType(grantType))
                .authorizationUri(config.getAuthorizationUri())
                .tokenUri(config.getTokenUri())
                .userInfoUri(config.getUserInfoUri());

        // 用户名属性是可选的
        if (StringUtils.hasText(config.getUserNameAttribute())) {
            builder.userNameAttributeName(config.getUserNameAttribute());
        }

        // JWK Set URI 是可选的
        if (StringUtils.hasText(config.getJwkSetUri())) {
            builder.jwkSetUri(config.getJwkSetUri());
        }

        ClientRegistration registration = builder.build();
        log.info("成功构建 ClientRegistration: registrationId={}, redirectUri模板={}",
                config.getRegistrationId(), registration.getRedirectUri());

        return registration;
    }

    /**
     * 提供一个方法用于在配置更新时主动清除缓存
     * <p>
     * 当通过管理后台更新或删除客户端配置时，应调用此方法确保下次加载的是最新配置。
     * 在集群环境中，需要通过消息队列等方式通知所有节点清除缓存。
     *
     * @param registrationId 客户端注册ID
     */
    public void clearCache(String registrationId) {
        log.info("清除客户端配置缓存: registrationId={}", registrationId);
        registrationCache.remove(registrationId);
    }

    /**
     * 清除所有缓存
     * <p>
     * 适用于批量更新配置或系统维护场景
     */
    public void clearAllCache() {
        log.info("清除所有客户端配置缓存, 当前缓存数量: {}", registrationCache.size());
        registrationCache.clear();
    }

    /**
     * 获取当前缓存的客户端数量
     *
     * @return 缓存数量
     */
    public int getCacheSize() {
        return registrationCache.size();
    }

    /**
     * 预加载所有启用的客户端配置到缓存
     * <p>
     * 可在应用启动时调用，提前加载常用配置
     */
    public void preloadCache() {
        log.info("预加载所有启用的客户端配置到缓存");

        clientConfigService.loadOauth2ClientConfigAllEnabled().forEach(config -> {
            try {
                ClientRegistration registration = buildClientRegistration(config);
                registrationCache.put(config.getRegistrationId(), registration);
                log.debug("预加载客户端配置: registrationId={}", config.getRegistrationId());
            } catch (Exception e) {
                log.error("预加载客户端配置失败: registrationId={}", config.getRegistrationId(), e);
            }
        });

        log.info("预加载完成，已缓存 {} 个客户端配置", registrationCache.size());
    }
}
