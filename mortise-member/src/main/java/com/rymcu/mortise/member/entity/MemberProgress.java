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
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 会员学习进度实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_member_progress", schema = "mortise")
public class MemberProgress implements Serializable {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 会员ID
     */
    @NotNull(message = "会员ID 不能为空")
    private Long memberId;

    /**
     * 报名ID
     */
    @NotNull(message = "报名ID 不能为空")
    private Long enrollmentId;

    /**
     * 目标类型：lesson-课时, module-模块, quiz-测验, assignment-作业, live_session-直播
     */
    @NotBlank(message = "目标类型不能为空")
    private String targetType;

    /**
     * 关联具体内容ID
     */
    @NotNull(message = "目标ID 不能为空")
    private Long targetId;

    /**
     * 完成状态：not_started-未开始, in_progress-进行中, completed-已完成, skipped-已跳过, failed-失败
     */
    @NotBlank(message = "完成状态不能为空")
    private String completionStatus;

    /**
     * 进度百分比
     */
    private BigDecimal progressPercent;

    /**
     * 花费时长(秒)
     */
    private Integer timeSpentSeconds;

    /**
     * 尝试次数
     */
    private Integer attemptsCount;

    /**
     * 最佳成绩
     */
    private BigDecimal bestScore;

    /**
     * 最新成绩
     */
    private BigDecimal latestScore;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedTime;

    /**
     * 最后访问时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAccessedTime;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedTime;

    /**
     * 互动数据(如视频播放位置)
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> interactionData;

    /**
     * 学习笔记
     */
    private String notes;

    /**
     * 状态：0-正常, 1-禁用
     */
    private Integer status;

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
