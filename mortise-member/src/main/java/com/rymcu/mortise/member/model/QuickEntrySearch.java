package com.rymcu.mortise.member.model;

import com.rymcu.mortise.common.model.BaseSearch;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 金刚区快捷入口搜索条件
 *
 * @author ronger
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuickEntrySearch extends BaseSearch {

    /**
     * 展示位置：home-首页, category-分类页
     */
    private String position;

    /**
     * 平台：all-全平台, h5-H5, miniapp-小程序, app-APP
     */
    private String platform;

    /**
     * 状态：0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 行索引
     */
    private Integer rowIndex;

    /**
     * 是否只查询有效的（在有效期内且启用）
     */
    private Boolean activeOnly;
}
