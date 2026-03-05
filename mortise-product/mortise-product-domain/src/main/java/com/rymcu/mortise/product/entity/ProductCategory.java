package com.rymcu.mortise.product.entity;

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
import java.util.List;
import java.util.Map;

/**
 * 产品分类实体 — 支持无限层级树形结构
 * <p>
 * 使用邻接表（parent_id）模型存储树，查询树结构由 Service 层重组。
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_product_category", schema = "mortise")
public class ProductCategory implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /** 分类名称 */
    @NotBlank(message = "分类名称不能为空")
    private String name;

    /** 分类别名（URL友好） */
    @NotBlank(message = "分类别名不能为空")
    private String slug;

    /** 分类描述 */
    private String description;

    /** 父分类ID，一级分类为 null */
    private Long parentId;

    /** 分类图片URL */
    private String imageUrl;

    /** 排序号 */
    private Integer sortNo;

    /** 是否激活 */
    private Boolean isActive;

    /** 状态：0-正常, 1-禁用 */
    private Integer status;

    /** 扩展元数据（JSON） */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> metadata;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    @Column(isLogicDelete = true)
    private Integer delFlag;

    // ---- 树形展示用，不映射数据库列 ----

    /** 子分类列表（由 Service 层组装，不持久化） */
    @Column(ignore = true)
    private List<ProductCategory> children;
}
