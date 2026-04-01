package com.rymcu.mortise.agent.application.service.query.conversation;

import com.rymcu.mortise.agent.application.result.ConversationInfoResult;

import java.util.List;

/**
 * 会话查询服务。
 */
public interface ConversationQueryService {

    List<ConversationInfoResult> listUserConversationInfos(Long userId);
}
