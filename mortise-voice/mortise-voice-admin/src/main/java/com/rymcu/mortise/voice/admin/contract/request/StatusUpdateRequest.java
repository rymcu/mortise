package com.rymcu.mortise.voice.admin.contract.request;

import jakarta.validation.constraints.NotNull;

/**
 * 通用状态更新请求。
 */
public record StatusUpdateRequest(@NotNull Integer status) {
}