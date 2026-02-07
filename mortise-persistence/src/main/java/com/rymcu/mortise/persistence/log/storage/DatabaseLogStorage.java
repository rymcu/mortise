package com.rymcu.mortise.persistence.log.storage;

import com.rymcu.mortise.log.entity.ApiLogEntity;
import com.rymcu.mortise.log.entity.OperationLogEntity;
import com.rymcu.mortise.log.spi.LogStorage;
import com.rymcu.mortise.persistence.log.entity.ApiLog;
import com.rymcu.mortise.persistence.log.entity.OperationLog;
import com.rymcu.mortise.persistence.log.mapper.ApiLogMapper;
import com.rymcu.mortise.persistence.log.mapper.OperationLogMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库日志存储实现
 * 实现 LogStorage SPI，提供日志数据库存储能力
 * <p>
 * 功能：
 * 1. 同步/异步保存到数据库
 * 2. 批量保存优化
 * 3. 同时输出到日志文件（便于调试）
 * <p>
 * 位置：mortise-persistence 模块（所有业务模块共享）
 *
 * @author ronger
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "mortise.log", name = "use-system-storage", havingValue = "false", matchIfMissing = true)
public class DatabaseLogStorage implements LogStorage {

    @Resource
    private OperationLogMapper operationLogMapper;

    @Resource
    private ApiLogMapper apiLogMapper;

    @Override
    public int getOrder() {
        return 100; // 默认优先级
    }

    // ==================== 操作日志 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(OperationLogEntity logEntity) {
        // 1. 记录到日志文件（便于实时查看）
        log.info("操作日志: clientType={}, module={}, operation={}, operator={}, operatorId={}, uri={}, method={}, ip={}, duration={}ms, success={}, traceId={}",
                logEntity.getClientType(),
                logEntity.getModule(),
                logEntity.getOperation(),
                logEntity.getOperatorAccount(),
                logEntity.getOperatorId(),
                logEntity.getRequestUri(),
                logEntity.getRequestMethod(),
                logEntity.getIp(),
                logEntity.getDuration(),
                logEntity.getSuccess(),
                logEntity.getTraceId());

        // 2. 保存到数据库
        try {
            OperationLog dbEntity = convertToOperationLog(logEntity);
            operationLogMapper.insert(dbEntity);
        } catch (Exception e) {
            log.error("操作日志保存到数据库失败: {}", e.getMessage(), e);
        }
    }

    @Async
    @Override
    public void saveAsync(OperationLogEntity logEntity) {
        save(logEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatch(List<OperationLogEntity> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }

        log.info("批量保存操作日志: count={}", logs.size());

        try {
            List<OperationLog> dbEntities = logs.stream()
                    .map(this::convertToOperationLog)
                    .collect(Collectors.toList());
            operationLogMapper.insertBatch(dbEntities);
        } catch (Exception e) {
            log.error("批量操作日志保存到数据库失败: {}", e.getMessage(), e);
            // 降级：逐条保存
            logs.forEach(this::save);
        }
    }

    // ==================== API 日志 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveApiLog(ApiLogEntity logEntity) {
        // 1. 记录到日志文件
        log.info("API日志: clientType={}, api={}, uri={}, method={}, user={}, userId={}, ip={}, status={}, duration={}ms, success={}, traceId={}",
                logEntity.getClientType(),
                logEntity.getApiDescription(),
                logEntity.getRequestUri(),
                logEntity.getRequestMethod(),
                logEntity.getUsername(),
                logEntity.getUserId(),
                logEntity.getClientIp(),
                logEntity.getHttpStatus(),
                logEntity.getDuration(),
                logEntity.getSuccess(),
                logEntity.getTraceId());

        // 2. 保存到数据库
        try {
            ApiLog dbEntity = convertToApiLog(logEntity);
            apiLogMapper.insert(dbEntity);
        } catch (Exception e) {
            log.error("API日志保存到数据库失败: {}", e.getMessage(), e);
        }
    }

    @Async
    @Override
    public void saveApiLogAsync(ApiLogEntity logEntity) {
        saveApiLog(logEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveApiLogBatch(List<ApiLogEntity> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }

        log.info("批量保存API日志: count={}", logs.size());

        try {
            List<ApiLog> dbEntities = logs.stream()
                    .map(this::convertToApiLog)
                    .collect(Collectors.toList());
            apiLogMapper.insertBatch(dbEntities);
        } catch (Exception e) {
            log.error("批量API日志保存到数据库失败: {}", e.getMessage(), e);
            // 降级：逐条保存
            logs.forEach(this::saveApiLog);
        }
    }

    // ==================== 转换方法 ====================

    /**
     * 将 OperationLogEntity 转换为数据库实体
     */
    private OperationLog convertToOperationLog(OperationLogEntity entity) {
        return OperationLog.builder()
                .id(entity.getId())
                .traceId(entity.getTraceId())
                .clientType(entity.getClientType())
                .module(entity.getModule())
                .operation(entity.getOperation())
                .operatorId(entity.getOperatorId())
                .operatorAccount(entity.getOperatorAccount())
                .operateTime(entity.getOperateTime())
                .method(truncate(entity.getMethod(), 500))
                .requestUri(truncate(entity.getRequestUri(), 500))
                .requestMethod(entity.getRequestMethod())
                .params(entity.getParams())
                .result(entity.getResult())
                .ip(entity.getIp())
                .userAgent(truncate(entity.getUserAgent(), 500))
                .duration(entity.getDuration())
                .success(entity.getSuccess())
                .errorMsg(entity.getErrorMsg())
                .build();
    }

    /**
     * 将 ApiLogEntity 转换为数据库实体
     */
    private ApiLog convertToApiLog(ApiLogEntity entity) {
        return ApiLog.builder()
                .id(entity.getId())
                .traceId(entity.getTraceId())
                .clientType(entity.getClientType())
                .apiDescription(truncate(entity.getApiDescription(), 500))
                .className(truncate(entity.getClassName(), 300))
                .methodName(truncate(entity.getMethodName(), 100))
                .userId(entity.getUserId())
                .username(truncate(entity.getUsername(), 100))
                .requestTime(entity.getRequestTime())
                .requestUri(truncate(entity.getRequestUri(), 500))
                .requestMethod(entity.getRequestMethod())
                .queryString(entity.getQueryString())
                .requestHeaders(entity.getRequestHeaders())
                .requestBody(entity.getRequestBody())
                .responseBody(entity.getResponseBody())
                .httpStatus(entity.getHttpStatus())
                .clientIp(entity.getClientIp())
                .userAgent(truncate(entity.getUserAgent(), 500))
                .duration(entity.getDuration())
                .success(entity.getSuccess())
                .errorMsg(entity.getErrorMsg())
                .build();
    }

    /**
     * 截断字符串，防止超出数据库字段长度
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }
}
