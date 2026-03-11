package com.rymcu.mortise.member.support;

/**
 * 会员积分等级映射
 */
public final class MemberPointLevelResolver {

    private MemberPointLevelResolver() {
    }

    public static String resolveLabel(Integer points) {
        int safePoints = points != null ? Math.max(points, 0) : 0;
        if (safePoints >= 10_000) {
            return "Lv.5 布道师";
        }
        if (safePoints >= 2_000) {
            return "Lv.4 专家";
        }
        if (safePoints >= 500) {
            return "Lv.3 贡献者";
        }
        if (safePoints >= 100) {
            return "Lv.2 入门者";
        }
        return "Lv.1 新手";
    }
}
