package com.rymcu.mortise.product.admin.controller;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.product.admin.facade.ProductSkuTargetAdminFacade;
import com.rymcu.mortise.product.entity.ProductSkuTarget;
import com.rymcu.mortise.web.annotation.AdminController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "SKU目标映射管理", description = "产品 SKU 与业务目标映射的后台管理接口")
@AdminController
@RequestMapping("/products/{productId}/skus/{skuId}/targets")
@RequiredArgsConstructor
public class ProductSkuTargetAdminController {

    private final ProductSkuTargetAdminFacade productSkuTargetAdminFacade;

    @GetMapping
    @ApiLog("查询SKU目标映射列表")
    @Operation(summary = "查询指定 SKU 的目标映射列表")
    @PreAuthorize("hasAuthority('product:sku-target:list')")
    public GlobalResult<List<ProductSkuTarget>> listTargets(
            @Parameter(description = "产品ID") @PathVariable Long productId,
            @Parameter(description = "SKU ID") @PathVariable Long skuId) {
        return GlobalResult.success(productSkuTargetAdminFacade.listTargets(productId, skuId));
    }

    @GetMapping("/{targetId}")
    @ApiLog("查询SKU目标映射详情")
    @Operation(summary = "查询 SKU 目标映射详情")
    @PreAuthorize("hasAuthority('product:sku-target:query')")
    public GlobalResult<ProductSkuTarget> getTarget(
            @Parameter(description = "产品ID") @PathVariable Long productId,
            @Parameter(description = "SKU ID") @PathVariable Long skuId,
            @Parameter(description = "目标映射ID") @PathVariable Long targetId) {
        return GlobalResult.success(productSkuTargetAdminFacade.getTarget(productId, skuId, targetId));
    }

    @PostMapping
    @ApiLog("创建SKU目标映射")
    @OperationLog(module = "SKU目标映射管理", operation = "创建SKU目标映射", recordParams = true, recordResult = true)
    @Operation(summary = "为指定 SKU 创建目标映射")
    @PreAuthorize("hasAuthority('product:sku-target:add')")
    public GlobalResult<ProductSkuTarget> createTarget(
            @Parameter(description = "产品ID") @PathVariable Long productId,
            @Parameter(description = "SKU ID") @PathVariable Long skuId,
            @Valid @RequestBody ProductSkuTarget target) {
        return GlobalResult.success(productSkuTargetAdminFacade.createTarget(productId, skuId, target));
    }

    @PutMapping("/{targetId}")
    @ApiLog("更新SKU目标映射")
    @OperationLog(module = "SKU目标映射管理", operation = "更新SKU目标映射", recordParams = true)
    @Operation(summary = "更新 SKU 目标映射")
    @PreAuthorize("hasAuthority('product:sku-target:edit')")
    public GlobalResult<Boolean> updateTarget(
            @Parameter(description = "产品ID") @PathVariable Long productId,
            @Parameter(description = "SKU ID") @PathVariable Long skuId,
            @Parameter(description = "目标映射ID") @PathVariable Long targetId,
            @Valid @RequestBody ProductSkuTarget target) {
        return GlobalResult.success(productSkuTargetAdminFacade.updateTarget(productId, skuId, targetId, target));
    }

    @DeleteMapping("/{targetId}")
    @ApiLog("删除SKU目标映射")
    @OperationLog(module = "SKU目标映射管理", operation = "删除SKU目标映射")
    @Operation(summary = "删除 SKU 目标映射")
    @PreAuthorize("hasAuthority('product:sku-target:delete')")
    public GlobalResult<Boolean> deleteTarget(
            @Parameter(description = "产品ID") @PathVariable Long productId,
            @Parameter(description = "SKU ID") @PathVariable Long skuId,
            @Parameter(description = "目标映射ID") @PathVariable Long targetId) {
        return GlobalResult.success(productSkuTargetAdminFacade.deleteTarget(productId, skuId, targetId));
    }
}
