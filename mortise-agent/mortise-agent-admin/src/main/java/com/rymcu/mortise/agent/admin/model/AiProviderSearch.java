package com.rymcu.mortise.agent.admin.model;

import com.rymcu.mortise.common.model.BaseSearch;
import lombok.Getter;
import lombok.Setter;

/**
 * AI 提供商查询条件
 *
 * @author ronger
 */
@Getter
@Setter
public class AiProviderSearch extends BaseSearch {

    /** 提供商名称 */
    private String name;

    /** 提供商编码 */
    private String code;
}
