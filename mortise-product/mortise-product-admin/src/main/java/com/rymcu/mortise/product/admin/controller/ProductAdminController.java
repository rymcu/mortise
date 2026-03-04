package com.rymcu.mortise.product.admin.controller;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.log.annotation.OperationLog;
import com.rymcu.mortise.product.entity.Product;
import com.rymcu.mortise.product.service.ProductService;
import com.rymcu.mortise.web.annotation.AdminController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 产品目录管理（后台）
 *
 * @author ronger
 */
@Tag(name = "产品目录管理", description = "产品数据管理接口")
@AdminController
@RequestMapping("/product/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProductAdminController {

    private final ProductService productService;

    @GetMapping
    @ApiLog("查询商品列表")
    @Operation(summary = "分页查询商品列表")
    public GlobalResult<Page<Product>> listProducts(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        var page = new Page<Product>(pageNum, pageSize);
        return GlobalResult.success(productService.page(page));
    }

    @GetMapping("/{id}")
    @ApiLog("查询商品详情")
    @Operation(summary = "查询商品详情")
    public GlobalResult<Product> getProduct(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        return GlobalResult.success(productService.getById(id));
    }

    @PostMapping
    @ApiLog("创建商品")
    @OperationLog(module = "商品管理", operation = "创建商品", recordParams = true, recordResult = true)
    @Operation(summary = "创建商品")
    public GlobalResult<Boolean> createProduct(@Valid @RequestBody Product product) {
        return GlobalResult.success(productService.save(product));
    }

    @PutMapping("/{id}")
    @ApiLog("更新商品")
    @OperationLog(module = "商品管理", operation = "更新商品", recordParams = true)
    @Operation(summary = "更新商品")
    public GlobalResult<Boolean> updateProduct(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @Valid @RequestBody Product product) {
        product.setId(id);
        return GlobalResult.success(productService.updateById(product));
    }

    @PatchMapping("/{id}/status")
    @ApiLog("更新商品状态")
    @OperationLog(module = "商品管理", operation = "更新商品状态", recordParams = true)
    @Operation(summary = "上架/下架商品")
    public GlobalResult<Boolean> updateStatus(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @Parameter(description = "状态：0-下架, 1-上架") @RequestParam Integer status) {
        return GlobalResult.success(productService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @ApiLog("删除商品")
    @OperationLog(module = "商品管理", operation = "删除商品")
    @Operation(summary = "删除商品")
    public GlobalResult<Boolean> deleteProduct(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        return GlobalResult.success(productService.removeById(id));
    }
}
