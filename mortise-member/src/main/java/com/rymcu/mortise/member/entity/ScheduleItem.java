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
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 排期项目实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_schedule_item", schema = "mortise")
public class ScheduleItem implements Serializable {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 排期ID
     */
    @NotNull(message = "排期ID 不能为空")
    private Long scheduleId;

    /**
     * 项目标题
     */
    @NotBlank(message = "项目标题不能为空")
    private String title;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 目标类型：live_occurrence-直播实例, lesson_unlock-课时解锁, assignment-作业, quiz-测验, milestone-里程碑, custom-自定义
     */
    @NotBlank(message = "目标类型不能为空")
    private String targetType;

    /**
     * 关联目标对象ID
     */
    @NotNull(message = "关联目标对象不能为空")
    private Long targetId;

    /**
     * 计划执行时间
     */
    @NotNull(message = "计划执行时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime plannedTime;

    /**
     * 实际执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualTime;

    /**
     * 预估时长(分钟)
     */
    private Integer durationMinutes;

    /**
     * 是否必须参与
     */
    private Boolean isMandatory;

    /**
     * 前置节点依赖
     */
    private Long[] prerequisiteItems;

    /**
     * 提醒时间偏移(分钟)，默认提前1天和1小时
     */
    private Integer[] notifyOffsets;

    /**
     * 通知模板
     */
    private String notificationTemplate;

    /**
     * 状态：0-待处理, 1-进行中, 2-已完成, 3-已跳过, 4-已取消
     */
    private Integer status;

    /**
     * 完成标准
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> completionCriteria;

    /**
     * 扩展数据
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> metadata;

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
