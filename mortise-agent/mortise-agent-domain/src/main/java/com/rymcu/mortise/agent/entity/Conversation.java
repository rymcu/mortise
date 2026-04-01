package com.rymcu.mortise.agent.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话领域对象。
 */
@Data
public class Conversation {

    private Long id;

    private Long userId;

    private String title;

    private Integer status;

    private Integer delFlag;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
