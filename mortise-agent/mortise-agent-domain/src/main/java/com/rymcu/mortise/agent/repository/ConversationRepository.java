package com.rymcu.mortise.agent.repository;

import com.rymcu.mortise.agent.entity.Conversation;

import java.util.List;

/**
 * 会话仓储端口。
 */
public interface ConversationRepository {

    Conversation findById(Long id);

    List<Conversation> findEnabledByUserId(Long userId);

    boolean save(Conversation conversation);

    boolean deleteById(Long id);
}
