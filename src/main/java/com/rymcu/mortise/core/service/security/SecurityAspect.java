package com.rymcu.mortise.core.service.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rymcu.mortise.auth.JwtConstants;
import com.rymcu.mortise.model.TokenUser;
import com.rymcu.mortise.util.UserUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 检查用户修改信息权限
 *
 * @author ronger
 */
@Aspect
@Component
public class SecurityAspect {

    Logger logger = LoggerFactory.getLogger(SecurityAspect.class);

    @Pointcut("@annotation(com.rymcu.mortise.core.service.security.annotation.SecurityInterceptor)")
    public void securityPointCut() {
    }

    /**
     * 检查用户修改信息权限
     *
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 调用出错
     */
    @Before(value = "securityPointCut()")
    public void doBefore(JoinPoint joinPoint) throws AccountNotFoundException, JsonProcessingException {
        logger.info("检查用户修改信息权限 start ...");
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String idUser = "";
        if (isAjax(request)) {
            Object[] objects = joinPoint.getArgs();
            if (objects[0] instanceof Integer) {
                idUser = objects[0].toString();
            } else {
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString = objectMapper.writeValueAsString(objects[0]);
                JsonNode jsonNode = objectMapper.readTree(jsonString);

                if (Objects.nonNull(jsonNode)) {
                    JsonNode idUserNode = jsonNode.get("idUser");
                    if (idUserNode != null) {
                        idUser = idUserNode.asText();
                    }
                }
            }
        } else {
            Map params = getParams(request);
            if (params.isEmpty()) {
                params = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            } else {
                params.putAll((Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE));
            }
            idUser = (String) params.get("idUser");
        }
        if (Objects.nonNull(idUser)) {
            String authHeader = request.getHeader(JwtConstants.AUTHORIZATION);
            if (StringUtils.isNotBlank(authHeader)) {
                TokenUser tokenUser = UserUtils.getTokenUser(authHeader);
                if (!idUser.equals(tokenUser.getIdUser().toString())) {
                    throw new BadCredentialsException("签名错误");
                }
            }
        } else {
            throw new AuthorizationDeniedException("无权限");
        }
        logger.info("检查用户修改信息权限 end ...");
    }

    private Map<String, String> getParams(HttpServletRequest request) {
        Map<String, String> paramsMap = new HashMap<>(10);
        Enumeration<String> paraNames = request.getParameterNames();
        while (paraNames.hasMoreElements()) {
            String key = paraNames.nextElement();
            if ("password".equals(key)) {
                continue;
            }
            paramsMap.put(key, request.getParameter(key));
        }
        return paramsMap;
    }

    private boolean isAjax(HttpServletRequest request) {
        String requestedWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return true;
        }
        String contentType = request.getContentType();
        return StringUtils.isNotBlank(contentType) && contentType.contains("application/json");
    }
}
