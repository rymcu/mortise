package com.rymcu.mortise.system.storage;

import com.rymcu.mortise.log.entity.OperationLogEntity;
import com.rymcu.mortise.log.spi.LogStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 系统日志存储实现
 * 实现 LogStorage SPI，提供日志存储能力
 * 
 * 当前实现：仅记录到日志文件（通过 Logback）
 * 未来可扩展：存储到数据库、ELK、Loki 等
 *
 * @author ronger
 */
@Slf4j
@Component
public class SystemLogStorage implements LogStorage {

    @Override
    public int getOrder() {
        return 100; // 默认优先级
    }

    @Override
    public void save(OperationLogEntity logEntity) {
        // 当前实现：记录到日志文件
        log.info("操作日志: module={}, operation={}, operator={}, duration={}ms, success={}", 
                logEntity.getModule(), 
                logEntity.getOperation(),
                logEntity.getOperatorAccount(),
                logEntity.getDuration(),
                logEntity.getSuccess());

        // TODO: 未来可扩展到数据库存储
        // Example:
        // operationLogMapper.insert(logEntity);
    }

    @Override
    public void saveAsync(OperationLogEntity logEntity) {
        // 异步保存，当前直接调用同步方法
        // Spring @Async 会在上层处理异步逻辑
        save(logEntity);
    }

    @Override
    public void saveBatch(List<OperationLogEntity> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }

        log.info("批量保存操作日志: count={}", logs.size());
        
        // 当前实现：循环保存
        logs.forEach(this::save);

        // TODO: 未来可优化为批量插入
        // Example:
        // operationLogMapper.insertBatch(logs);
    }
}
