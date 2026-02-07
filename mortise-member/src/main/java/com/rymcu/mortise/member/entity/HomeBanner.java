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
 * 首页轮播图实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_home_banner", schema = "mortise")
public class HomeBanner implements Serializable {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 轮播图标题
     */
    @NotBlank(message = "轮播图标题不能为空")
    private String title;

    /**
     * 副标题
     */
    private String subtitle;

    /**
     * 图片URL
     */
    @NotBlank(message = "图片URL不能为空")
    private String imageUrl;

    /**
     * 链接类型：none-无链接, internal-内部链接, external-外部链接, product-商品, course-课程, category-分类
     */
    private String linkType;

    /**
     * 链接值（根据linkType存储不同内容）
     */
    private String linkValue;

    /**
     * 展示位置：home-首页, category-分类页, member-会员中心
     */
    private String position;

    /**
     * 平台：all-全平台, h5-H5, miniapp-小程序, app-APP
     */
    private String platform;

    /**
     * 排序号，数值越小越靠前
     */
    private Integer sortNo;

    /**
     * 生效开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 生效结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 状态：0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 点击次数统计
     */
    private Integer clickCount;

    /**
     * 曝光次数统计
     */
    private Integer viewCount;

    /**
     * 背景色（可选）
     */
    private String backgroundColor;

    /**
     * 扩展元数据
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> metadata;

    /**
     * 创建人ID
     */
    private Long createdBy;

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
