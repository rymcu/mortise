package com.rymcu.mortise.product.service;

import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.product.entity.Product;

import java.util.List;
import java.util.Map;

/**
 * 产品目录服务
 *
 * @author ronger
 */
public interface ProductService extends IService<Product> {

    /**
     * 根据产品类型查询
     *
     * @param productType 产品类型
     * @return 产品列表
     */
    List<Product> findByProductType(String productType);

    /**
     * 更新产品状态（触发 SPI 生命周期回调）
     *
     * @param id     产品ID
     * @param status 状态
     * @return 是否成功
     */
    Boolean updateStatus(Long id, Integer status);

    /**
     * 获取所有可用的产品类型（内置 + SPI 注册）
     *
     * @return 类型 code → 描述 的映射
     */
    Map<String, String> getAllProductTypes();
}
