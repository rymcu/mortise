package com.rymcu.mortise.system.controller.facade;

import com.rymcu.mortise.core.result.GlobalResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

public interface OAuth2QrcodeAdminFacade {

    GlobalResult<Map<String, String>> getWeChatQRCode(String registrationId, HttpServletRequest request, HttpServletResponse response);

    GlobalResult<Map<String, Object>> getStateQRCode(String state);
}
