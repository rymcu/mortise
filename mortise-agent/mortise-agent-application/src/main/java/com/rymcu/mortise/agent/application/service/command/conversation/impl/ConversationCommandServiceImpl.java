package com.rymcu.mortise.agent.application.service.command.conversation.impl;

import com.rymcu.mortise.agent.application.service.command.conversation.ConversationCommandService;
import com.rymcu.mortise.agent.entity.Conversation;
import com.rymcu.mortise.agent.repository.ConversationRepository;
import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 会话命令服务实现。
 */
@Service
public class ConversationCommandServiceImpl implements ConversationCommandService {

    private static final int MAX_TITLE_LENGTH = 50;

    private final ConversationRepository conversationRepository;

    public ConversationCommandServiceImpl(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    @Override
    public Conversation getOrCreate(String conversationId, Long userId, String firstMessage) {
        if (StringUtils.hasText(conversationId)) {
            try {
                Long id = Long.parseLong(conversationId);
                return validateOwnership(id, userId);
            } catch (NumberFormatException e) {
                throw new BusinessException("会话 ID 格式无效");
            }
        }
        return createConversation(userId, firstMessage);
    }

    @Override
    public Long getOrCreateConversationId(String conversationId, Long userId, String firstMessage) {
        Conversation conversation = getOrCreate(conversationId, userId, firstMessage);
        return conversation.getId();
    }

    @Override
    public Conversation validateOwnership(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId);
        if (conversation == null) {
            throw new BusinessException("会话不存在");
        }
        if (!Objects.equals(conversation.getUserId(), userId)) {
            throw new BusinessException("无权访问该会话");
        }
        return conversation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long conversationId, Long userId) {
        validateOwnership(conversationId, userId);
        conversationRepository.deleteById(conversationId);
    }

    @Transactional(rollbackFor = Exception.class)
    private Conversation createConversation(Long userId, String firstMessage) {
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle(generateTitle(firstMessage));
        conversation.setStatus(Status.ENABLED.getCode());
        conversationRepository.save(conversation);
        return conversation;
    }

    private String generateTitle(String firstMessage) {
        if (!StringUtils.hasText(firstMessage)) {
            return "新会话";
        }
        if (firstMessage.length() <= MAX_TITLE_LENGTH) {
            return firstMessage;
        }
        return firstMessage.substring(0, MAX_TITLE_LENGTH) + "...";
    }
}
