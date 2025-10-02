package com.rymcu.mortise.auth.config;

import com.rymcu.mortise.auth.filter.JwtAuthenticationFilter;
import com.rymcu.mortise.auth.handler.JwtAuthenticationEntryPoint;
import com.rymcu.mortise.auth.handler.OAuth2LoginSuccessHandler;
import com.rymcu.mortise.auth.handler.OAuth2LogoutSuccessHandler;
import com.rymcu.mortise.auth.handler.RewriteAccessDeniedHandler;
import com.rymcu.mortise.auth.repository.CacheAuthorizationRequestRepository;
import com.rymcu.mortise.auth.spi.SecurityConfigurer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Web 安全配置
 * <p>
 * 核心安全配置，支持通过 SecurityConfigurer SPI 扩展
 *
 * @author ronger
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@ConditionalOnClass(HttpSecurity.class)
public class WebSecurityConfig {

    @Resource
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Resource
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @Resource
    private RewriteAccessDeniedHandler rewriteAccessDeniedHandler;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    private final List<SecurityConfigurer> securityConfigurers;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final CacheAuthorizationRequestRepository cacheAuthorizationRequestRepository;

    /**
     * 构造函数注入（使用 Optional 处理可选依赖）
     */
    @Autowired
    public WebSecurityConfig(
            Optional<List<SecurityConfigurer>> configurersOptional,
            Optional<ClientRegistrationRepository> clientRegistrationRepositoryOptional,
            Optional<CacheAuthorizationRequestRepository> cacheAuthorizationRequestRepositoryOptional) {
        this.securityConfigurers = configurersOptional.orElse(null);
        this.clientRegistrationRepository = clientRegistrationRepositoryOptional.orElse(null);
        this.cacheAuthorizationRequestRepository = cacheAuthorizationRequestRepositoryOptional.orElse(null);

        log.info("==========================================================");
        log.info("WebSecurityConfig 构造函数被调用");
        log.info("发现 {} 个 SecurityConfigurer 扩展",
                 this.securityConfigurers == null ? 0 : this.securityConfigurers.size());
        log.info("OAuth2 客户端注册仓库: {}",
                 this.clientRegistrationRepository != null ? "已配置" : "未配置");
        log.info("OAuth2 授权请求仓库: {}",
                 this.cacheAuthorizationRequestRepository != null ? "已配置" : "未配置");
        log.info("==========================================================");
    }

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * JWT 解码器工厂（用于 OIDC ID Token）
     */
    @Bean
    public JwtDecoderFactory<ClientRegistration> idTokenDecoderFactory() {
        OidcIdTokenDecoderFactory idTokenDecoderFactory = new OidcIdTokenDecoderFactory();
        idTokenDecoderFactory.setJwsAlgorithmResolver(clientRegistration -> SignatureAlgorithm.ES384);
        return idTokenDecoderFactory;
    }

    /**
     * OAuth2 登录成功处理器
     */
    @Bean
    public OAuth2LoginSuccessHandler oauth2LoginSuccessHandler() {
        return new OAuth2LoginSuccessHandler();
    }

    /**
     * OAuth2 登出成功处理器
     */
    @Bean
    public OAuth2LogoutSuccessHandler oauth2LogoutSuccessHandler() {
        return new OAuth2LogoutSuccessHandler();
    }

    /**
     * OIDC 用户服务
     * 用于从 OIDC 提供者获取用户信息，并发布 OidcUserEvent 事件
     * 业务层可以通过 @EventListener 监听此事件进行后续处理（如保存用户到数据库）
     */
    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService();
        return request -> {
            // 获取用户信息
            OidcUser user = delegate.loadUser(request);

            // 发布事件，业务层可以监听此事件进行后续处理
            // 注意：通过反射避免直接依赖 system 模块，防止循环依赖
            try {
                Class<?> eventClass = Class.forName("com.rymcu.mortise.system.handler.event.OidcUserEvent");
                Object event = eventClass.getConstructor(OidcUser.class).newInstance(user);
                applicationEventPublisher.publishEvent(event);
                log.info("OIDC 用户信息已加载，发布 OidcUserEvent 事件: {}", user.getEmail());
            } catch (Exception e) {
                log.warn("发布 OidcUserEvent 失败（可能 system 模块未加载）: {}", e.getMessage());
            }

            return user;
        };
    }

    /**
     * OAuth2 授权请求解析器
     */
    private OAuth2AuthorizationRequestResolver authorizationRequestResolver() {
        if (clientRegistrationRepository == null) {
            return null;
        }

        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository,
                        "/api/v1/oauth2/authorization");
        resolver.setAuthorizationRequestCustomizer(authorizationRequestCustomizer());
        return resolver;
    }

    /**
     * OAuth2 授权请求自定义器
     * 设置 prompt 参数为 consent，即每次都要求用户同意授权
     */
    private Consumer<OAuth2AuthorizationRequest.Builder> authorizationRequestCustomizer() {
        return customizer -> customizer
                .additionalParameters(params -> params.put("prompt", "consent"));
    }

    /**
     * 认证管理器
     *
     * @param configuration 认证配置
     * @return AuthenticationManager
     * @throws Exception 异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Security 过滤器链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 基础配置
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> {
                    // ========== 应用 SPI 扩展配置 ==========
                    // 注意：必须在 anyRequest() 之前调用
                    applySecurityConfigurers(authorize);

                    // 其他所有请求需要认证
                    authorize.anyRequest().authenticated();
                })
                .httpBasic(Customizer.withDefaults());

        // 配置 OAuth2 登录（如果有客户端注册仓库）
        if (clientRegistrationRepository != null) {
            http.oauth2Login(oauth2Login ->
                oauth2Login
                    .authorizationEndpoint(authorization -> {
                        OAuth2AuthorizationRequestResolver resolver = authorizationRequestResolver();
                        if (resolver != null) {
                            authorization.authorizationRequestResolver(resolver);
                        }
                        if (cacheAuthorizationRequestRepository != null) {
                            authorization.authorizationRequestRepository(cacheAuthorizationRequestRepository);
                        }
                    })
                    .redirectionEndpoint(redirection ->
                        redirection.baseUri("/api/v1/oauth2/code/*"))
                    .userInfoEndpoint(userInfoEndpoint ->
                        userInfoEndpoint.oidcUserService(oidcUserService()))
                    .successHandler(oauth2LoginSuccessHandler())
            );

            // 配置登出
            http.logout(logout ->
                logout.logoutSuccessHandler(oauth2LogoutSuccessHandler())
            );

            log.info("OAuth2 登录配置已启用");
        } else {
            log.info("未检测到 OAuth2 客户端配置，跳过 OAuth2 登录配置");
        }

        // 配置异常处理
        http.exceptionHandling(exception -> exception
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(rewriteAccessDeniedHandler)
        );

        // 添加 JWT 认证过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("WebSecurityConfig 配置完成");

        return http.build();
    }

    /**
     * 应用所有 SecurityConfigurer SPI 扩展
     *
     * @param registry 授权请求匹配器注册表
     */
    private void applySecurityConfigurers(
            org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
    ) {
        if (securityConfigurers == null || securityConfigurers.isEmpty()) {
            log.info("未发现 SecurityConfigurer 扩展");
            return;
        }

        // 按优先级排序并应用
        securityConfigurers.stream()
                .filter(SecurityConfigurer::isEnabled)
                .sorted(Comparator.comparingInt(SecurityConfigurer::getOrder))
                .forEach(configurer -> {
                    try {
                        configurer.configureAuthorization(registry);
                        log.info("应用 SecurityConfigurer: {}", configurer.getClass().getSimpleName());
                    } catch (Exception e) {
                        log.error("应用 SecurityConfigurer 失败: {}", configurer.getClass().getSimpleName(), e);
                    }
                });
    }
}
