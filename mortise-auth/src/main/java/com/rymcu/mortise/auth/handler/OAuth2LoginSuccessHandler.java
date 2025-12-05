package com.rymcu.mortise.auth.handler;

import com.rymcu.mortise.auth.spi.OAuth2LoginSuccessHandlerProvider;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * OAuth2 登录成功处理器（基于 SPI）
 * <p>
 * 根据 registrationId 动态路由到不同的 Handler
 * <p>
 * 工作原理:
 * 1. 通过 {@link OAuth2LoginSuccessHandlerProvider} SPI 发现所有的处理器提供者
 * 2. 每个提供者声明自己支持的 registrationId 列表
 * 3. 根据 OAuth2 登录的 registrationId 路由到对应的处理器
 * <p>
 * 示例配置:
 * - System 模块提供者: 支持 ["logto", "logto-admin"]
 * - Member 模块提供者: 支持 ["logto-member", "github", "google", "wechat"]
 * <p>
 * 使用 @PostConstruct 延迟初始化路由表，避免循环依赖
 *
 * @author ronger
 * @since 1.0.0
 */
@Slf4j
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * registrationId → Handler 映射表
     * <p>
     * Key: registrationId (例如: logto-admin, logto-member, github)
     * Value: 对应的处理器实例
     */
    private final Map<String, AuthenticationSuccessHandler> handlerMap = new HashMap<>();

    /**
     * 默认 Handler（当找不到匹配的 registrationId 时使用）
     */
    private AuthenticationSuccessHandler defaultHandler;

    /**
     * Provider 列表（使用 Optional 处理可选依赖）
     */
    private final Optional<List<OAuth2LoginSuccessHandlerProvider>> providersOptional;

    /**
     * 构造函数注入 Provider 列表（但不立即初始化路由表）
     *
     * @param providersOptional OAuth2 登录成功处理器提供者列表（可选）
     */
    public OAuth2LoginSuccessHandler(Optional<List<OAuth2LoginSuccessHandlerProvider>> providersOptional) {
        this.providersOptional = providersOptional;
        log.info("OAuth2LoginSuccessHandler 构造函数执行（延迟初始化路由表）");
    }

    /**
     * 在所有依赖注入完成后初始化路由表
     * <p>
     * 使用 @PostConstruct 确保在循环依赖解决后再构建路由表
     */
    @PostConstruct
    public void initializeRoutes() {
        log.info("==========================================================");
        log.info("OAuth2LoginSuccessHandler 初始化路由表（基于 SPI）");

        if (providersOptional.isEmpty() || providersOptional.get().isEmpty()) {
            log.warn("未发现任何 OAuth2LoginSuccessHandlerProvider，OAuth2 登录功能可能无法正常工作");
            log.info("==========================================================");
            return;
        }

        List<OAuth2LoginSuccessHandlerProvider> providers = providersOptional.get();

        // 按优先级排序（优先级高的优先）
        providers.stream()
                .filter(OAuth2LoginSuccessHandlerProvider::isEnabled)
                .sorted(Comparator.comparingInt(OAuth2LoginSuccessHandlerProvider::getOrder).reversed())
                .forEach(provider -> {
                    AuthenticationSuccessHandler handler = provider.getHandler();
                    String[] registrationIds = provider.getSupportedRegistrationIds();

                    if (handler == null) {
                        log.warn("Provider {} 返回 null handler，已跳过", provider.getClass().getSimpleName());
                        return;
                    }

                    if (registrationIds == null || registrationIds.length == 0) {
                        log.warn("Provider {} 未声明支持的 registrationId，已跳过", provider.getClass().getSimpleName());
                        return;
                    }

                    // 注册到路由表
                    for (String registrationId : registrationIds) {
                        if (!handlerMap.containsKey(registrationId)) {
                            handlerMap.put(registrationId, handler);
                            log.info("注册路由: {} → {} (优先级: {})",
                                registrationId,
                                handler.getClass().getSimpleName(),
                                provider.getOrder());
                        } else {
                            log.debug("registrationId={} 已被注册，跳过优先级较低的 Provider", registrationId);
                        }
                    }

                    // 设置默认 Handler
                    if (provider.isDefault() && this.defaultHandler == null) {
                        this.defaultHandler = handler;
                        log.info("设置默认 Handler: {} (优先级: {})",
                            handler.getClass().getSimpleName(),
                            provider.getOrder());
                    }
                });

        log.info("OAuth2LoginSuccessHandler 路由表初始化完成:");
        log.info("  - 已注册 {} 个路由规则", handlerMap.size());
        log.info("  - 默认 Handler: {}",
            defaultHandler != null ? defaultHandler.getClass().getSimpleName() : "未设置");
        handlerMap.forEach((registrationId, handler) ->
            log.debug("    {} → {}", registrationId, handler.getClass().getSimpleName())
        );
        log.info("==========================================================");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 登录成功处理器被调用");

        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Auth)) {
            log.warn("认证类型不正确: {}", authentication.getClass());
            sendErrorResponse(response, "认证类型不正确");
            return;
        }

        String registrationId = oauth2Auth.getAuthorizedClientRegistrationId();
        log.info("OAuth2 registrationId: {}", registrationId);

        // 根据 registrationId 查找对应的 Handler
        AuthenticationSuccessHandler handler = handlerMap.get(registrationId);

        if (handler == null) {
            handler = defaultHandler;
            log.warn("未找到 registrationId={} 的专用 Handler，使用默认 Handler: {}",
                registrationId,
                handler != null ? handler.getClass().getSimpleName() : "null");
        } else {
            log.info("找到 registrationId={} 的 Handler: {}",
                registrationId,
                handler.getClass().getSimpleName());
        }

        if (handler == null) {
            log.error("找不到可用的 Handler，registrationId={}", registrationId);
            sendErrorResponse(response, "OAuth2 登录配置错误");
            return;
        }

        // 委托给具体的 Handler 处理
        log.info("委托给 {} 处理认证成功逻辑", handler.getClass().getSimpleName());
        handler.onAuthenticationSuccess(request, response, authentication);
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(String.format(
            "{\"success\":false,\"message\":\"%s\"}", message
        ));
    }

    /**
     * 获取当前路由规则（用于调试）
     */
    public Map<String, String> getRoutes() {
        Map<String, String> routes = new HashMap<>();
        handlerMap.forEach((registrationId, handler) ->
            routes.put(registrationId, handler.getClass().getSimpleName())
        );
        return routes;
    }
}
