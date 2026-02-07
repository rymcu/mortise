package com.rymcu.mortise.member.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.rymcu.mortise.persistence.mybatis.handler.JsonbTypeHandler;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 阿里云 VOD 视频资产实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_vod_video", schema = "mortise")
public class VodVideo implements Serializable {

    /**
     * 主键 ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 视频标题
     */
    private String title;

    /**
     * 视频描述
     */
    private String description;

    /**
     * 视频标签，逗号分隔
     */
    private String tags;

    /**
     * 阿里云 VOD 返回的 VideoId
     */
    @NotBlank(message = "阿里云 VOD VideoId 不能为空")
    private String aliyunVideoId;

    /**
     * 阿里云区域
     */
    private String aliyunRegionId;

    /**
     * 视频时长(秒)
     */
    private Integer durationSeconds;

    /**
     * 源文件大小(字节)
     */
    private Long fileSize;

    /**
     * 视频格式
     */
    private String format;

    /**
     * 视频封面 URL
     */
    private String coverUrl;

    /**
     * 是否 HLS 标准加密
     */
    private Boolean isEncrypted;

    /**
     * 转码模板组 ID
     */
    private String transcodeTemplateGroupId;

    /**
     * 转码状态: Unstarted, Transcoding, TranscodeSuccess, TranscodeFail
     */
    private String transcodeStatus;

    /**
     * 视频状态: Uploading, UploadFail, UploadSuccess, Transcoding, TranscodeFail, Blocked, Normal
     */
    private String status;

    /**
     * 上传者 ID
     */
    private Long uploaderId;

    /**
     * 关联的原始媒体资产 ID
     */
    private Long sourceMediaAssetId;

    /**
     * 其他元数据
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
