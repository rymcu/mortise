package com.rymcu.mortise.core.model;

/**
 * 内容审核命中项。
 *
 * @param keyword     命中关键词
 * @param category    命中分类
 * @param level       风险等级（数字越大风险越高）
 * @param matchedText 命中文本片段
 */
public record ContentModerationHit(
        String keyword,
        String category,
        Integer level,
        String matchedText
) {
}
