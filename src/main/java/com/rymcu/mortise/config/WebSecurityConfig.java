package com.rymcu.mortise.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rymcu.mortise.auth.*;
import com.rymcu.mortise.handler.event.OidcUserEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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

import java.util.function.Consumer;

/**
 * Created on 2025/2/24 19:24.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.auth
 */
@Slf4j
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Resource
    private ClientRegistrationRepository clientRegistrationRepository;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Resource
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @Resource
    private RewriteAccessDenyFilter rewriteAccessDenyFilter;

    @Bean
    public JwtDecoderFactory<ClientRegistration> idTokenDecoderFactory() {
        OidcIdTokenDecoderFactory idTokenDecoderFactory = new OidcIdTokenDecoderFactory();
        idTokenDecoderFactory.setJwsAlgorithmResolver(clientRegistration -> SignatureAlgorithm.ES384);
        return idTokenDecoderFactory;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((authorize) -> {
                    authorize.requestMatchers("/api/v1/common/**").permitAll();
                    authorize.requestMatchers("/api/v1/auth/login/**").permitAll();
                    authorize.requestMatchers("/api/v1/auth/logout/**").permitAll();
                    authorize.requestMatchers("/api/v1/auth/refresh-token/**").permitAll();
                    authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    authorize.anyRequest().authenticated();
                }).oauth2Login(oauth2Login ->
                        oauth2Login
                                .authorizationEndpoint(authorization -> authorization.authorizationRequestResolver(
                                        authorizationRequestResolver(this.clientRegistrationRepository))
                                        .authorizationRequestRepository(new RedisAuthorizationRequestRepository(this.stringRedisTemplate, this.objectMapper)))
                                .redirectionEndpoint(redirection -> redirection.baseUri("/api/v1/oauth2/code/*"))
                                .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.oidcUserService(oidcUserService()))
                                .successHandler(oauth2LoginSuccessHandler())
                )
                .logout(logout ->
                        logout
                                .logoutSuccessHandler(new OAuth2LogoutSuccessHandler())
                ).httpBasic(Customizer.withDefaults());

        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint).accessDeniedHandler(rewriteAccessDenyFilter));

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public OAuth2LoginSuccessHandler oauth2LoginSuccessHandler() {
        return new OAuth2LoginSuccessHandler();
    }

    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService();
        return request -> {
            // 获取用户信息
            OidcUser user = delegate.loadUser(request);
            // 可在此处添加自定义逻辑（如保存到数据库）
            applicationEventPublisher.publishEvent(new OidcUserEvent(user));
            return user;
        };
    }

    private OAuth2AuthorizationRequestResolver authorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        DefaultOAuth2AuthorizationRequestResolver authorizationRequestResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/api/v1/oauth2/authorization");
        authorizationRequestResolver.setAuthorizationRequestCustomizer(authorizationRequestCustomizer());

        return authorizationRequestResolver;
    }

    private Consumer<OAuth2AuthorizationRequest.Builder> authorizationRequestCustomizer() {
        // 设置 prompt 参数为 consent，即同意授权
        return customizer -> customizer
                .additionalParameters(params -> params.put("prompt", "consent"));
    }
}
