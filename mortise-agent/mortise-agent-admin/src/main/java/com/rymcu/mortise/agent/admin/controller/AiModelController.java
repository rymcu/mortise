package com.rymcu.mortise.agent.admin.controller;

import com.rymcu.mortise.agent.admin.contract.query.AiModelSearch;
import com.rymcu.mortise.agent.admin.contract.request.AiModelUpsertRequest;
import com.rymcu.mortise.agent.admin.contract.request.StatusUpdateRequest;
import com.rymcu.mortise.agent.admin.contract.response.AiModelInfo;
import com.rymcu.mortise.agent.admin.facade.AdminAiModelFacade;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * AI 模型管理控制器
 *
 * @author ronger
 */
@Tag(name = "AI 模型管理", description = "AI 模型的增删改查及状态管理")
@AdminController
@RequestMapping("/agent/models")
@RequiredArgsConstructor
public class AiModelController {

    private final AdminAiModelFacade adminAiModelFacade;

    @Operation(summary = "获取模型列表", description = "分页查询 AI 模型信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('agent:model:list')")
    @ApiLog(recordParams = false, recordResponseBody = false, value = "查询 AI 模型列表")
    public GlobalResult<PageResult<AiModelInfo>> listModels(@Parameter(description = "查询条件") @Valid AiModelSearch search) {
        return GlobalResult.success(adminAiModelFacade.findModelList(PageQuery.of(search.getPageNum(), search.getPageSize()), search));
    }

    @Operation(summary = "获取模型详情", description = "根据 ID 获取 AI 模型详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "模型不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('agent:model:query')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "获取 AI 模型详情")
    public GlobalResult<AiModelInfo> getModelById(
            @Parameter(description = "模型 ID", required = true) @PathVariable("id") Long id) {
        return GlobalResult.success(adminAiModelFacade.findModelInfoById(id));
    }

    @Operation(summary = "新增模型", description = "创建新的 AI 模型")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('agent:model:add')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "新增 AI 模型")
    @OperationLog(module = "AI 配置", operation = "新增模型", recordParams = true)
    public GlobalResult<Boolean> addModel(
            @Parameter(description = "模型信息", required = true) @Valid @RequestBody AiModelUpsertRequest request) {
        return GlobalResult.success(adminAiModelFacade.createModel(request));
    }

    @Operation(summary = "编辑模型", description = "更新 AI 模型信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "模型不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('agent:model:edit')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "编辑 AI 模型")
    @OperationLog(module = "AI 配置", operation = "编辑模型", recordParams = true)
    public GlobalResult<Boolean> updateModel(
            @Parameter(description = "模型 ID", required = true) @PathVariable("id") Long id,
            @Parameter(description = "模型信息", required = true) @Valid @RequestBody AiModelUpsertRequest request) {
        return GlobalResult.success(adminAiModelFacade.updateModel(id, request));
    }

    @Operation(summary = "删除模型", description = "逻辑删除 AI 模型")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "模型不存在"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('agent:model:delete')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "删除 AI 模型")
    @OperationLog(module = "AI 配置", operation = "删除模型", recordParams = true)
    public GlobalResult<Boolean> deleteModel(
            @Parameter(description = "模型 ID", required = true) @PathVariable("id") Long id) {
        return GlobalResult.success(adminAiModelFacade.deleteModel(id));
    }

    @Operation(summary = "启用模型", description = "启用指定 AI 模型")
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('agent:model:edit')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "启用 AI 模型")
    @OperationLog(module = "AI 配置", operation = "启用模型", recordParams = true)
    public GlobalResult<Boolean> enableModel(
            @Parameter(description = "模型 ID", required = true) @PathVariable("id") Long id) {
        return GlobalResult.success(adminAiModelFacade.enableModel(id));
    }

    @Operation(summary = "禁用模型", description = "禁用指定 AI 模型")
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('agent:model:edit')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "禁用 AI 模型")
    @OperationLog(module = "AI 配置", operation = "禁用模型", recordParams = true)
    public GlobalResult<Boolean> disableModel(
            @Parameter(description = "模型 ID", required = true) @PathVariable("id") Long id) {
        return GlobalResult.success(adminAiModelFacade.disableModel(id));
    }

    @Operation(summary = "更新模型状态", description = "更新 AI 模型状态")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('agent:model:edit')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "更新 AI 模型状态")
    @OperationLog(module = "AI 配置", operation = "更新模型状态", recordParams = true)
    public GlobalResult<Boolean> updateModelStatus(
            @Parameter(description = "模型 ID", required = true) @PathVariable("id") Long id,
            @Parameter(description = "状态信息", required = true) @Valid @RequestBody StatusUpdateRequest request) {
        return GlobalResult.success(adminAiModelFacade.updateStatus(id, request.status()));
    }
}
