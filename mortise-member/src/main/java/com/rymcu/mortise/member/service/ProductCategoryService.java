package com.rymcu.mortise.member.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.member.entity.ProductCategory;
import com.rymcu.mortise.member.model.ProductCategorySearch;

import java.util.List;

/**
 * Created on 2025/11/20 10:57.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.member.service
 */
public interface ProductCategoryService extends IService<ProductCategory> {

    /**
     * 分页查询商品分类
     *
     * @param page   分页对象
     * @param search 查询条件
     * @return 分页数据
     */
    Page<ProductCategory> findCategoryList(Page<ProductCategory> page, ProductCategorySearch search);

    /**
     * 获取子分类列表
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<ProductCategory> findChildren(Long parentId);

    /**
     * 获取分类树
     *
     * @return 分类树
     */
    List<ProductCategory> findTree();

}
