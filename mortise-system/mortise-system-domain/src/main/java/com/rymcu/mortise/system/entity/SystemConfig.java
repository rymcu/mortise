package com.rymcu.mortise.system.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统全局配置持久化实体
 * <p>
 * 每行记录对应一个配置组（config_group）下的一个配置项（config_key → config_value）。
 * Schema（字段定义）由 {@link com.rymcu.mortise.system.constant.SiteConfigSchema} 维护，不入库。
 *
 * @author ronger
 */
@Table(value = "mortise_system_config", schema = "mortise")
@Data
public class SystemConfig implements Serializable {

    /**
     * 主键
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 配置分组，如 site / seo
     */
    private String configGroup;

    /**
     * 配置项唯一 key，如 site.name / site.logo
     */
    private String configKey;

    /**
     * 配置项值
     */
    private String configValue;

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
}
