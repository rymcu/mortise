package com.rymcu.mortise.product.service;

import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.product.entity.ProductCategory;

import java.util.List;

/**
 * 产品分类服务
 *
 * @author ronger
 */
public interface ProductCategoryService extends IService<ProductCategory> {

    /**
     * 查询完整分类树（仅激活的分类）
     *
     * @return 根节点列表（子节点已递归填充）
     */
    List<ProductCategory> getTree();

    /**
     * 查询完整分类树（含禁用的分类，供后台管理使用）
     *
     * @return 根节点列表
     */
    List<ProductCategory> getFullTree();

    /**
     * 更新分类状态
     *
     * @param id     分类ID
     * @param status 状态：0-正常, 1-禁用
     * @return 是否成功
     */
    Boolean updateStatus(Long id, Integer status);
}
