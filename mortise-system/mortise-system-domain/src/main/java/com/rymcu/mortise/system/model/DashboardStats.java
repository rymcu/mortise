package com.rymcu.mortise.system.model;

/**
 * 仪表盘统计数据
 *
 * @author ronger
 */
public record DashboardStats(
    Long userCount,
    Long roleCount,
    Long menuCount,
    Long memberCount
) {
}
