package com.rymcu.mortise.member.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.rymcu.mortise.persistence.mybatis.handler.JsonbTypeHandler;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 课程排期实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_course_schedule", schema = "mortise")
public class CourseSchedule implements Serializable {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 课程ID
     */
    @NotNull(message = "课程ID 不能为空")
    private Long courseId;

    /**
     * 排期名称，如"2024年春季班"
     */
    @NotBlank(message = "排期名称不能为空")
    private String name;

    /**
     * 排期描述
     */
    private String description;

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 报名开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime enrollmentStartTime;

    /**
     * 报名结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime enrollmentEndTime;

    /**
     * 时区
     */
    @NotBlank(message = "时区不能为空")
    private String timezone;

    /**
     * 本排期最大学员数
     */
    private Integer maxStudents;

    /**
     * 最小开班人数
     */
    private Integer minStudents;

    /**
     * 当前报名人数
     */
    private Integer currentStudents;

    /**
     * 讲师ID，可覆盖课程默认讲师
     */
    private Long instructorId;

    /**
     * 是否默认排期
     */
    private Boolean isDefault;

    /**
     * 是否自动开班
     */
    private Boolean autoStart;

    /**
     * 状态：0-草稿, 1-已发布, 2-招生中, 3-已满员, 4-已开班, 5-已完成, 6-已取消
     */
    private Integer status;

    /**
     * 价格覆盖
     */
    private BigDecimal priceOverride;

    /**
     * 排期特殊设置
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> settings;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    /**
     * 删除标记：0-未删除, 1-已删除
     */
    @Column(isLogicDelete = true)
    private Integer delFlag;
}
