package com.rymcu.mortise.member.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created on 2025/11/4 9:56.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.edu.model
 */
@Data
public class LessonOutlineInfo implements Serializable {

    /**
     * 课时ID
     */
    private Long lessonId;

    /**
     * 课时标题
     */
    private String title;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 时长（秒）
     */
    private Integer durationSeconds;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 是否免费
     */
    private Boolean isFree;

    /**
     * 发布策略
     */
    private String releaseStrategy;
}
