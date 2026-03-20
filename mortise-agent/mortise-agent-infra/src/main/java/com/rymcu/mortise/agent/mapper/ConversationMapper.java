package com.rymcu.mortise.agent.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.agent.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话 Mapper
 *
 * @author ronger
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}
