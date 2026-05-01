package com.rymcu.mortise.product.api.controller;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.log.annotation.ApiLog;
import com.rymcu.mortise.product.api.dto.ApiProductModels.ProductDetailVO;
import com.rymcu.mortise.product.api.facade.ProductCatalogApiFacade;
import com.rymcu.mortise.product.entity.Product;
import com.rymcu.mortise.web.annotation.ApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 产品目录接口（客户端）
 *
 * @author ronger
 */
@Tag(name = "产品目录接口", description = "客户端产品查询接口")
@ApiController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductApiController {

    private final ProductCatalogApiFacade productCatalogApiFacade;

    @GetMapping
    @ApiLog("查询产品列表")
    @Operation(summary = "根据产品类型查询产品列表")
    public GlobalResult<List<Product>> listProducts(
            @Parameter(description = "产品类型") @RequestParam String productType) {
        return GlobalResult.success(productCatalogApiFacade.listProducts(productType));
    }

    @GetMapping("/{id}")
    @ApiLog("查询产品详情")
    @Operation(summary = "获取上架产品详情")
    public GlobalResult<ProductDetailVO> getProductDetail(
            @Parameter(description = "产品ID") @PathVariable("id") Long id) {
        return GlobalResult.success(productCatalogApiFacade.getProductDetail(id));
    }

    @GetMapping("/types")
    @ApiLog("查询产品类型列表")
    @Operation(summary = "获取所有可用产品类型（内置 + SPI 扩展）")
    public GlobalResult<Map<String, String>> listProductTypes() {
        return GlobalResult.success(productCatalogApiFacade.listProductTypes());
    }
}
