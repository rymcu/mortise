package com.rymcu.mortise.system.controller.facade.impl;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.notification.model.ChannelConfigSaveRequest;
import com.rymcu.mortise.notification.model.ChannelConfigVO;
import com.rymcu.mortise.notification.service.NotificationChannelConfigService;
import com.rymcu.mortise.system.controller.facade.NotificationChannelConfigAdminFacade;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationChannelConfigAdminFacadeImpl implements NotificationChannelConfigAdminFacade {

    private final NotificationChannelConfigService notificationChannelConfigService;

    public NotificationChannelConfigAdminFacadeImpl(NotificationChannelConfigService notificationChannelConfigService) {
        this.notificationChannelConfigService = notificationChannelConfigService;
    }

    @Override
    public GlobalResult<List<ChannelConfigVO>> listChannels() {
        return GlobalResult.success(notificationChannelConfigService.listAllChannels());
    }

    @Override
    public GlobalResult<ChannelConfigVO> getChannel(String channel) {
        return GlobalResult.success(notificationChannelConfigService.getChannel(channel));
    }

    @Override
    public GlobalResult<Void> saveChannel(String channel, ChannelConfigSaveRequest request) {
        notificationChannelConfigService.saveChannel(channel, request);
        return GlobalResult.success();
    }
}
