package com.rymcu.mortise.log.spi;

import com.rymcu.mortise.log.entity.ApiLogEntity;
import com.rymcu.mortise.log.entity.OperationLogEntity;

/**
 * 日志存储 SPI 接口
 * 业务模块可实现此接口来自定义日志存储方式
 * 例如：存储到数据库、发送到 ELK、Loki 等日志系统
 *
 * @author ronger
 */
public interface LogStorage {

    /**
     * 获取优先级，数字越小优先级越高
     * 多个实现时，优先级高的会被优先调用
     */
    default int getOrder() {
        return 100;
    }

    // ==================== 操作日志相关方法 ====================

    /**
     * 同步保存操作日志
     *
     * @param log 操作日志实体
     */
    void save(OperationLogEntity log);

    /**
     * 异步保存操作日志
     * 默认实现为调用同步保存方法
     *
     * @param log 操作日志实体
     */
    default void saveAsync(OperationLogEntity log) {
        save(log);
    }

    /**
     * 批量保存操作日志
     * 默认实现为循环调用 save
     *
     * @param logs 操作日志列表
     */
    default void saveBatch(java.util.List<OperationLogEntity> logs) {
        if (logs != null && !logs.isEmpty()) {
            logs.forEach(this::save);
        }
    }

    // ==================== API 日志相关方法 ====================

    /**
     * 同步保存 API 日志
     *
     * @param log API日志实体
     */
    default void saveApiLog(ApiLogEntity log) {
        // 默认空实现，子类可选择性覆盖
    }

    /**
     * 异步保存 API 日志
     * 默认实现为调用同步保存方法
     *
     * @param log API日志实体
     */
    default void saveApiLogAsync(ApiLogEntity log) {
        saveApiLog(log);
    }

    /**
     * 批量保存 API 日志
     * 默认实现为循环调用 saveApiLog
     *
     * @param logs API日志列表
     */
    default void saveApiLogBatch(java.util.List<ApiLogEntity> logs) {
        if (logs != null && !logs.isEmpty()) {
            logs.forEach(this::saveApiLog);
        }
    }
}
