package com.rymcu.mortise.agent.infra.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.agent.entity.Conversation;
import com.rymcu.mortise.agent.infra.persistence.PersistenceObjectMapper;
import com.rymcu.mortise.agent.infra.persistence.entity.ConversationPO;
import com.rymcu.mortise.agent.mapper.ConversationMapper;
import com.rymcu.mortise.agent.repository.ConversationRepository;
import com.rymcu.mortise.common.enumerate.Status;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.rymcu.mortise.agent.infra.persistence.entity.table.ConversationPOTableDef.CONVERSATION_PO;

/**
 * 会话仓储实现。
 */
@Repository
public class ConversationRepositoryImpl implements ConversationRepository {

    private final ConversationMapper conversationMapper;

    public ConversationRepositoryImpl(ConversationMapper conversationMapper) {
        this.conversationMapper = conversationMapper;
    }

    @Override
    public Conversation findById(Long id) {
        return PersistenceObjectMapper.copy(conversationMapper.selectOneById(id), Conversation::new);
    }

    @Override
    public List<Conversation> findEnabledByUserId(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(CONVERSATION_PO.USER_ID.eq(userId))
                .and(CONVERSATION_PO.STATUS.eq(Status.ENABLED.getCode()))
                .orderBy(CONVERSATION_PO.UPDATED_TIME.desc());
        return PersistenceObjectMapper.copyList(conversationMapper.selectListByQuery(queryWrapper), Conversation::new);
    }

    @Override
    public boolean save(Conversation conversation) {
        ConversationPO conversationPO = PersistenceObjectMapper.copy(conversation, ConversationPO::new);
        boolean saved = conversationMapper.insertSelective(conversationPO) > 0;
        if (saved) {
            conversation.setId(conversationPO.getId());
        }
        return saved;
    }

    @Override
    public boolean deleteById(Long id) {
        return conversationMapper.deleteById(id) > 0;
    }
}
