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
 * 商品分类实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_product_category", schema = "mortise")
public class ProductCategory implements Serializable {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String name;

    /**
     * 分类别名
     */
    @NotBlank(message = "分类别名不能为空")
    private String slug;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 分类图片URL
     */
    private String imageUrl;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 是否激活
     */
    private Boolean isActive;

    /**
     * 状态：0-正常, 1-禁用
     */
    private Integer status;

    /**
     * 扩展元数据
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
