package com.rymcu.mortise.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created on 2024/9/22 20:03.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.entity
 */
@Table(name = "mortise_dict")
@Data
public class DictType {
    /**
     * 主键
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    private String id;
    /**
     * 名称
     */
    private String typeName;
    /**
     * 代码
     */
    private String typeCode;
    /**
     * 排序
     */
    private Integer sortNo;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 创建人
     */
    private Long createdBy;
    /**
     * 创建时间
     */
    private Date createdTime;
    /**
     * 更新人
     */
    private Long updatedBy;
    /**
     * 更新时间
     */
    private Date updatedTime;
}
