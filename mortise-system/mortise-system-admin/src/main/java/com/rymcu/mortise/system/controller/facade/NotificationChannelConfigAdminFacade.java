package com.rymcu.mortise.system.controller.facade;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.notification.model.ChannelConfigSaveRequest;
import com.rymcu.mortise.notification.model.ChannelConfigVO;

import java.util.List;

public interface NotificationChannelConfigAdminFacade {

    GlobalResult<List<ChannelConfigVO>> listChannels();

    GlobalResult<ChannelConfigVO> getChannel(String channel);

    GlobalResult<Void> saveChannel(String channel, ChannelConfigSaveRequest request);
}
