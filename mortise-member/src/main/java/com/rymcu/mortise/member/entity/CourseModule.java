package com.rymcu.mortise.member.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.rymcu.mortise.persistence.mybatis.handler.DurationTypeHandler;
import com.rymcu.mortise.persistence.mybatis.handler.JsonbTypeHandler;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 课程模块实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_course_module", schema = "mortise")
public class CourseModule implements Serializable {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 课程ID
     */
    @NotNull(message = "所属课程不能为空")
    private Long courseId;

    /**
     * 模块标题
     */
    @NotBlank(message = "模块标题不能为空")
    private String title;

    /**
     * 模块描述
     */
    private String description;

    /**
     * 排序号
     */
    @NotNull(message = "排序号不能为空")
    private Integer sortNo;

    /**
     * 预估章节时长
     */
    @Column(typeHandler = DurationTypeHandler.class)
    private Duration durationMinutes;

    /**
     * 是否免费试看
     */
    private Boolean isFree;

    /**
     * 解锁条件
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> unlockCondition;

    /**
     * 状态：0-正常, 1-禁用
     */
    private Integer status;

    /**
     * 删除标记：0-未删除, 1-已删除
     */
    @Column(isLogicDelete = true)
    private Integer delFlag;

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
}
