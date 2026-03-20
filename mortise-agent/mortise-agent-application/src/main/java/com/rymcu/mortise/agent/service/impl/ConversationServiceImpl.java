package com.rymcu.mortise.agent.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.agent.entity.Conversation;
import com.rymcu.mortise.agent.mapper.ConversationMapper;
import com.rymcu.mortise.agent.service.ConversationService;
import com.rymcu.mortise.common.enumerate.Status;
import com.rymcu.mortise.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 会话服务实现
 *
 * @author ronger
 */
@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    /** 会话标题最大长度 */
    private static final int MAX_TITLE_LENGTH = 50;

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
    public Conversation validateOwnership(Long conversationId, Long userId) {
        Conversation conversation = getById(conversationId);
        if (conversation == null) {
            throw new BusinessException("会话不存在");
        }
        if (!Objects.equals(conversation.getUserId(), userId)) {
            throw new BusinessException("无权访问该会话");
        }
        return conversation;
    }

    private Conversation createConversation(Long userId, String firstMessage) {
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle(generateTitle(firstMessage));
        conversation.setStatus(Status.ENABLED.getCode());
        save(conversation);
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
