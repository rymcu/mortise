package com.rymcu.mortise.member.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.member.entity.ProductCategory;
import com.rymcu.mortise.member.mapper.ProductCategoryMapper;
import com.rymcu.mortise.member.model.ProductCategorySearch;
import com.rymcu.mortise.member.service.ProductCategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.rymcu.mortise.member.entity.table.ProductCategoryTableDef.PRODUCT_CATEGORY;

/**
 * Created on 2025/11/20 10:58.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.member.service.impl
 */
@Service
@Primary
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {

    @Override
    public Page<ProductCategory> findCategoryList(Page<ProductCategory> page, ProductCategorySearch search) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .where(PRODUCT_CATEGORY.NAME.like(search.getQuery(), StringUtils.isNotBlank(search.getQuery())))
                .and(PRODUCT_CATEGORY.PARENT_ID.eq(search.getParentId(), Objects.nonNull(search.getParentId())))
                .and(PRODUCT_CATEGORY.IS_ACTIVE.eq(search.getIsActive(), Objects.nonNull(search.getIsActive())))
                .and(PRODUCT_CATEGORY.STATUS.eq(search.getStatus(), Objects.nonNull(search.getStatus())))
                .orderBy(PRODUCT_CATEGORY.SORT_NO.asc(), PRODUCT_CATEGORY.CREATED_TIME.desc());
        return mapper.paginate(page, queryWrapper);
    }

    @Override
    public List<ProductCategory> findChildren(Long parentId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(PRODUCT_CATEGORY.PARENT_ID.eq(parentId))
                .orderBy(PRODUCT_CATEGORY.SORT_NO.asc());
        return list(queryWrapper);
    }

    @Override
    public List<ProductCategory> findTree() {
        // 获取所有顶级分类（parent_id 为 null）
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(PRODUCT_CATEGORY.PARENT_ID.isNull())
                .orderBy(PRODUCT_CATEGORY.SORT_NO.asc());
        return list(queryWrapper);
    }

}
