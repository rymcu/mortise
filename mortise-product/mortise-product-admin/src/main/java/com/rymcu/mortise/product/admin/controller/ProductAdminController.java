package com.rymcu.mortise.product.admin.controller;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.product.admin.facade.ProductAdminFacade;
import com.rymcu.mortise.product.entity.Product;
import com.rymcu.mortise.web.annotation.AdminController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 产品目录管理（后台）
 * <p>
 * 聚焦于产品描述型元数据（标题、分类、规格、SEO 等）的管理。
 * 定价、库存、物流等交易属性由 mortise-commerce 模块负责，不在此处管理。
 *
 * @author ronger
 */
@Tag(name = "产品目录管理", description = "产品数据管理接口")
@AdminController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductAdminController {

    private final ProductAdminFacade productAdminFacade;

    @GetMapping
    @ApiLog("查询产品列表")
    @Operation(summary = "分页查询产品列表（支持按类型、分类、状态、关键字过滤）")
    @PreAuthorize("hasAuthority('product:catalog:list')")
    public GlobalResult<Page<Product>> listProducts(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "标题关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "产品类型") @RequestParam(required = false) String productType,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "状态：0-草稿, 1-上架, 2-下架, 3-停产") @RequestParam(required = false) Integer status,
            @Parameter(description = "是否推荐") @RequestParam(required = false) Boolean isFeatured) {
        return GlobalResult.success(
                productAdminFacade.listProducts(pageNum, pageSize, keyword, productType, categoryId, status, isFeatured)
        );
    }

    @GetMapping("/{id}")
    @ApiLog("查询产品详情")
    @Operation(summary = "查询产品详情")
    @PreAuthorize("hasAuthority('product:catalog:query')")
    public GlobalResult<Product> getProduct(
            @Parameter(description = "产品ID") @PathVariable Long id) {
        return GlobalResult.success(productAdminFacade.getProduct(id));
    }

    @PostMapping
    @ApiLog("创建产品")
    @OperationLog(module = "产品管理", operation = "创建产品", recordParams = true, recordResult = true)
    @Operation(summary = "创建产品（仅填写描述型元数据，不含定价/库存）")
    @PreAuthorize("hasAuthority('product:catalog:add')")
    public GlobalResult<Boolean> createProduct(@Valid @RequestBody Product product) {
        return GlobalResult.success(productAdminFacade.createProduct(product));
    }

    @PutMapping("/{id}")
    @ApiLog("更新产品")
    @OperationLog(module = "产品管理", operation = "更新产品", recordParams = true)
    @Operation(summary = "更新产品")
    @PreAuthorize("hasAuthority('product:catalog:edit')")
    public GlobalResult<Boolean> updateProduct(
            @Parameter(description = "产品ID") @PathVariable Long id,
            @Valid @RequestBody Product product) {
        return GlobalResult.success(productAdminFacade.updateProduct(id, product));
    }

    @PatchMapping("/{id}/status")
    @ApiLog("更新产品状态")
    @OperationLog(module = "产品管理", operation = "更新产品状态", recordParams = true)
    @Operation(summary = "上架/下架/停产产品（状态机：0-草稿→1-上架→2-下架→3-停产）")
    @PreAuthorize("hasAuthority('product:catalog:edit')")
    public GlobalResult<Boolean> updateStatus(
            @Parameter(description = "产品ID") @PathVariable Long id,
            @Parameter(description = "状态：0-草稿, 1-上架, 2-下架, 3-停产") @RequestParam Integer status) {
        return GlobalResult.success(productAdminFacade.updateStatus(id, status));
    }

    @PatchMapping("/batch/status")
    @ApiLog("批量更新产品状态")
    @OperationLog(module = "产品管理", operation = "批量更新产品状态", recordParams = true)
    @Operation(summary = "批量更新产品状态（如批量上架/下架）")
    @PreAuthorize("hasAuthority('product:catalog:edit')")
    public GlobalResult<Integer> batchUpdateStatus(
            @Parameter(description = "产品ID列表") @RequestBody List<Long> ids,
            @Parameter(description = "目标状态") @RequestParam Integer status) {
        return GlobalResult.success(productAdminFacade.batchUpdateStatus(ids, status));
    }

    @DeleteMapping("/{id}")
    @ApiLog("删除产品")
    @OperationLog(module = "产品管理", operation = "删除产品")
    @Operation(summary = "逻辑删除产品")
    @PreAuthorize("hasAuthority('product:catalog:delete')")
    public GlobalResult<Boolean> deleteProduct(
            @Parameter(description = "产品ID") @PathVariable Long id) {
        return GlobalResult.success(productAdminFacade.deleteProduct(id));
    }

    @GetMapping("/types")
    @ApiLog("查询产品类型列表")
    @Operation(summary = "获取所有可用产品类型（内置 + SPI 扩展）")
    @PreAuthorize("hasAuthority('product:catalog:query')")
    public GlobalResult<Map<String, String>> listProductTypes() {
        return GlobalResult.success(productAdminFacade.listProductTypes());
    }
}
