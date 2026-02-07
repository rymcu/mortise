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
 * 学员报名实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_enrollment", schema = "mortise")
public class Enrollment implements Serializable {

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
     * 课程ID
     */
    @NotNull(message = "课程ID 不能为空")
    private Long courseId;

    /**
     * 排期ID
     */
    private Long scheduleId;

    /**
     * 关联订单项ID
     */
    private Long orderItemId;

    /**
     * 报名类型：paid-付费, free-免费, trial-试学, invited-邀请, transferred-转让
     */
    @NotBlank(message = "报名类型不能为空")
    private String enrollmentType;

    /**
     * 报名来源
     */
    private String source;

    /**
     * 状态：0-待激活, 1-进行中, 2-暂停, 3-已完成, 4-已过期, 5-已取消, 6-已退款
     */
    private Integer status;

    /**
     * 报名时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime enrolledTime;

    /**
     * 学习有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresTime;

    /**
     * 开始学习时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedTime;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedTime;

    /**
     * 学习进度百分比
     */
    private BigDecimal progressPercent;

    /**
     * 累计学习时长(分钟)
     */
    private Integer totalStudyMinutes;

    /**
     * 最后访问时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAccessTime;

    /**
     * 是否已颁发证书
     */
    private Boolean certificateIssued;

    /**
     * 最终成绩
     */
    private BigDecimal finalScore;

    /**
     * 备注
     */
    private String notes;

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
