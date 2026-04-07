package com.rymcu.mortise.voice.admin.controller;

import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.voice.admin.contract.query.VoiceModelSearch;
import com.rymcu.mortise.voice.admin.contract.query.VoiceProfileSearch;
import com.rymcu.mortise.voice.admin.contract.query.VoiceProviderSearch;
import com.rymcu.mortise.voice.admin.contract.request.StatusUpdateRequest;
import com.rymcu.mortise.voice.admin.contract.request.VoiceModelUpsertRequest;
import com.rymcu.mortise.voice.admin.contract.request.VoiceProfileUpsertRequest;
import com.rymcu.mortise.voice.admin.contract.request.VoiceProviderUpsertRequest;
import com.rymcu.mortise.voice.admin.contract.response.VoiceModelInfo;
import com.rymcu.mortise.voice.admin.contract.response.VoiceProfileInfo;
import com.rymcu.mortise.voice.admin.contract.response.VoiceProviderInfo;
import com.rymcu.mortise.voice.admin.facade.AdminVoiceCatalogFacade;
import com.rymcu.mortise.web.annotation.AdminController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 管理端语音目录控制器。
 */
@Tag(name = "语音目录管理", description = "提供商、模型、配置目录管理")
@AdminController
@RequestMapping("/voice")
@RequiredArgsConstructor
public class VoiceCatalogController {

    private final AdminVoiceCatalogFacade adminVoiceCatalogFacade;

    @Operation(summary = "获取语音提供商列表")
    @GetMapping("/providers")
    @PreAuthorize("hasAuthority('voice:provider:list')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "查询语音提供商列表")
    public GlobalResult<PageResult<VoiceProviderInfo>> listProviders(
            @Parameter(description = "查询条件") @Valid VoiceProviderSearch search
    ) {
        return GlobalResult.success(adminVoiceCatalogFacade.findProviderPage(
                PageQuery.of(search.getPageNum(), search.getPageSize()),
                search
        ));
    }

    @Operation(summary = "获取语音提供商选项")
    @GetMapping("/providers/options")
    @PreAuthorize("hasAuthority('voice:provider:list')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "查询语音提供商选项")
    public GlobalResult<List<VoiceProviderInfo>> listProviderOptions(
            @RequestParam(value = "enabledOnly", required = false) Boolean enabledOnly
    ) {
        return GlobalResult.success(adminVoiceCatalogFacade.listProviderOptions(enabledOnly));
    }

    @Operation(summary = "获取语音提供商详情")
    @GetMapping("/providers/{id}")
    @PreAuthorize("hasAuthority('voice:provider:query')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "查询语音提供商详情")
    public GlobalResult<VoiceProviderInfo> getProviderById(@PathVariable("id") Long id) {
        return GlobalResult.success(adminVoiceCatalogFacade.findProviderById(id));
    }

    @Operation(summary = "新增语音提供商")
    @PostMapping("/providers")
    @PreAuthorize("hasAuthority('voice:provider:add')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "新增语音提供商")
    @OperationLog(module = "语音配置", operation = "新增语音提供商", recordParams = true)
    public GlobalResult<Boolean> createProvider(@Valid @RequestBody VoiceProviderUpsertRequest request) {
        return GlobalResult.success(adminVoiceCatalogFacade.createProvider(request));
    }

    @Operation(summary = "更新语音提供商")
    @PutMapping("/providers/{id}")
    @PreAuthorize("hasAuthority('voice:provider:edit')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "更新语音提供商")
    @OperationLog(module = "语音配置", operation = "更新语音提供商", recordParams = true)
    public GlobalResult<Boolean> updateProvider(
            @PathVariable("id") Long id,
            @Valid @RequestBody VoiceProviderUpsertRequest request
    ) {
        return GlobalResult.success(adminVoiceCatalogFacade.updateProvider(id, request));
    }

    @Operation(summary = "删除语音提供商")
    @DeleteMapping("/providers/{id}")
    @PreAuthorize("hasAuthority('voice:provider:delete')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "删除语音提供商")
    @OperationLog(module = "语音配置", operation = "删除语音提供商", recordParams = true)
    public GlobalResult<Boolean> deleteProvider(@PathVariable("id") Long id) {
        return GlobalResult.success(adminVoiceCatalogFacade.deleteProvider(id));
    }

    @Operation(summary = "启用语音提供商")
    @PutMapping("/providers/{id}/enable")
    @PreAuthorize("hasAuthority('voice:provider:edit')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "启用语音提供商")
    @OperationLog(module = "语音配置", operation = "启用语音提供商", recordParams = true)
    public GlobalResult<Boolean> enableProvider(@PathVariable("id") Long id) {
        return GlobalResult.success(adminVoiceCatalogFacade.updateProviderStatus(id, 1));
    }

    @Operation(summary = "禁用语音提供商")
    @PutMapping("/providers/{id}/disable")
    @PreAuthorize("hasAuthority('voice:provider:edit')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "禁用语音提供商")
    @OperationLog(module = "语音配置", operation = "禁用语音提供商", recordParams = true)
    public GlobalResult<Boolean> disableProvider(@PathVariable("id") Long id) {
        return GlobalResult.success(adminVoiceCatalogFacade.updateProviderStatus(id, 0));
    }

    @Operation(summary = "更新语音提供商状态")
    @PatchMapping("/providers/{id}/status")
    @PreAuthorize("hasAuthority('voice:provider:edit')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "更新语音提供商状态")
    @OperationLog(module = "语音配置", operation = "更新语音提供商状态", recordParams = true)
    public GlobalResult<Boolean> updateProviderStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        return GlobalResult.success(adminVoiceCatalogFacade.updateProviderStatus(id, request.status()));
    }

    @Operation(summary = "获取语音模型列表")
    @GetMapping("/models")
    @PreAuthorize("hasAuthority('voice:model:list')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "查询语音模型列表")
    public GlobalResult<PageResult<VoiceModelInfo>> listModels(
            @Parameter(description = "查询条件") @Valid VoiceModelSearch search
    ) {
        return GlobalResult.success(adminVoiceCatalogFacade.findModelPage(
                PageQuery.of(search.getPageNum(), search.getPageSize()),
                search
        ));
    }

    @Operation(summary = "获取语音模型选项")
    @GetMapping("/models/options")
    @PreAuthorize("hasAuthority('voice:model:list')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "查询语音模型选项")
    public GlobalResult<List<VoiceModelInfo>> listModelOptions(
            @RequestParam(value = "enabledOnly", required = false) Boolean enabledOnly
    ) {
        return GlobalResult.success(adminVoiceCatalogFacade.listModelOptions(enabledOnly));
    }

    @Operation(summary = "获取语音模型详情")
    @GetMapping("/models/{id}")
    @PreAuthorize("hasAuthority('voice:model:query')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "查询语音模型详情")
    public GlobalResult<VoiceModelInfo> getModelById(@PathVariable("id") Long id) {
        return GlobalResult.success(adminVoiceCatalogFacade.findModelById(id));
    }

    @Operation(summary = "新增语音模型")
    @PostMapping("/models")
    @PreAuthorize("hasAuthority('voice:model:add')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "新增语音模型")
    @OperationLog(module = "语音配置", operation = "新增语音模型", recordParams = true)
    public GlobalResult<Boolean> createModel(@Valid @RequestBody VoiceModelUpsertRequest request) {
        return GlobalResult.success(adminVoiceCatalogFacade.createModel(request));
    }

    @Operation(summary = "更新语音模型")
    @PutMapping("/models/{id}")
    @PreAuthorize("hasAuthority('voice:model:edit')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "更新语音模型")
    @OperationLog(module = "语音配置", operation = "更新语音模型", recordParams = true)
    public GlobalResult<Boolean> updateModel(
            @PathVariable("id") Long id,
            @Valid @RequestBody VoiceModelUpsertRequest request
    ) {
        return GlobalResult.success(adminVoiceCatalogFacade.updateModel(id, request));
    }

    @Operation(summary = "删除语音模型")
    @DeleteMapping("/models/{id}")
    @PreAuthorize("hasAuthority('voice:model:delete')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "删除语音模型")
    @OperationLog(module = "语音配置", operation = "删除语音模型", recordParams = true)
    public GlobalResult<Boolean> deleteModel(@PathVariable("id") Long id) {
        return GlobalResult.success(adminVoiceCatalogFacade.deleteModel(id));
    }

    @Operation(summary = "启用语音模型")
    @PutMapping("/models/{id}/enable")
    @PreAuthorize("hasAuthority('voice:model:edit')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "启用语音模型")
    @OperationLog(module = "语音配置", operation = "启用语音模型", recordParams = true)
    public GlobalResult<Boolean> enableModel(@PathVariable("id") Long id) {
        return GlobalResult.success(adminVoiceCatalogFacade.updateModelStatus(id, 1));
    }

    @Operation(summary = "禁用语音模型")
    @PutMapping("/models/{id}/disable")
    @PreAuthorize("hasAuthority('voice:model:edit')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "禁用语音模型")
    @OperationLog(module = "语音配置", operation = "禁用语音模型", recordParams = true)
    public GlobalResult<Boolean> disableModel(@PathVariable("id") Long id) {
        return GlobalResult.success(adminVoiceCatalogFacade.updateModelStatus(id, 0));
    }

    @Operation(summary = "更新语音模型状态")
    @PatchMapping("/models/{id}/status")
    @PreAuthorize("hasAuthority('voice:model:edit')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "更新语音模型状态")
    @OperationLog(module = "语音配置", operation = "更新语音模型状态", recordParams = true)
    public GlobalResult<Boolean> updateModelStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        return GlobalResult.success(adminVoiceCatalogFacade.updateModelStatus(id, request.status()));
    }

    @Operation(summary = "获取语音配置列表")
    @GetMapping("/profiles")
    @PreAuthorize("hasAuthority('voice:profile:list')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "查询语音配置列表")
    public GlobalResult<PageResult<VoiceProfileInfo>> listProfiles(
            @Parameter(description = "查询条件") @Valid VoiceProfileSearch search
    ) {
        return GlobalResult.success(adminVoiceCatalogFacade.findProfilePage(
                PageQuery.of(search.getPageNum(), search.getPageSize()),
                search
        ));
    }

    @Operation(summary = "获取语音配置选项")
    @GetMapping("/profiles/options")
    @PreAuthorize("hasAuthority('voice:profile:list')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "查询语音配置选项")
    public GlobalResult<List<VoiceProfileInfo>> listProfileOptions(
            @RequestParam(value = "enabledOnly", required = false) Boolean enabledOnly
    ) {
        return GlobalResult.success(adminVoiceCatalogFacade.listProfileOptions(enabledOnly));
    }

    @Operation(summary = "获取语音配置详情")
    @GetMapping("/profiles/{id}")
    @PreAuthorize("hasAuthority('voice:profile:query')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "查询语音配置详情")
    public GlobalResult<VoiceProfileInfo> getProfileById(@PathVariable("id") Long id) {
        return GlobalResult.success(adminVoiceCatalogFacade.findProfileById(id));
    }

    @Operation(summary = "新增语音配置")
    @PostMapping("/profiles")
    @PreAuthorize("hasAuthority('voice:profile:add')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "新增语音配置")
    @OperationLog(module = "语音配置", operation = "新增语音配置", recordParams = true)
    public GlobalResult<Boolean> createProfile(@Valid @RequestBody VoiceProfileUpsertRequest request) {
        return GlobalResult.success(adminVoiceCatalogFacade.createProfile(request));
    }

    @Operation(summary = "更新语音配置")
    @PutMapping("/profiles/{id}")
    @PreAuthorize("hasAuthority('voice:profile:edit')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "更新语音配置")
    @OperationLog(module = "语音配置", operation = "更新语音配置", recordParams = true)
    public GlobalResult<Boolean> updateProfile(
            @PathVariable("id") Long id,
            @Valid @RequestBody VoiceProfileUpsertRequest request
    ) {
        return GlobalResult.success(adminVoiceCatalogFacade.updateProfile(id, request));
    }

    @Operation(summary = "删除语音配置")
    @DeleteMapping("/profiles/{id}")
    @PreAuthorize("hasAuthority('voice:profile:delete')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "删除语音配置")
    @OperationLog(module = "语音配置", operation = "删除语音配置", recordParams = true)
    public GlobalResult<Boolean> deleteProfile(@PathVariable("id") Long id) {
        return GlobalResult.success(adminVoiceCatalogFacade.deleteProfile(id));
    }

    @Operation(summary = "启用语音配置")
    @PutMapping("/profiles/{id}/enable")
    @PreAuthorize("hasAuthority('voice:profile:edit')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "启用语音配置")
    @OperationLog(module = "语音配置", operation = "启用语音配置", recordParams = true)
    public GlobalResult<Boolean> enableProfile(@PathVariable("id") Long id) {
        return GlobalResult.success(adminVoiceCatalogFacade.updateProfileStatus(id, 1));
    }

    @Operation(summary = "禁用语音配置")
    @PutMapping("/profiles/{id}/disable")
    @PreAuthorize("hasAuthority('voice:profile:edit')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "禁用语音配置")
    @OperationLog(module = "语音配置", operation = "禁用语音配置", recordParams = true)
    public GlobalResult<Boolean> disableProfile(@PathVariable("id") Long id) {
        return GlobalResult.success(adminVoiceCatalogFacade.updateProfileStatus(id, 0));
    }

    @Operation(summary = "更新语音配置状态")
    @PatchMapping("/profiles/{id}/status")
    @PreAuthorize("hasAuthority('voice:profile:edit')")
    @ApiLog(recordParams = true, recordResponseBody = false, value = "更新语音配置状态")
    @OperationLog(module = "语音配置", operation = "更新语音配置状态", recordParams = true)
    public GlobalResult<Boolean> updateProfileStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        return GlobalResult.success(adminVoiceCatalogFacade.updateProfileStatus(id, request.status()));
    }
}