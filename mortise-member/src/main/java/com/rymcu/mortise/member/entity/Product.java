package com.rymcu.mortise.member.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.rymcu.mortise.persistence.mybatis.handler.JsonbTypeHandler;
import com.rymcu.mortise.persistence.mybatis.handler.StringListTypeHandler;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 商品实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_product", schema = "mortise")
public class Product implements Serializable {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 商品标题
     */
    @NotBlank(message = "商品标题不能为空")
    private String title;

    /**
     * 商品副标题
     */
    private String subtitle;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 简短描述
     */
    private String shortDescription;

    /**
     * 封面图片URL
     */
    private String coverImageUrl;

    /**
     * 商品图片数组
     */
    @Column(typeHandler = StringListTypeHandler.class)
    private List<String> galleryImages;

    /**
     * 商品类型：course-课程, bundle-套餐, membership-会员, service-服务, material-资料, live_event-直播活动
     */
    @NotBlank(message = "商品类型不能为空")
    private String productType;

    /**
     * 分类ID
     */
    @NotNull(message = "所属分类不能为空")
    private Long categoryId;

    /**
     * 标签数组
     */
    @Column
    private String[] tags;

    /**
     * 商品特性
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> features;

    /**
     * 规格参数
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> specifications;

    /**
     * SEO标题
     */
    private String seoTitle;

    /**
     * SEO描述
     */
    private String seoDescription;

    /**
     * SEO关键词
     */
    private String seoKeywords;

    /**
     * 状态：0-草稿, 1-上架, 2-下架, 3-停产
     */
    private Integer status;

    /**
     * 是否推荐商品
     */
    private Boolean isFeatured;

    /**
     * 是否数字商品
     */
    private Boolean isDigital;

    /**
     * 是否需要物流
     */
    private Boolean requiresShipping;

    /**
     * 重量(克)
     */
    private Integer weightGrams;

    /**
     * 尺寸信息
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> dimensions;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 销售数量
     */
    private Integer saleCount;

    /**
     * 平均评分
     */
    private BigDecimal ratingAverage;

    /**
     * 评分数量
     */
    private Integer ratingCount;

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
