package com.rymcu.mortise.member.enumerate;

import lombok.Getter;

/**
 * 会员等级枚举
 * <p>
 * 根据积分区间确定会员等级，支持阈值判断和标签解析。
 *
 * @author ronger
 */
@Getter
public enum MemberLevel {

    NOVICE("Lv.1 新手", "normal", 0),
    BEGINNER("Lv.2 入门者", "beginner", 100),
    CONTRIBUTOR("Lv.3 贡献者", "contributor", 500),
    EXPERT("Lv.4 专家", "expert", 2_000),
    EVANGELIST("Lv.5 布道师", "evangelist", 10_000);

    /** 显示标签 */
    private final String label;
    /** 等级编码（存储值） */
    private final String code;
    /** 最低积分阈值（含） */
    private final int minPoints;

    MemberLevel(String label, String code, int minPoints) {
        this.label = label;
        this.code = code;
        this.minPoints = minPoints;
    }

    /**
     * 根据积分解析等级
     */
    public static MemberLevel fromPoints(int points) {
        int safePoints = Math.max(points, 0);
        MemberLevel[] levels = values();
        // 从最高等级向下匹配
        for (int i = levels.length - 1; i >= 0; i--) {
            if (safePoints >= levels[i].minPoints) {
                return levels[i];
            }
        }
        return NOVICE;
    }

    /**
     * 根据积分获取标签
     */
    public static String resolveLabel(Integer points) {
        int safePoints = points != null ? points : 0;
        return fromPoints(safePoints).getLabel();
    }

    /** 默认注册等级编码 */
    public static final String DEFAULT_LEVEL_CODE = NOVICE.code;
}
