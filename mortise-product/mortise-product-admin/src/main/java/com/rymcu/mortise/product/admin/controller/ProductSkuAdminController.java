package com.rymcu.mortise.product.admin.controller;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.product.entity.ProductSku;
import com.rymcu.mortise.product.service.ProductSkuService;
import com.rymcu.mortise.web.annotation.AdminController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产品 SKU 管理（后台）
 * <p>
 * 管理 SKU 的骨架信息（编码、名称、规格属性）。
 * 定价、库存、物流等交易字段由 mortise-commerce 的 SkuPricing 管理，不在此处暴露。
 *
 * @author ronger
 */
@Tag(name = "产品SKU管理", description = "产品规格单元（骨架）管理接口")
@AdminController
@RequestMapping("/products/{productId}/skus")
@RequiredArgsConstructor
public class ProductSkuAdminController {

    private final ProductSkuService productSkuService;

    @GetMapping
    @ApiLog("查询SKU列表")
    @Operation(summary = "查询指定产品的所有SKU（默认SKU排在最前）")
    @PreAuthorize("hasAuthority('product:sku:list')")
    public GlobalResult<List<ProductSku>> listSkus(
            @Parameter(description = "产品ID") @PathVariable Long productId) {
        return GlobalResult.success(productSkuService.findByProductId(productId));
    }

    @GetMapping("/{skuId}")
    @ApiLog("查询SKU详情")
    @Operation(summary = "查询SKU详情")
    @PreAuthorize("hasAuthority('product:sku:query')")
    public GlobalResult<ProductSku> getSku(
            @Parameter(description = "产品ID") @PathVariable Long productId,
            @Parameter(description = "SKU ID") @PathVariable Long skuId) {
        return GlobalResult.success(productSkuService.getById(skuId));
    }

    @PostMapping
    @ApiLog("创建SKU")
    @OperationLog(module = "产品SKU管理", operation = "创建SKU", recordParams = true, recordResult = true)
    @Operation(summary = "为产品创建SKU（仅填写规格骨架，不含定价）")
    @PreAuthorize("hasAuthority('product:sku:add')")
    public GlobalResult<Boolean> createSku(
            @Parameter(description = "产品ID") @PathVariable Long productId,
            @Valid @RequestBody ProductSku sku) {
        sku.setProductId(productId);
        return GlobalResult.success(productSkuService.save(sku));
    }

    @PutMapping("/{skuId}")
    @ApiLog("更新SKU")
    @OperationLog(module = "产品SKU管理", operation = "更新SKU", recordParams = true)
    @Operation(summary = "更新SKU基本信息")
    @PreAuthorize("hasAuthority('product:sku:edit')")
    public GlobalResult<Boolean> updateSku(
            @Parameter(description = "产品ID") @PathVariable Long productId,
            @Parameter(description = "SKU ID") @PathVariable Long skuId,
            @Valid @RequestBody ProductSku sku) {
        sku.setId(skuId);
        sku.setProductId(productId);
        return GlobalResult.success(productSkuService.updateById(sku));
    }

    @PatchMapping("/{skuId}/status")
    @ApiLog("更新SKU状态")
    @OperationLog(module = "产品SKU管理", operation = "更新SKU状态", recordParams = true)
    @Operation(summary = "更新SKU状态（active-上架 / inactive-下架 / discontinued-停售）")
    @PreAuthorize("hasAuthority('product:sku:edit')")
    public GlobalResult<Boolean> updateSkuStatus(
            @Parameter(description = "产品ID") @PathVariable Long productId,
            @Parameter(description = "SKU ID") @PathVariable Long skuId,
            @Parameter(description = "状态：active / inactive / discontinued") @RequestParam String status) {
        return GlobalResult.success(productSkuService.updateStatus(skuId, status));
    }

    @PatchMapping("/{skuId}/default")
    @ApiLog("设置默认SKU")
    @OperationLog(module = "产品SKU管理", operation = "设置默认SKU", recordParams = true)
    @Operation(summary = "设置默认SKU（自动取消同产品下其他SKU的默认标记）")
    @PreAuthorize("hasAuthority('product:sku:edit')")
    public GlobalResult<Boolean> setDefaultSku(
            @Parameter(description = "产品ID") @PathVariable Long productId,
            @Parameter(description = "SKU ID") @PathVariable Long skuId) {
        return GlobalResult.success(productSkuService.setDefault(productId, skuId));
    }

    @DeleteMapping("/{skuId}")
    @ApiLog("删除SKU")
    @OperationLog(module = "产品SKU管理", operation = "删除SKU")
    @Operation(summary = "逻辑删除SKU")
    @PreAuthorize("hasAuthority('product:sku:delete')")
    public GlobalResult<Boolean> deleteSku(
        @Parameter(description = "产品ID") @PathVariable Long productId,
        @Parameter(description = "SKU ID") @PathVariable Long skuId) {
        return GlobalResult.success(productSkuService.removeById(skuId));
    }
}
