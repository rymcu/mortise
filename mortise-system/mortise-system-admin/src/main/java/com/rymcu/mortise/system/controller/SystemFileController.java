package com.rymcu.mortise.system.controller;

import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.controller.facade.SystemFileAdminFacade;
import com.rymcu.mortise.system.controller.vo.FileDetailVO;
import com.rymcu.mortise.web.annotation.AdminController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 后台文件管理控制器
 * 使用 x-file-storage 进行文件存储
 *
 * @author ronger
 */
@Validated
@AdminController
@RequestMapping("/files")
@Tag(name = "文件管理", description = "后台文件上传与管理接口")
public class SystemFileController {

    @Resource
    private SystemFileAdminFacade systemFileAdminFacade;

    /**
     * 分页查询文件列表
     */
    @Operation(summary = "文件列表", description = "分页查询已上传的文件，支持按文件名关键词过滤")
    @GetMapping
    @PreAuthorize("hasAuthority('system:file:list')")
    @com.rymcu.mortise.log.annotation.ApiLog(value = "查询文件列表", recordParams = false, recordResponseBody = false)
    public GlobalResult<PageResult<FileDetailVO>> listFiles(
            @Parameter(description = "页码，从 1 开始") @RequestParam(defaultValue = "1") @Min(1) int pageNumber,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") @Min(1) int pageSize,
            @Parameter(description = "文件名关键词") @RequestParam(required = false) String keyword) {
        return systemFileAdminFacade.listFiles(pageNumber, pageSize, keyword);
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
        return systemFileAdminFacade.deleteFile(id);
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
        return systemFileAdminFacade.upload(file);
    }
}
