package com.rymcu.mortise.member.api.controller;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.web.annotation.ApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 * 使用 x-file-storage 进行文件存储
 *
 * @author ronger
 */
@Slf4j
@Validated
@ApiController
@RequestMapping("/app/files")
@RequiredArgsConstructor
@Tag(name = "文件上传", description = "会员文件上传接口")
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * 上传文件
     */
    @Operation(summary = "文件上传")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public GlobalResult<FileInfo> upload(@NotNull @RequestParam("file") MultipartFile file) {
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return GlobalResult.error("文件不能为空");
            }

            // 上传文件
            FileInfo fileInfo = fileStorageService.of(file).upload();
            return GlobalResult.success(fileInfo);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return GlobalResult.error("文件上传失败: " + e.getMessage());
        }
    }
}
