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
 * 金刚区快捷入口实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_quick_entry", schema = "mortise")
public class QuickEntry implements Serializable {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 入口名称
     */
    @NotBlank(message = "入口名称不能为空")
    private String name;

    /**
     * 图标URL
     */
    @NotBlank(message = "图标URL不能为空")
    private String iconUrl;

    /**
     * 图标类型：image-图片, icon-字体图标, lottie-动画
     */
    private String iconType;

    /**
     * 图标值（字体图标名称或动画标识）
     */
    private String iconValue;

    /**
     * 链接类型：none-无链接, internal-内部链接, external-外部链接, product-商品, course-课程, category-分类, miniapp-小程序
     */
    private String linkType;

    /**
     * 链接值（根据linkType存储不同内容）
     */
    private String linkValue;

    /**
     * 小程序appid（linkType为miniapp时使用）
     */
    private String linkAppid;

    /**
     * 展示位置：home-首页, category-分类页
     */
    private String position;

    /**
     * 平台：all-全平台, h5-H5, miniapp-小程序, app-APP
     */
    private String platform;

    /**
     * 分组名称（用于多行展示时分组）
     */
    private String groupName;

    /**
     * 行索引（第几行）
     */
    private Integer rowIndex;

    /**
     * 排序号，数值越小越靠前
     */
    private Integer sortNo;

    /**
     * 角标文字
     */
    private String badgeText;

    /**
     * 角标类型：hot-热门, new-新品, sale-促销, custom-自定义
     */
    private String badgeType;

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
     * 背景色（可选）
     */
    private String backgroundColor;

    /**
     * 文字颜色（可选）
     */
    private String textColor;

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
