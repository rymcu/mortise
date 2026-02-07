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
 * 商品SKU目标关联实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_product_sku_target", schema = "mortise")
public class ProductSkuTarget implements Serializable {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 商品SKU ID
     */
    @NotNull(message = "商品SKU ID 不能为空")
    private Long productSkuId;

    /**
     * 目标类型：course-课程, course_schedule-课程排期, bundle-套餐, membership-会员, live_event-直播活动, material-资料, service-服务
     */
    @NotBlank(message = "目标类型不能为空")
    private String targetType;

    /**
     * 关联目标实体ID
     */
    @NotNull(message = "关联目标实体ID 不能为空")
    private Long targetId;

    /**
     * 发放数量
     */
    @NotNull(message = "发放数量不能为空")
    private Integer quantity;

    /**
     * 有效天数
     */
    private Integer validityDays;

    /**
     * 访问级别
     */
    private String accessLevel;

    /**
     * 发放条件
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> conditions;

    /**
     * 扩展元数据
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> metadata;

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
