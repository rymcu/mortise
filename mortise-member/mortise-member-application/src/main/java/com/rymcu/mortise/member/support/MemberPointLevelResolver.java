package com.rymcu.mortise.member.support;

import com.rymcu.mortise.member.enumerate.MemberLevel;

/**
 * 会员积分等级映射
 * <p>
 * 委托给 {@link MemberLevel} 枚举实现，保留此工具类作为统一入口。
 */
public final class MemberPointLevelResolver {

    private MemberPointLevelResolver() {
    }

    public static String resolveLabel(Integer points) {
        return MemberLevel.resolveLabel(points);
    }

    public static MemberLevel resolve(Integer points) {
        int safePoints = points != null ? points : 0;
        return MemberLevel.fromPoints(safePoints);
    }
}
