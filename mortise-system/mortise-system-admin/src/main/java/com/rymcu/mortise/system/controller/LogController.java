package com.rymcu.mortise.system.controller;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.persistence.log.entity.ApiLog;
import com.rymcu.mortise.persistence.log.entity.OperationLog;
import com.rymcu.mortise.system.model.LogSearch;
import com.rymcu.mortise.system.service.LogQueryService;
import com.rymcu.mortise.web.annotation.AdminController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 日志管理控制器
 * 提供操作日志和 API 访问日志的查询与删除功能
 *
 * @author ronger
 */
@Tag(name = "日志管理", description = "操作日志与 API 日志管理接口")
@AdminController
@RequestMapping("/logs")
public class LogController {

    @Resource
    private LogQueryService logQueryService;

    // ==================== 操作日志 ====================

    @Operation(summary = "分页查询操作日志", description = "支持关键词、客户端类型、模块、时间范围过滤")
    @GetMapping("/operation")
    @PreAuthorize("hasAuthority('system:operation-log:list')")
    @com.rymcu.mortise.log.annotation.ApiLog(recordParams = false, recordResponseBody = false, value = "查询操作日志")
    public GlobalResult<Page<OperationLog>> listOperationLogs(
            @Parameter(description = "查询条件") @Valid LogSearch search) {
        Page<OperationLog> page = new Page<>(search.getPageNum(), search.getPageSize());
        page = logQueryService.findOperationLogs(page, search);
        return GlobalResult.success(page);
    }

    @Operation(summary = "删除操作日志", description = "根据 ID 删除指定操作日志记录")
    @DeleteMapping("/operation/{id}")
    @PreAuthorize("hasAuthority('system:operation-log:delete')")
    @com.rymcu.mortise.log.annotation.ApiLog(value = "删除操作日志")
    @com.rymcu.mortise.log.annotation.OperationLog(module = "日志管理", operation = "删除操作日志", recordParams = true)
    public GlobalResult<Boolean> deleteOperationLog(
            @Parameter(description = "日志ID", required = true) @PathVariable Long id) {
        return GlobalResult.success(logQueryService.deleteOperationLog(id));
    }

    // ==================== API 日志 ====================

    @Operation(summary = "分页查询 API 日志", description = "支持关键词、客户端类型、时间范围过滤")
    @GetMapping("/api")
    @PreAuthorize("hasAuthority('system:api-log:list')")
    @com.rymcu.mortise.log.annotation.ApiLog(recordParams = false, recordResponseBody = false, value = "查询 API 日志")
    public GlobalResult<Page<ApiLog>> listApiLogs(
            @Parameter(description = "查询条件") @Valid LogSearch search) {
        Page<ApiLog> page = new Page<>(search.getPageNum(), search.getPageSize());
        page = logQueryService.findApiLogs(page, search);
        return GlobalResult.success(page);
    }

    @Operation(summary = "删除 API 日志", description = "根据 ID 删除指定 API 日志记录")
    @DeleteMapping("/api/{id}")
    @PreAuthorize("hasAuthority('system:api-log:delete')")
    @com.rymcu.mortise.log.annotation.ApiLog(value = "删除 API 日志")
    @com.rymcu.mortise.log.annotation.OperationLog(module = "日志管理", operation = "删除 API 日志", recordParams = true)
    public GlobalResult<Boolean> deleteApiLog(
            @Parameter(description = "日志ID", required = true) @PathVariable Long id) {
        return GlobalResult.success(logQueryService.deleteApiLog(id));
    }
}
