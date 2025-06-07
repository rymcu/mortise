package com.rymcu.mortise.model;

import lombok.Data;

/**
 * Created on 2025/4/12 20:19.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.model
 */
@Data
public class DictInfo {
    /**
     * 名称
     */
    private String label;
    /**
     * 数据
     */
    private String value;
    /**
     * 颜色
     */
    private String color;

    /**
     * 图标
     */
    private String icon;

    /**
     * 图片
     */
    private String image;
}
