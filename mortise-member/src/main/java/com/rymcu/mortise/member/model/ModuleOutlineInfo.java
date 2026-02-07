package com.rymcu.mortise.member.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created on 2025/11/4 9:56.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.edu.model
 */
@Data
public class ModuleOutlineInfo implements Serializable {

    /**
     * 模块ID
     */
    private Long moduleId;

    /**
     * 模块标题
     */
    private String title;

    /**
     * 模块描述
     */
    private String description;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 预估时长（分钟）
     */
    private Integer durationMinutes;

    /**
     * 是否免费试看
     */
    private Boolean isFree;

    /**
     * 课时数量
     */
    private Integer lessonCount;

    /**
     * 课时列表
     */
    private List<LessonOutlineInfo> lessons;
}
