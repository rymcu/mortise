package com.rymcu.mortise.core.model;

import java.util.List;

/**
 * 内容审核结果。
 *
 * @param passed 是否通过
 * @param action 建议动作
 * @param hits   命中详情
 * @param reason 说明
 * @param score  风险分（0-1，数值越高风险越高）
 */
public record ContentModerationResult(
        boolean passed,
        ContentModerationAction action,
        List<ContentModerationHit> hits,
        String reason,
        Double score
) {

    /**
     * 直接放行的默认结果。
     *
     * @return 放行结果
     */
    public static ContentModerationResult pass() {
        return new ContentModerationResult(true, ContentModerationAction.PASS, List.of(), "default-pass", 0D);
    }
}
