package com.rymcu.mortise.web;

import com.rymcu.mortise.core.result.GlobalResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 通知管理控制器
 * 提供系统通知相关功能
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2024/4/25
 */
@Tag(name = "通知管理", description = "系统通知相关接口")
@RestController
@RequestMapping("/api/v1/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    @Operation(summary = "获取通知列表", description = "获取当前用户的通知列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    @GetMapping
    public GlobalResult<List<Object>> getNotifications() {
        // TODO: 实现通知功能
        List<Object> notifications = new ArrayList<>();
        return GlobalResult.success(notifications);
    }
}
