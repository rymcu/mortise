package com.rymcu.mortise.agent.application.service.query.conversation.impl;

import com.rymcu.mortise.agent.application.result.ConversationInfoResult;
import com.rymcu.mortise.agent.application.service.query.conversation.ConversationQueryService;
import com.rymcu.mortise.agent.entity.Conversation;
import com.rymcu.mortise.agent.repository.ConversationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会话查询服务实现。
 */
@Service
public class ConversationQueryServiceImpl implements ConversationQueryService {

    private final ConversationRepository conversationRepository;

    public ConversationQueryServiceImpl(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    @Override
    public List<ConversationInfoResult> listUserConversationInfos(Long userId) {
        return conversationRepository.findEnabledByUserId(userId).stream()
                .map(this::toInfo)
                .toList();
    }

    private ConversationInfoResult toInfo(Conversation conversation) {
        return new ConversationInfoResult(
                conversation.getId(),
                conversation.getUserId(),
                conversation.getTitle(),
                conversation.getStatus(),
                conversation.getCreatedTime(),
                conversation.getUpdatedTime()
        );
    }
}
