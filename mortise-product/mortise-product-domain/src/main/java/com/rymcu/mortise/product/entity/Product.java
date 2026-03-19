package com.rymcu.mortise.product.entity;

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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 产品实体 — 通用资源目录基础信息
 * <p>
 * 仅保留"产品是什么"的描述型元数据，不包含定价、库存、物流等交易属性。
 * 电商、IoT 等业务域可通过扩展表或 SPI 补充各自领域的附加属性。
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_product", schema = "mortise")
public class Product implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /** 产品编码（PRD-{ULID} 格式，全局唯一） */
    private String productCode;

    /** 产品标题 */
    @NotBlank(message = "产品标题不能为空")
    private String title;

    /** 副标题 */
    private String subtitle;

    /** 详细描述 */
    private String description;

    /** 简短描述 */
    private String shortDescription;

    /** 封面图片URL */
    private String coverImageUrl;

    /** 图片画廊 */
    @Column(typeHandler = StringListTypeHandler.class)
    private List<String> galleryImages;

    /** 产品类型编码（可通过 SPI 扩展自定义类型） */
    @NotBlank(message = "产品类型不能为空")
    private String productType;

    /** 所属分类ID */
    @NotNull(message = "所属分类不能为空")
    private Long categoryId;

    /** 标签 */
    @Column
    private String[] tags;

    /** 产品特性（JSON） */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> features;

    /** 规格参数（JSON） */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> specifications;

    /** SEO 标题 */
    private String seoTitle;

    /** SEO 描述 */
    private String seoDescription;

    /** SEO 关键词 */
    private String seoKeywords;

    /** 状态：0-草稿, 1-上架, 2-下架, 3-停用 */
    private Integer status;

    /** 是否推荐 */
    private Boolean isFeatured;

    /** 排序号 */
    private Integer sortNo;

    /** 创建人ID */
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedTime;

    @Column(isLogicDelete = true)
    private Integer delFlag;
}
