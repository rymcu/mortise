package com.rymcu.mortise.system.controller.oauth2;

import com.rymcu.mortise.web.annotation.AdminController;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.controller.facade.OAuth2QrcodeAdminFacade;
import com.rymcu.mortise.log.annotation.ApiLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * Created on 2025/10/8 22:33.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.auth.controller
 */
@Tag(name = "OAuth2 授权二维码", description = "OAuth2 授权二维码生成接口")
@AdminController
@RequestMapping("/oauth2/qrcode")
@RequiredArgsConstructor
public class Oauth2QrcodeController {

    private final OAuth2QrcodeAdminFacade oauth2QrcodeAdminFacade;

    @Operation(
        summary = "获取微信 OAuth2 授权二维码链接",
        description = "根据 registration_id 获取微信 OAuth2 授权二维码的跳转链接。前端可跳转该链接进行扫码登录。",
        parameters = {
            @Parameter(name = "registration_id", description = "OAuth2 客户端注册ID", required = true, example = "wechat")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "操作成功，返回二维码授权链接。"),
            @ApiResponse(responseCode = "400", description = "请求参数错误或找不到客户端注册ID。")
        }
    )
    @GetMapping("/wechat/{registrationId}")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "获取微信OAuth2授权二维码链接")
    public GlobalResult<Map<String, String>> getWeChatQRCode(@PathVariable String registrationId, HttpServletRequest request, HttpServletResponse response) {
        return oauth2QrcodeAdminFacade.getWeChatQRCode(registrationId, request, response);
    }

    @GetMapping("/state/{state}")
    @ApiLog(recordParams = false, recordRequestBody = false, recordResponseBody = false, value = "查询OAuth2二维码状态")
    public GlobalResult<Map<String, Object>> getStateQRCode(@PathVariable String state) {
        return oauth2QrcodeAdminFacade.getStateQRCode(state);
    }
}

