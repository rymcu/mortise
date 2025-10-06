package com.rymcu.mortise.wechat.model;

import com.rymcu.mortise.common.model.BaseSearch;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created on 2025/10/6 15:57.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.wechat.model
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WeChatAccountSearch extends BaseSearch {

    /**
     * 账号类型：mp-公众号, open-开放平台, miniapp-小程序
     */
    private String accountType;

    /**
     * 账号名称（模糊查询）
     */
    private String accountName;

    /**
     * 是否启用：0-禁用, 1-启用
     */
    private Integer isEnabled;
}
