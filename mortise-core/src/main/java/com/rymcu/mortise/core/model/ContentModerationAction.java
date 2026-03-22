package com.rymcu.mortise.core.model;

/**
 * 内容审核建议动作。
 */
public enum ContentModerationAction {
    /**
     * 允许通过。
     */
    PASS,
    /**
     * 建议进入人工复审。
     */
    REVIEW,
    /**
     * 建议直接拒绝。
     */
    REJECT,
    /**
     * 建议对命中片段做脱敏后再处理。
     */
    MASK
}
