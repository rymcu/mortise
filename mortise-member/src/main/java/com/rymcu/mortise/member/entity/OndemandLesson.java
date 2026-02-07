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
 * 录播课程课时实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_ondemand_lesson", schema = "mortise")
public class OndemandLesson implements Serializable {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 模块ID
     */
    @NotNull(message = "所属模块不能为空")
    private Long moduleId;

    /**
     * 课时标题
     */
    @NotBlank(message = "课时标题不能为空")
    private String title;

    /**
     * 课时描述
     */
    private String description;

    /**
     * 内容类型：video-视频, audio-音频, document-文档, quiz-测验, assignment-作业, scorm-SCORM
     */
    @NotBlank(message = "内容类型不能为空")
    private String contentType;

    /**
     * 媒体资产ID（用于非视频类型）
     */
    private Long mediaAssetId;

    /**
     * VOD视频 ID（用于视频类型，推荐使用）
     */
    private Long vodVideoId;

    /**
     * 内容URL（用于非VOD视频的其他类型内容）
     */
    private String contentUrl;

    /**
     * 内容元数据
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> contentMetadata;

    /**
     * 内容时长(秒)
     */
    private Integer durationSeconds;

    /**
     * 字幕/转录文本
     */
    private String transcript;

    /**
     * 排序号
     */
    @NotNull(message = "排序号不能为空")
    private Integer sortNo;

    /**
     * 是否免费
     */
    private Boolean isFree;

    /**
     * 是否允许下载
     */
    private Boolean downloadAllowed;

    /**
     * 是否允许离线观看
     */
    private Boolean offlineAllowed;

    /**
     * 发布策略：immediate-立即, scheduled-定时, conditional-条件
     */
    private String releaseStrategy;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime releaseTime;

    /**
     * 发布条件
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> releaseConditions;

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
     * 观看次数
     */
    private Integer viewCount;

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
