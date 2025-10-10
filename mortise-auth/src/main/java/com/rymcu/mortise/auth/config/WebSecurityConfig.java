package com.rymcu.mortise.auth.config;

import com.rymcu.mortise.auth.filter.JwtAuthenticationFilter;
import com.rymcu.mortise.auth.handler.*;
import com.rymcu.mortise.auth.repository.CacheAuthorizationRequestRepository;
import com.rymcu.mortise.auth.repository.DynamicClientRegistrationRepository;
import com.rymcu.mortise.auth.spi.SecurityConfigurer;
import com.rymcu.mortise.auth.support.UnifiedOAuth2AccessTokenResponseClient;
import com.rymcu.mortise.auth.support.UnifiedOAuth2AuthorizationRequestResolver;
import com.rymcu.mortise.auth.support.UnifiedOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Comparator;
import java.util.List;

/**
 * Web 安全配置
 * <p>
 * 核心安全配置，支持通过 SecurityConfigurer SPI 扩展
 * 支持动态 OAuth2 客户端配置，无需重启应用即可添加/修改/删除客户端
 *
 * @author ronger
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@ConditionalOnClass(HttpSecurity.class)
@RequiredArgsConstructor
public class WebSecurityConfig {

    // --- 必需的依赖 ---
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final RewriteAccessDeniedHandler rewriteAccessDeniedHandler;

    // --- OAuth2 相关依赖（全部可选） ---
    // 使用 ObjectProvider 使得 OAuth2 成为可选功能，当这些 Bean 不存在时应用仍可正常启动
    private final ObjectProvider<DynamicClientRegistrationRepository> dynamicClientRegistrationRepositoryProvider;
    private final ObjectProvider<OAuth2LoginSuccessHandler> oauth2LoginSuccessHandlerProvider;
    private final ObjectProvider<OAuth2LogoutSuccessHandler> oauth2LogoutSuccessHandlerProvider;
    private final ObjectProvider<CacheAuthorizationRequestRepository> cacheAuthorizationRequestRepositoryProvider;
    // 注入失败处理器
    private final ObjectProvider<OAuth2LoginFailureHandler> oAuth2LoginFailureHandlers;

    // --- 统一的 OAuth2 组件（可选） ---
    private final ObjectProvider<UnifiedOAuth2UserService> unifiedOAuth2UserServiceProvider;
    private final ObjectProvider<UnifiedOAuth2AccessTokenResponseClient> unifiedAccessTokenResponseClientProvider;
    private final ObjectProvider<UnifiedOAuth2AuthorizationRequestResolver> unifiedAuthorizationRequestResolverProvider;

    // --- SPI 扩展 ---
    private final List<SecurityConfigurer> securityConfigurers; // Spring 会自动注入一个空列表，如果没有任何实现

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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
     * JWT Decoder Factory - 支持多种签名算法
     * <p>
     * Logto 使用 ES384 (椭圆曲线) 或 RS256 (RSA) 签名算法
     * Spring Security 默认只支持 RS256，需要显式配置支持其他算法
     *
     * @return JwtDecoderFactory
     */
    @Bean
    public JwtDecoderFactory<ClientRegistration> idTokenDecoderFactory() {
        OidcIdTokenDecoderFactory idTokenDecoderFactory = new OidcIdTokenDecoderFactory();
        // 配置 JWT 解码器支持 ES384 签名算法（Logto 默认使用）
        // 也会自动支持 RS256、ES256 等其他常见算法
        idTokenDecoderFactory.setJwsAlgorithmResolver(clientRegistration -> SignatureAlgorithm.ES384);
        log.info("配置 JWT Decoder Factory: 支持 ES384 签名算法（Logto）");
        return idTokenDecoderFactory;
    }

    /**
     * Security 过滤器链配置
     * <p>
     * 支持动态 OAuth2 客户端，配置总是启用，具体客户端可用性取决于数据库配置
     */
    @Bean
    @Order(100)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("配置安全过滤器链（支持动态 OAuth2 客户端）...");

        // 基础配置 (总是应用)
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(rewriteAccessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 授权规则配置
        http.authorizeHttpRequests(authorize -> {
                    // ========== 应用 SPI 扩展配置 ==========
                    // 注意：必须在 anyRequest() 之前调用
                    applySecurityConfigurers(authorize);

                    authorize
                            .requestMatchers(
                                    "/api/v1/oauth2/qrcode/**",
                                    "/oauth2/authorization/**",
                                    "/login/oauth2/**")
                            .permitAll()
                            .anyRequest().authenticated();
                })
                .httpBasic(Customizer.withDefaults());

        // --- 条件化配置 OAuth2 登录 ---
        // OAuth2 是可选功能，只有当相关 Bean 存在时才配置
        DynamicClientRegistrationRepository dynamicRepository =
                dynamicClientRegistrationRepositoryProvider.getIfAvailable();

        if (dynamicRepository != null) {
            // 如果动态仓库存在，则配置 OAuth2
            log.info("检测到 DynamicClientRegistrationRepository，启用动态 OAuth2 客户端支持");
            try {
                configureOAuth2Login(http, dynamicRepository);
            } catch (Exception e) {
                log.error("配置 OAuth2 登录失败", e);
                throw new IllegalStateException("配置 OAuth2 登录失败", e);
            }
        } else {
            // 如果 Bean 不存在，则打印日志并跳过配置
            log.info("未检测到 DynamicClientRegistrationRepository，跳过 OAuth2 登录配置");
        }

        log.info("WebSecurityConfig 配置完成");

        return http.build();
    }

    /**
     * 配置 OAuth2 登录
     * <p>
     * 将 OAuth2 的配置逻辑抽离到一个单独的方法中，保持主方法的整洁
     * 使用动态客户端注册仓库，支持运行时动态管理客户端配置
     * 所有 OAuth2 相关的处理器都是可选的，通过 ObjectProvider 获取
     *
     * @param http                         HttpSecurity
     * @param clientRegistrationRepository 客户端注册仓库（动态实现）
     * @throws Exception 配置异常
     */
    private void configureOAuth2Login(HttpSecurity http,
                                      ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        http.oauth2Login(oauth2Login -> {
            oauth2Login
                    .authorizationEndpoint(authorization -> {
                        // 使用统一的授权请求解析器（如果存在）
                        authorization.authorizationRequestResolver(
                                authorizationRequestResolver(clientRegistrationRepository)
                        );
                        // 如果 CacheAuthorizationRequestRepository 存在，则配置它
                        cacheAuthorizationRequestRepositoryProvider.ifAvailable(
                                authorization::authorizationRequestRepository
                        );
                    })
                    .tokenEndpoint(token -> {
                        // 使用统一的 Token 客户端（如果存在）
                        unifiedAccessTokenResponseClientProvider.ifAvailable(
                                token::accessTokenResponseClient
                        );
                    })
                    .redirectionEndpoint(redirection -> redirection.baseUri("/login/oauth2/code/*"));

            // 配置用户信息服务（可选）
            unifiedOAuth2UserServiceProvider.ifAvailable(service ->
                    oauth2Login.userInfoEndpoint(userInfo -> userInfo.userService(service))
            );

            // 配置成功处理器（可选）
            OAuth2LoginSuccessHandler successHandler = oauth2LoginSuccessHandlerProvider.getIfAvailable();
            if (successHandler != null) {
                log.info("配置 OAuth2 登录成功处理器: {}", successHandler.getClass().getSimpleName());
                oauth2Login.successHandler(successHandler);
            } else {
                log.warn("未找到 OAuth2LoginSuccessHandler，将使用 Spring Security 默认行为");
            }

            // 配置失败处理器（可选）
            OAuth2LoginFailureHandler  failureHandler = oAuth2LoginFailureHandlers.getIfAvailable();
            if (failureHandler != null) {
                log.info("配置 OAuth2 登录失败处理器：{}", failureHandler.getClass().getSimpleName());
                oauth2Login.failureHandler(failureHandler);
            } else {
                log.warn("未找到 OAuth2LoginFailureHandler，将使用 Spring Security 默认行为");
            }

        });

        // 配置登出成功处理器（可选）
        oauth2LogoutSuccessHandlerProvider.ifAvailable(handler -> {
            try {
                http.logout(logout -> logout.logoutSuccessHandler(handler));
            } catch (Exception e) {
                log.error("配置 OAuth2 登出处理器失败", e);
            }
        });
    }

    /**
     * OAuth2 授权请求解析器
     * <p>
     * 使用动态客户端注册仓库，支持运行时查找客户端配置
     * 优先使用统一授权请求解析器（如果存在），否则使用默认解析器
     *
     * @param clientRegistrationRepository 客户端注册仓库
     * @return OAuth2AuthorizationRequestResolver
     */
    private OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {

        // 优先使用统一授权请求解析器（如果存在）
        UnifiedOAuth2AuthorizationRequestResolver unifiedResolver =
                unifiedAuthorizationRequestResolverProvider.getIfAvailable();

        if (unifiedResolver != null) {
            log.info("使用统一 OAuth2 授权请求解析器（支持微信等特殊处理）");
            return unifiedResolver;
        }

        // 回退到默认解析器
        log.info("使用默认 OAuth2 授权请求解析器");
        DefaultOAuth2AuthorizationRequestResolver defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository,
                        "/oauth2/authorization");

        // 可以在这里自定义授权请求，例如添加额外参数
        // defaultResolver.setAuthorizationRequestCustomizer(customizer ->
        //     customizer.additionalParameters(params -> params.put("prompt", "consent"))
        // );

        return defaultResolver;
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
