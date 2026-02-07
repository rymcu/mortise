package com.rymcu.mortise.member.model;

import com.rymcu.mortise.common.model.BaseSearch;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品搜索条件
 *
 * @author ronger
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductSearch extends BaseSearch {

    /**
     * 商品类型
     */
    private String productType;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 是否推荐
     */
    private Boolean isFeatured;

    /**
     * 创建人ID
     */
    private Long createdBy;
}
