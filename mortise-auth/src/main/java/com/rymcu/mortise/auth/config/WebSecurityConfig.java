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
import org.springframework.beans.factory.ObjectProvider;
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

import jakarta.servlet.http.HttpServletRequest;
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

    private final ObjectProvider<OAuth2LoginSuccessHandler> oauth2LoginSuccessHandlerProvider;
    private final List<SecurityConfigurer> securityConfigurers;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final CacheAuthorizationRequestRepository cacheAuthorizationRequestRepository;

    /**
     * 构造函数注入（使用 Optional 和 ObjectProvider 处理可选依赖）
     */
    @Autowired
    public WebSecurityConfig(
            ObjectProvider<OAuth2LoginSuccessHandler> oauth2LoginSuccessHandlerProvider,
            Optional<List<SecurityConfigurer>> configurersOptional,
            Optional<ClientRegistrationRepository> clientRegistrationRepositoryOptional,
            Optional<CacheAuthorizationRequestRepository> cacheAuthorizationRequestRepositoryOptional) {
        this.oauth2LoginSuccessHandlerProvider = oauth2LoginSuccessHandlerProvider;
        this.securityConfigurers = configurersOptional.orElse(null);
        this.clientRegistrationRepository = clientRegistrationRepositoryOptional.orElse(null);
        this.cacheAuthorizationRequestRepository = cacheAuthorizationRequestRepositoryOptional.orElse(null);

        log.info("==========================================================");
        log.info("WebSecurityConfig 构造函数被调用");
        log.info("OAuth2LoginSuccessHandler Provider: 已注入");
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
     * OAuth2 登出成功处理器
     */
    @Bean
    public OAuth2LogoutSuccessHandler oauth2LogoutSuccessHandler() {
        return new OAuth2LogoutSuccessHandler();
    }

    /**
     * OIDC 用户服务
     * 仅负责从 OIDC 提供者加载用户信息，不处理业务逻辑
     * 业务逻辑（如创建/更新用户）由 SuccessHandler 处理
     */
    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        // 使用默认的 OidcUserService 即可
        return new OidcUserService();
    }

    /**
     * OAuth2 授权请求解析器
     */
    private OAuth2AuthorizationRequestResolver authorizationRequestResolver() {
        if (clientRegistrationRepository == null) {
            return null;
        }

    DefaultOAuth2AuthorizationRequestResolver legacyResolver =
        new DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository,
            "/api/v1/oauth2/authorization");
    legacyResolver.setAuthorizationRequestCustomizer(authorizationRequestCustomizer());

    DefaultOAuth2AuthorizationRequestResolver springDefaultResolver =
        new DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository,
            "/oauth2/authorization");
    springDefaultResolver.setAuthorizationRequestCustomizer(authorizationRequestCustomizer());

    return new CompoundAuthorizationRequestResolver(springDefaultResolver, legacyResolver);
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

            authorize
                .requestMatchers(
                    "/oauth2/authorization/**",
                    "/login/oauth2/**",
                    "/api/v1/oauth2/**")
                .permitAll()
                .anyRequest().authenticated();
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
                        redirection.baseUri("/login/oauth2/code/*"))
                    .userInfoEndpoint(userInfoEndpoint ->
                        userInfoEndpoint.oidcUserService(oidcUserService()))
                    .successHandler(oauth2LoginSuccessHandlerProvider.getObject())  // 延迟获取 Handler
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

        private static class CompoundAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

            private final OAuth2AuthorizationRequestResolver[] delegates;

            private CompoundAuthorizationRequestResolver(OAuth2AuthorizationRequestResolver... delegates) {
                this.delegates = delegates;
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                for (OAuth2AuthorizationRequestResolver delegate : delegates) {
                    OAuth2AuthorizationRequest requestResult = delegate.resolve(request);
                    if (requestResult != null) {
                        return requestResult;
                    }
                }
                return null;
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                for (OAuth2AuthorizationRequestResolver delegate : delegates) {
                    OAuth2AuthorizationRequest requestResult = delegate.resolve(request, clientRegistrationId);
                    if (requestResult != null) {
                        return requestResult;
                    }
                }
                return null;
            }
        }
}
