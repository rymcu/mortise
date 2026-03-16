package com.rymcu.mortise.system.controller;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.file.entity.FileDetail;
import com.rymcu.mortise.file.mapper.FileDetailMapper;
import com.rymcu.mortise.file.service.FileDetailService;
import com.rymcu.mortise.web.annotation.AdminController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 后台文件管理控制器
 * 使用 x-file-storage 进行文件存储
 *
 * @author ronger
 */
@Slf4j
@Validated
@AdminController
@RequestMapping("/files")
@Tag(name = "文件管理", description = "后台文件上传与管理接口")
public class SystemFileController {

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private FileDetailMapper fileDetailMapper;

    @Resource
    private FileDetailService fileDetailService;

    /**
     * 分页查询文件列表
     */
    @Operation(summary = "文件列表", description = "分页查询已上传的文件，支持按文件名关键词过滤")
    @GetMapping
    @PreAuthorize("hasAuthority('system:file:list')")
    @com.rymcu.mortise.log.annotation.ApiLog(value = "查询文件列表", recordParams = false, recordResponseBody = false)
    public GlobalResult<Page<FileDetail>> listFiles(
            @Parameter(description = "页码，从 1 开始") @RequestParam(defaultValue = "1") @Min(1) int pageNumber,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") @Min(1) int pageSize,
            @Parameter(description = "文件名关键词") @RequestParam(required = false) String keyword) {
        var qw = QueryWrapper.create();
        if (StringUtils.hasText(keyword)) {
            var colOriginalFilename = new QueryColumn("original_filename");
            qw.and(colOriginalFilename.like(keyword));
        }
        qw.orderBy(new QueryColumn(FileDetail.COL_ID), false);
        Page<FileDetail> page = fileDetailMapper.paginate(new Page<>(pageNumber, pageSize), qw);
        return GlobalResult.success(page);
    }

    /**
     * 删除文件（同时删除存储介质上的文件和数据库记录）
     */
    @Operation(summary = "删除文件", description = "根据 ID 删除文件，同时清理存储介质上的实体文件")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:file:delete')")
    @com.rymcu.mortise.log.annotation.ApiLog(value = "删除文件")
    @com.rymcu.mortise.log.annotation.OperationLog(module = "文件管理", operation = "删除文件", recordParams = true)
    public GlobalResult<Boolean> deleteFile(
            @Parameter(description = "文件 ID", required = true) @PathVariable Long id) {
        FileDetail detail = fileDetailMapper.selectOneById(id);
        if (detail == null) {
            return GlobalResult.error("文件不存在");
        }
        try {
            FileInfo fileInfo = fileDetailService.getByUrl(detail.getUrl());
            if (fileInfo != null) {
                // delete() 会同时删除存储介质文件和数据库记录（通过 FileRecorder）
                fileStorageService.delete(fileInfo);
            } else {
                // 存储记录丢失，直接清除数据库记录
                fileDetailMapper.deleteById(id);
            }
            return GlobalResult.success(true);
        } catch (Exception e) {
            log.error("删除文件失败, id={}", id, e);
            return GlobalResult.error("删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件（用于头像等资源）
     */
    @Operation(summary = "文件上传", description = "上传文件并返回访问地址")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('system:file:upload')")
    @com.rymcu.mortise.log.annotation.ApiLog(value = "文件上传", recordRequestBody = false, recordResponseBody = false)
    public GlobalResult<FileInfo> upload(
            @NotNull @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return GlobalResult.error("文件不能为空");
        }
        try {
            FileInfo fileInfo = fileStorageService.of(file).upload();
            return GlobalResult.success(fileInfo);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return GlobalResult.error("文件上传失败: " + e.getMessage());
        }
    }
}
