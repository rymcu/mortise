package com.rymcu.mortise.core.model;

import java.util.Map;

/**
 * 内容审核请求。
 *
 * @param bizType        业务类型（如 article/comment/product）
 * @param bizId          业务主键
 * @param operatorUserId 操作用户 ID
 * @param title          标题（可为空）
 * @param content        正文内容
 * @param metadata       额外上下文（如语言、客户端、标签等）
 */
public record ContentModerationRequest(
        String bizType,
        Long bizId,
        Long operatorUserId,
        String title,
        String content,
        Map<String, Object> metadata
) {
}
