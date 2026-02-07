package com.rymcu.mortise.member.model;

import com.rymcu.mortise.common.model.BaseSearch;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分类搜索条件
 *
 * @author ronger
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductCategorySearch extends BaseSearch {

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 是否激活
     */
    private Boolean isActive;

    /**
     * 状态
     */
    private Integer status;
}
