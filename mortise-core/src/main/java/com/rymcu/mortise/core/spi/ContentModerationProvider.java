package com.rymcu.mortise.core.spi;

import com.rymcu.mortise.core.model.ContentModerationRequest;
import com.rymcu.mortise.core.model.ContentModerationResult;

/**
 * 内容审核 SPI。
 * <p>
 * 定义在 core 作为跨业务域扩展点，具体实现由业务模块按需提供。
 * 该接口只定义中立契约，不包含社区专有实体。
 * </p>
 */
public interface ContentModerationProvider {

    /**
     * 审核顺序，值越小优先级越高。
     *
     * @return 顺序
     */
    default int getOrder() {
        return 0;
    }

    /**
     * 是否支持当前业务类型。
     *
     * @param bizType 业务类型
     * @return true 表示支持
     */
    default boolean supports(String bizType) {
        return true;
    }

    /**
     * 执行内容审核。
     *
     * @param request 审核请求
     * @return 审核结果
     */
    default ContentModerationResult moderate(ContentModerationRequest request) {
        return ContentModerationResult.pass();
    }
}
