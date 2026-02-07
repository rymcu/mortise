package com.rymcu.mortise.member.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created on 2025/11/4 9:55.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.edu.model
 */
@Data
public class ScheduleInfo implements Serializable {

    /**
     * 排期ID
     */
    private Long scheduleId;

    /**
     * 排期名称
     */
    private String name;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 当前报名人数
     */
    private Integer currentStudents;

    /**
     * 最大学员数
     */
    private Integer maxStudents;

    /**
     * 状态
     */
    private Integer status;
}
