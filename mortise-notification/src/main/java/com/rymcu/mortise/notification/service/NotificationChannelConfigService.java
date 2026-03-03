package com.rymcu.mortise.notification.service;

import com.rymcu.mortise.notification.model.ChannelConfigSaveRequest;
import com.rymcu.mortise.notification.model.ChannelConfigVO;

import java.util.List;

/**
 * 通知渠道配置管理服务
 * <p>
 * 对管理端提供渠道配置的 CRUD 能力，
 * 内部将数据库多行数据聚合为单个 {@link ChannelConfigVO} 对象返回给前端。
 *
 * @author ronger
 */
public interface NotificationChannelConfigService {

    /**
     * 查询所有已定义渠道的配置（不论是否已在 DB 中保存过）
     *
     * @return 渠道配置列表，每个渠道一个聚合对象
     */
    List<ChannelConfigVO> listAllChannels();

    /**
     * 查询指定渠道的配置
     *
     * @param channel 渠道 code，如 "email"、"sms"
     * @return 渠道配置聚合对象
     */
    ChannelConfigVO getChannel(String channel);

    /**
     * 保存（全量覆盖）指定渠道的配置
     * <p>
     * 内部采用先删后写策略，并在完成后主动刷新缓存。
     *
     * @param channel 渠道 code
     * @param request 保存请求（enabled + values）
     */
    void saveChannel(String channel, ChannelConfigSaveRequest request);
}
