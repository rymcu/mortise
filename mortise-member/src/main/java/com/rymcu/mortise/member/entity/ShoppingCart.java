package com.rymcu.mortise.member.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 购物车实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_shopping_cart", schema = "mortise")
public class ShoppingCart implements Serializable {

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
     * 商品SKU ID
     */
    @NotNull(message = "商品SKU ID 不能为空")
    private Long productSkuId;

    /**
     * 数量
     */
    @NotNull(message = "数量不能为空")
    private Integer quantity;

    /**
     * 状态：0-正常, 1-失效
     */
    private Integer status;

    /**
     * 删除标记：0-未删除, 1-已删除
     */
    @Column(isLogicDelete = true)
    private Integer delFlag;

    /**
     * 添加时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime addedTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}
