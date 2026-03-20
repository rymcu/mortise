package com.rymcu.mortise.agent.admin.controller;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.agent.admin.model.AiProviderInfo;
import com.rymcu.mortise.agent.admin.model.AiProviderSearch;
import com.rymcu.mortise.agent.admin.service.AdminAiProviderService;
import com.rymcu.mortise.agent.entity.AiProvider;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.web.annotation.AdminController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * AI 提供商管理控制器
 *
 * @author ronger
 */
@Tag(name = "AI 提供商管理", description = "AI 提供商的增删改查及状态管理")
@AdminController
@RequestMapping("/agent/providers")
@RequiredArgsConstructor
public class AiProviderController {

    private final AdminAiProviderService adminAiProviderService;

    @Operation(summary = "获取提供商列表", description = "分页查询 AI 提供商信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('agent:provider:list')")
    @ApiLog(recordParams = false, recordResponseBody = false, value = "查询 AI 提供商列表")
    public GlobalResult<Page<AiProviderInfo>> listProviders(@Parameter(description = "查询条件") @Valid AiProviderSearch search) {
        Page<AiProviderInfo> page = new Page<>(search.getPageNum(), search.getPageSize());
        page = adminAiProviderService.findProviderList(page, search);
        return GlobalResult.success(page);
    }

    @Operation(summary = "获取提供商详情", description = "根据 ID 获取 AI 提供商详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "提供商不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('agent:provider:query')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "获取 AI 提供商详情")
    public GlobalResult<AiProviderInfo> getProviderById(
            @Parameter(description = "提供商 ID", required = true) @PathVariable("id") Long id) {
        return GlobalResult.success(adminAiProviderService.findProviderInfoById(id));
    }

    @Operation(summary = "新增提供商", description = "创建新的 AI 提供商")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('agent:provider:add')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "新增 AI 提供商")
    @OperationLog(module = "AI 配置", operation = "新增提供商", recordParams = true)
    public GlobalResult<Boolean> addProvider(
            @Parameter(description = "提供商信息", required = true) @Valid @RequestBody AiProvider provider) {
        return GlobalResult.success(adminAiProviderService.save(provider));
    }

    @Operation(summary = "编辑提供商", description = "更新 AI 提供商信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "提供商不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('agent:provider:edit')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "编辑 AI 提供商")
    @OperationLog(module = "AI 配置", operation = "编辑提供商", recordParams = true)
    public GlobalResult<Boolean> updateProvider(
            @Parameter(description = "提供商 ID", required = true) @PathVariable("id") Long id,
            @Parameter(description = "提供商信息", required = true) @Valid @RequestBody AiProvider provider) {
        provider.setId(id);
        return GlobalResult.success(adminAiProviderService.updateById(provider));
    }

    @Operation(summary = "删除提供商", description = "逻辑删除 AI 提供商")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "提供商不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('agent:provider:delete')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "删除 AI 提供商")
    @OperationLog(module = "AI 配置", operation = "删除提供商", recordParams = true)
    public GlobalResult<Boolean> deleteProvider(
            @Parameter(description = "提供商 ID", required = true) @PathVariable("id") Long id) {
        return GlobalResult.success(adminAiProviderService.removeById(id));
    }

    @Operation(summary = "启用提供商", description = "启用指定 AI 提供商")
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('agent:provider:edit')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "启用 AI 提供商")
    @OperationLog(module = "AI 配置", operation = "启用提供商", recordParams = true)
    public GlobalResult<Boolean> enableProvider(
            @Parameter(description = "提供商 ID", required = true) @PathVariable("id") Long id) {
        return GlobalResult.success(adminAiProviderService.enableProvider(id));
    }

    @Operation(summary = "禁用提供商", description = "禁用指定 AI 提供商")
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('agent:provider:edit')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "禁用 AI 提供商")
    @OperationLog(module = "AI 配置", operation = "禁用提供商", recordParams = true)
    public GlobalResult<Boolean> disableProvider(
            @Parameter(description = "提供商 ID", required = true) @PathVariable("id") Long id) {
        return GlobalResult.success(adminAiProviderService.disableProvider(id));
    }

    @Operation(summary = "更新提供商状态", description = "更新 AI 提供商状态")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('agent:provider:edit')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "更新 AI 提供商状态")
    @OperationLog(module = "AI 配置", operation = "更新提供商状态", recordParams = true)
    public GlobalResult<Boolean> updateProviderStatus(
            @Parameter(description = "提供商 ID", required = true) @PathVariable("id") Long id,
            @Parameter(description = "状态信息", required = true) @Valid @RequestBody AiProviderInfo providerInfo) {
        return GlobalResult.success(adminAiProviderService.updateStatus(id, providerInfo.status()));
    }
}
