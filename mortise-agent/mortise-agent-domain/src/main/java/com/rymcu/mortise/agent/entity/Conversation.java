package com.rymcu.mortise.agent.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话实体
 *
 * @author ronger
 */
@Data
@Table(value = "mortise_conversation", schema = "mortise")
public class Conversation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /** 所属用户 ID */
    private Long userId;

    /** 会话标题（通常取自首条消息） */
    private String title;

    /** 状态：0=禁用/归档, 1=启用/活跃 */
    private Integer status;

    /** 逻辑删除 */
    @Column(isLogicDelete = true)
    private Integer delFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(onInsertValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(onInsertValue = "CURRENT_TIMESTAMP", onUpdateValue = "CURRENT_TIMESTAMP")
    private LocalDateTime updatedTime;
}
