package com.rymcu.mortise.product.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.product.dto.ProductQueryParam;
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
     * 分页查询产品（支持按类型、分类、状态、关键字过滤）
     *
     * @param page  分页参数
     * @param param 筛选条件
     * @return 分页结果
     */
    Page<Product> pageByParam(Page<Product> page, ProductQueryParam param);

    /**
     * 批量更新产品状态
     *
     * @param ids    产品ID列表
     * @param status 目标状态
     * @return 成功更新数量
     */
    int batchUpdateStatus(List<Long> ids, Integer status);

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
