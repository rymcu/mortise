package com.rymcu.mortise.log.service.impl;

import com.rymcu.mortise.log.entity.ApiLogEntity;
import com.rymcu.mortise.log.entity.OperationLogEntity;
import com.rymcu.mortise.log.service.LogService;
import com.rymcu.mortise.log.spi.LogStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 日志服务实现
 * 收集所有 LogStorage 实现并调用
 *
 * @author ronger
 */
@Slf4j
@Service
public class LogServiceImpl implements LogService {

    private final List<LogStorage> logStorages;

    /**
     * 构造函数注入（使用 Optional 处理可选依赖）
     */
    @Autowired
    public LogServiceImpl(Optional<List<LogStorage>> storagesOptional) {
        this.logStorages = storagesOptional.orElse(null);
    }

    @Override
    public void recordLog(OperationLogEntity logEntity) {
        if (logStorages == null || logStorages.isEmpty()) {
            log.warn("没有配置日志存储实现，操作日志将被丢弃");
            return;
        }

        // 按优先级排序并调用所有实现
        logStorages.stream()
                .sorted(Comparator.comparingInt(LogStorage::getOrder))
                .forEach(storage -> {
                    try {
                        storage.save(logEntity);
                    } catch (Exception e) {
                        log.error("操作日志存储失败: {}", storage.getClass().getSimpleName(), e);
                    }
                });
    }

    @Async
    @Override
    public void recordLogAsync(OperationLogEntity logEntity) {
        if (logStorages == null || logStorages.isEmpty()) {
            log.warn("没有配置日志存储实现，操作日志将被丢弃");
            return;
        }

        logStorages.stream()
                .sorted(Comparator.comparingInt(LogStorage::getOrder))
                .forEach(storage -> {
                    try {
                        storage.saveAsync(logEntity);
                    } catch (Exception e) {
                        log.error("异步操作日志存储失败: {}", storage.getClass().getSimpleName(), e);
                    }
                });
    }

    @Override
    public void recordApiLog(ApiLogEntity logEntity) {
        if (logStorages == null || logStorages.isEmpty()) {
            log.warn("没有配置日志存储实现，API日志将被丢弃");
            return;
        }

        // 按优先级排序并调用所有实现
        logStorages.stream()
                .sorted(Comparator.comparingInt(LogStorage::getOrder))
                .forEach(storage -> {
                    try {
                        storage.saveApiLog(logEntity);
                    } catch (Exception e) {
                        log.error("API日志存储失败: {}", storage.getClass().getSimpleName(), e);
                    }
                });
    }

    @Async
    @Override
    public void recordApiLogAsync(ApiLogEntity logEntity) {
        if (logStorages == null || logStorages.isEmpty()) {
            log.warn("没有配置日志存储实现，API日志将被丢弃");
            return;
        }

        logStorages.stream()
                .sorted(Comparator.comparingInt(LogStorage::getOrder))
                .forEach(storage -> {
                    try {
                        storage.saveApiLogAsync(logEntity);
                    } catch (Exception e) {
                        log.error("异步API日志存储失败: {}", storage.getClass().getSimpleName(), e);
                    }
                });
    }
}
