package com.rymcu.mortise.member.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.rymcu.mortise.persistence.mybatis.handler.DurationTypeHandler;
import com.rymcu.mortise.persistence.mybatis.handler.JsonbTypeHandler;
import com.rymcu.mortise.persistence.mybatis.handler.StringListTypeHandler;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 课程实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_course", schema = "mortise")
public class Course implements Serializable {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 课程标题
     */
    @NotBlank(message = "课程标题不能为空")
    private String title;

    /**
     * 课程副标题
     */
    private String subtitle;

    /**
     * 课程描述
     */
    private String description;

    /**
     * 封面图片URL
     */
    private String coverImageUrl;

    /**
     * 课程类型：ondemand-录播, live-直播, hybrid-混合, cohort-小组
     */
    @NotBlank(message = "课程类型不能为空")
    private String type;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 难度级别：beginner-初级, intermediate-中级, advanced-高级
     */
    private String difficultyLevel;

    /**
     * 语言
     */
    private String language;

    /**
     * 预估总时长
     */
    @Column(typeHandler = DurationTypeHandler.class)
    private Duration durationMinutes;

    /**
     * 最大学员数限制
     */
    private Integer maxStudents;
    /**
     * 学习目标数组
     */
    @Column(typeHandler = StringListTypeHandler.class)
    private List<String> learningObjectives;

    /**
     * 目标学员
     */
    private String targetAudience;

    /**
     * 主讲教师ID
     */

    @NotNull(message = "主讲教师不能为空")
    private Long instructorId;

    /**
     * 状态：0-正常, 1-禁用
     */
    private Integer status;

    /**
     * 是否推荐课程
     */
    private Boolean isFeatured;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 课程元数据
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> metadata;

    /**
     * SEO关键词
     */
    private String seoKeywords;

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
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedTime;

    /**
     * 删除标记：0-未删除, 1-已删除
     */
    @Column(isLogicDelete = true)
    private Integer delFlag;
}
