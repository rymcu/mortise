package com.rymcu.mortise.product.api.controller;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.product.entity.ProductCategory;
import com.rymcu.mortise.product.service.ProductCategoryService;
import com.rymcu.mortise.web.annotation.ApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 产品分类接口（客户端）
 *
 * @author ronger
 */
@Tag(name = "产品分类接口", description = "客户端产品分类查询接口")
@ApiController
@RequestMapping("/app/product-categories")
@RequiredArgsConstructor
public class ProductCategoryApiController {

    private final ProductCategoryService productCategoryService;

    @GetMapping("/tree")
    @ApiLog("查询分类树")
    @Operation(summary = "获取激活的产品分类树（用于前端导航/筛选）")
    public GlobalResult<List<ProductCategory>> getTree() {
        return GlobalResult.success(productCategoryService.getTree());
    }
}
