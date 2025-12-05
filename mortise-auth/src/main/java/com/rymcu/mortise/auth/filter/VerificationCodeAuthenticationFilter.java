package com.rymcu.mortise.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rymcu.mortise.auth.config.SmsAuthProperties;
import com.rymcu.mortise.auth.enumerate.UserType;
import com.rymcu.mortise.auth.token.VerificationCodeAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.util.Map;

/**
 * 短信验证码登录过滤器
 * <p>
 * 拦截验证码登录请求，构建 SmsCodeAuthenticationToken 并提交给 AuthenticationManager 认证
 * </p>
 * <p>
 * 设计参考 UsernamePasswordAuthenticationFilter
 * </p>
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2025/11/12
 */
@Slf4j
public class VerificationCodeAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    /**
     * 短信验证码登录配置
     */
    @Getter
    private final SmsAuthProperties properties;

    /**
     * 是否仅支持 POST 请求
     */
    @Setter
    private boolean postOnly = true;

    /**
     * JSON 序列化工具
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 构造函数（使用默认配置）
     *
     * @param authenticationManager 认证管理器
     */
    public VerificationCodeAuthenticationFilter(AuthenticationManager authenticationManager) {
        this(authenticationManager, new SmsAuthProperties());
    }

    /**
     * 构造函数（使用自定义配置）
     *
     * @param authenticationManager 认证管理器
     * @param properties            短信验证码登录配置
     */
    public VerificationCodeAuthenticationFilter(AuthenticationManager authenticationManager,
                                                 SmsAuthProperties properties) {
        super(createRequestMatcher(properties));
        this.properties = properties;
        this.postOnly = properties.isPostOnly();
        setAuthenticationManager(authenticationManager);
        log.info("短信验证码登录过滤器已初始化: loginUrl={}", properties.getLoginUrl());
    }

    /**
     * 创建请求匹配器
     *
     * @param properties 配置属性
     * @return RequestMatcher
     */
    private static RequestMatcher createRequestMatcher(SmsAuthProperties properties) {
        return request -> {
            String loginUrl = properties.getLoginUrl();
            boolean isPost = "POST".equalsIgnoreCase(request.getMethod());
            boolean isMatchPath = loginUrl.equals(request.getServletPath());
            return (!properties.isPostOnly() || isPost) && isMatchPath;
        };
    }

    /**
     * 尝试认证
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @return Authentication 认证对象
     * @throws AuthenticationException 认证异常
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {

        // 1. 检查请求方法
        if (postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("不支持的认证方法: " + request.getMethod());
        }

        // 2. 提取认证参数（支持 JSON 和 Form）
        String mobile;
        String smsCode;
        String userType;

        String contentType = request.getContentType();
        if (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
            // JSON 格式
            Map<String, String> params = parseJsonBody(request);
            mobile = params.get(properties.getMobileParameter());
            smsCode = params.get(properties.getCodeParameter());
            userType = params.get(properties.getUserTypeParameter());
        } else {
            // Form 格式
            mobile = obtainParameter(request, properties.getMobileParameter());
            smsCode = obtainParameter(request, properties.getCodeParameter());
            userType = obtainParameter(request, properties.getUserTypeParameter());
        }

        // 3. 参数校验
        if (StringUtils.isBlank(mobile)) {
            throw new AuthenticationServiceException("手机号不能为空");
        }
        if (StringUtils.isBlank(smsCode)) {
            throw new AuthenticationServiceException("验证码不能为空");
        }

        // 4. 设置默认用户类型
        if (StringUtils.isBlank(userType)) {
            userType = determineUserType(request);
        }

        log.debug("短信验证码登录请求: mobile={}, userType={}", mobile, userType);

        // 5. 创建未认证的 Token
        VerificationCodeAuthenticationToken authRequest = new VerificationCodeAuthenticationToken(mobile.trim(), smsCode.trim());
        authRequest.setDetails(userType); // 将用户类型存储到 details 中

        // 6. 提交给 AuthenticationManager 进行认证
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    /**
     * 从请求中提取参数
     *
     * @param request HTTP 请求
     * @param parameter 参数名
     * @return 参数值
     */
    private String obtainParameter(HttpServletRequest request, String parameter) {
        String value = request.getParameter(parameter);
        return value != null ? value.trim() : "";
    }

    /**
     * 解析 JSON 请求体
     *
     * @param request HTTP 请求
     * @return 参数 Map
     * @throws IOException 读取失败
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> parseJsonBody(HttpServletRequest request) throws IOException {
        try {
            return objectMapper.readValue(request.getInputStream(), Map.class);
        } catch (IOException e) {
            log.error("解析JSON请求体失败", e);
            throw new AuthenticationServiceException("请求格式错误", e);
        }
    }

    /**
     * 根据请求路径确定用户类型
     * <p>
     * 支持通过路径区分不同的用户类型，例如：
     * <ul>
     *   <li>/api/v1/auth/login/sms/system - 系统用户</li>
     *   <li>/api/v1/auth/login/sms/member - 会员用户</li>
     *   <li>/api/v1/auth/login/sms - 默认为系统用户</li>
     * </ul>
     * </p>
     *
     * @param request HTTP 请求
     * @return 用户类型标识
     */
    private String determineUserType(HttpServletRequest request) {
        String requestUri = request.getRequestURI();

        if (requestUri.contains("/member")) {
            return UserType.MEMBER.getCode();
        }

        // 默认为系统用户
        return UserType.SYSTEM.getCode();
    }

}
