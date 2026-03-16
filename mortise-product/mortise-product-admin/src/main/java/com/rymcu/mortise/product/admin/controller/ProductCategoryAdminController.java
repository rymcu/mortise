package com.rymcu.mortise.product.admin.controller;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.product.entity.ProductCategory;
import com.rymcu.mortise.product.service.ProductCategoryService;
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
 * 产品分类管理（后台）
 * <p>
 * 提供分类树查询、新增、编辑、状态管理、删除接口。
 * 树形结构由 Service 层通过邻接表动态组装，前端直接消费树形 JSON。
 *
 * @author ronger
 */
@Tag(name = "产品分类管理", description = "产品分类树形结构管理接口")
@AdminController
@RequestMapping("/product-categories")
@RequiredArgsConstructor
public class ProductCategoryAdminController {

    private final ProductCategoryService productCategoryService;

    @GetMapping("/tree")
    @ApiLog("查询分类树")
    @Operation(summary = "查询完整分类树（含禁用，供后台管理使用）")
    @PreAuthorize("hasAuthority('product:category:list')")
    public GlobalResult<List<ProductCategory>> getFullTree() {
        return GlobalResult.success(productCategoryService.getFullTree());
    }

    @GetMapping("/{id}")
    @ApiLog("查询分类详情")
    @Operation(summary = "查询分类详情")
    @PreAuthorize("hasAuthority('product:category:query')")
    public GlobalResult<ProductCategory> getCategory(
            @Parameter(description = "分类ID") @PathVariable Long id) {
        return GlobalResult.success(productCategoryService.getById(id));
    }

    @PostMapping
    @ApiLog("创建分类")
    @OperationLog(module = "产品分类管理", operation = "创建分类", recordParams = true, recordResult = true)
    @Operation(summary = "创建产品分类")
    @PreAuthorize("hasAuthority('product:category:add')")
    public GlobalResult<Boolean> createCategory(@Valid @RequestBody ProductCategory category) {
        return GlobalResult.success(productCategoryService.save(category));
    }

    @PutMapping("/{id}")
    @ApiLog("更新分类")
    @OperationLog(module = "产品分类管理", operation = "更新分类", recordParams = true)
    @Operation(summary = "更新产品分类")
    @PreAuthorize("hasAuthority('product:category:edit')")
    public GlobalResult<Boolean> updateCategory(
            @Parameter(description = "分类ID") @PathVariable Long id,
            @Valid @RequestBody ProductCategory category) {
        category.setId(id);
        return GlobalResult.success(productCategoryService.updateById(category));
    }

    @PatchMapping("/{id}/status")
    @ApiLog("更新分类状态")
    @OperationLog(module = "产品分类管理", operation = "更新分类状态", recordParams = true)
    @Operation(summary = "启用/禁用分类（0-正常, 1-禁用）")
    @PreAuthorize("hasAuthority('product:category:edit')")
    public GlobalResult<Boolean> updateStatus(
            @Parameter(description = "分类ID") @PathVariable Long id,
            @Parameter(description = "状态：0-正常, 1-禁用") @RequestParam Integer status) {
        return GlobalResult.success(productCategoryService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @ApiLog("删除分类")
    @OperationLog(module = "产品分类管理", operation = "删除分类")
    @Operation(summary = "删除产品分类（注意：删除前请确认该分类下无产品及子分类）")
    @PreAuthorize("hasAuthority('product:category:delete')")
    public GlobalResult<Boolean> deleteCategory(
        @Parameter(description = "分类ID") @PathVariable Long id) {
        return GlobalResult.success(productCategoryService.removeById(id));
    }
}
