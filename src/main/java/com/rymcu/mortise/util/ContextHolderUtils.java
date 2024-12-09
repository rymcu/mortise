package com.rymcu.mortise.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author ronger
 * @ClassName: ContextHolderUtils
 * @Description: 上下文工具类
 * @date 2024-04-13
 */
public class ContextHolderUtils {
    private static final Map<String, HttpSession> sessionMap = new HashMap<>();

    /**
     * SpringMvc下获取request
     *
     * @return HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

    }

    /**
     * SpringMvc下获取session
     *
     * @return HttpSession
     */
    public static HttpSession getSession() {
        HttpServletRequest request = getRequest();
        String tempSessionId = request.getParameter("sessionId");
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        if (StringUtils.isNotEmpty(tempSessionId) && !tempSessionId.equals(sessionId)) {
            sessionId = tempSessionId;
            if (sessionMap.containsKey(sessionId)) {
                session = sessionMap.get(sessionId);
            }
        }
        if (!sessionMap.containsKey(sessionId)) {
            sessionMap.put(sessionId, session);
        }
        return session;
    }

    public static HttpSession getSession2() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        return request.getSession();

    }

    public static HttpSession getSession(String sessionId) {
        HttpSession session = sessionMap.get(sessionId);
        return session == null ? getSession() : session;
    }

    public static void removeSession(String sessionId) {
        sessionMap.remove(sessionId);
    }

}
