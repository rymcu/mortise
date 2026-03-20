package com.rymcu.mortise.agent.service;

import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.agent.entity.Conversation;

/**
 * 会话服务接口
 *
 * @author ronger
 */
public interface ConversationService extends IService<Conversation> {

    /**
     * 获取或创建会话，同时校验归属关系
     *
     * @param conversationId 会话 ID（可为 null，此时自动创建）
     * @param userId         当前用户 ID
     * @param firstMessage   首条消息内容（仅新建时用于生成标题）
     * @return 会话实体
     */
    Conversation getOrCreate(String conversationId, Long userId, String firstMessage);

    /**
     * 校验会话归属关系
     *
     * @param conversationId 会话 ID
     * @param userId         当前用户 ID
     * @return 会话实体
     * @throws com.rymcu.mortise.core.exception.BusinessException 当会话不存在或不属于该用户时抛出
     */
    Conversation validateOwnership(Long conversationId, Long userId);
}
