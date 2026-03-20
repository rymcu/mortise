package com.rymcu.mortise.agent.admin.model;

import com.rymcu.mortise.common.model.BaseSearch;
import lombok.Getter;
import lombok.Setter;

/**
 * AI 模型查询条件
 *
 * @author ronger
 */
@Getter
@Setter
public class AiModelSearch extends BaseSearch {

    /** 所属提供商 ID */
    private Long providerId;

    /** 模型名称 */
    private String modelName;
}
