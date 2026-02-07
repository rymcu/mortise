package com.rymcu.mortise.file.listener;

import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.aspect.DeleteAspectChain;
import org.dromara.x.file.storage.core.aspect.FileStorageAspect;
import org.dromara.x.file.storage.core.platform.FileStorage;
import org.dromara.x.file.storage.core.recorder.FileRecorder;
import org.springframework.stereotype.Component;

/**
 * 文件删除监听器
 * 监听文件删除事件，可以在此处执行额外的清理工作
 *
 * @author ronger
 */
@Slf4j
@Component
public class FileDeleteListener implements FileStorageAspect {

    /**
     * 删除文件之前的操作
     */
    @Override
    public boolean deleteAround(DeleteAspectChain chain, FileInfo fileInfo, FileStorage fileStorage, FileRecorder fileRecorder) {
        log.info("准备删除文件: url={}, platform={}", fileInfo.getUrl(), fileInfo.getPlatform());

        try {
            // 执行删除
            boolean success = chain.next(fileInfo, fileStorage, fileRecorder);

            if (success) {
                log.info("文件删除成功: url={}", fileInfo.getUrl());
                // 这里可以执行额外的清理工作，例如：
                // 1. 删除关联的缓存
                // 2. 发送删除事件通知
                // 3. 记录删除日志
            } else {
                log.warn("文件删除失败: url={}", fileInfo.getUrl());
            }

            return success;
        } catch (Exception e) {
            log.error("删除文件时发生异常: url={}", fileInfo.getUrl(), e);
            throw e;
        }
    }
}
