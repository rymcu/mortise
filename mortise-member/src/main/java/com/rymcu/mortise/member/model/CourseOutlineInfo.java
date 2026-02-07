package com.rymcu.mortise.member.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created on 2025/11/4 9:54.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.edu.model
 */
@Data
public class CourseOutlineInfo implements Serializable {

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程标题
     */
    private String courseTitle;

    /**
     * 课程类型
     */
    private String courseType;

    /**
     * 课程描述
     */
    private String description;

    /**
     * 难度级别
     */
    private String difficultyLevel;

    /**
     * 预估总时长（分钟）
     */
    private Integer totalDurationMinutes;

    /**
     * 总模块数
     */
    private Integer totalModules;

    /**
     * 总课时数
     */
    private Integer totalLessons;

    /**
     * 关联来源类型：course-直接关联课程, course_schedule-通过排期关联
     */
    private String sourceType;

    /**
     * 关联来源ID（如果是通过排期关联，则为排期ID）
     */
    private Long sourceId;

    /**
     * 排期信息（如果是通过排期关联）
     */
    private ScheduleInfo scheduleInfo;

    /**
     * 模块列表
     */
    private List<ModuleOutlineInfo> modules;
}
