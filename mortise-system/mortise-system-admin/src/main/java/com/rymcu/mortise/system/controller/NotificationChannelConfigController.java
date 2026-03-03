package com.rymcu.mortise.system.controller;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.notification.model.ChannelConfigSaveRequest;
import com.rymcu.mortise.notification.model.ChannelConfigVO;
import com.rymcu.mortise.notification.service.NotificationChannelConfigService;
import com.rymcu.mortise.web.annotation.AdminController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 通知渠道配置管理控制器
 * <p>
 * 提供通知渠道（邮件、短信、微信等）配置的查询与保存接口。
 * 前端通过切换渠道 Tab 来查看/编辑各渠道的整体配置（非多行明细）。
 *
 * @author ronger
 */
@Tag(name = "通知渠道配置", description = "通知渠道配置管理接口")
@AdminController
@RequestMapping("/notification/channels")
@PreAuthorize("hasRole('ADMIN')")
public class NotificationChannelConfigController {

    @Resource
    private NotificationChannelConfigService notificationChannelConfigService;

    @Operation(summary = "查询所有渠道配置", description = "返回所有已定义渠道的配置（含 Schema 字段定义 + 当前值），密码字段已脱敏")
    @GetMapping
    @ApiLog("查询通知渠道配置列表")
    public GlobalResult<List<ChannelConfigVO>> listChannels() {
        return GlobalResult.success(notificationChannelConfigService.listAllChannels());
    }

    @Operation(summary = "查询指定渠道配置", description = "返回指定渠道的配置详情，密码字段已脱敏")
    @GetMapping("/{channel}")
    @ApiLog("查询通知渠道配置详情")
    public GlobalResult<ChannelConfigVO> getChannel(
            @Parameter(description = "渠道标识，如 email、sms、WeChat", required = true)
            @PathVariable String channel) {
        return GlobalResult.success(notificationChannelConfigService.getChannel(channel));
    }

    @Operation(summary = "保存渠道配置", description = "全量覆盖保存指定渠道的配置，保存后立即刷新缓存")
    @PutMapping("/{channel}")
    @ApiLog("保存通知渠道配置")
    @OperationLog(module = "通知渠道配置", operation = "保存渠道配置", recordParams = true)
    public GlobalResult<Void> saveChannel(
            @Parameter(description = "渠道标识，如 email、sms、WeChat", required = true)
            @PathVariable String channel,
            @Parameter(description = "配置内容", required = true)
            @Valid @RequestBody ChannelConfigSaveRequest request) {
        notificationChannelConfigService.saveChannel(channel, request);
        return GlobalResult.success();
    }
}
