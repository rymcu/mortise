package com.rymcu.mortise.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

/**
 * 文件上传控制器
 * 提供文件和图片的上传、下载、删除功能
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @since 2024/12/10
 */
@Tag(name = "文件管理", description = "文件上传下载相关接口")
@RestController
@RequestMapping("/api/v1/files")
@PreAuthorize("isAuthenticated()")
public class FileController {

    @Resource
    private UploadService uploadService;

    @Operation(summary = "上传文件", description = "上传文件到服务器")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "上传成功"),
            @ApiResponse(responseCode = "400", description = "文件为空或参数错误"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    @PostMapping("/upload")
    @Transactional(rollbackFor = Exception.class)
    public GlobalResult<ObjectNode> uploadFile(@Parameter(description = "上传的文件", required = true) @RequestParam("file") MultipartFile multipartFile) throws NoSuchAlgorithmException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return GlobalResult.error("请选择要上传的文件");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode data = objectMapper.createObjectNode();

        FileInfo fileInfo = uploadService.uploadFile(multipartFile);
        data.put("fileUrl", fileInfo.getUrl());
        data.put("fileName", fileInfo.getOriginalFilename());
        data.put("fileSize", fileInfo.getSize());

        return GlobalResult.success(data);
    }

    @Operation(summary = "上传图片", description = "上传图片到服务器，支持缩略图生成")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "上传成功"),
            @ApiResponse(responseCode = "400", description = "图片为空或参数错误"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    @PostMapping("/images/upload")
    @Transactional(rollbackFor = Exception.class)
    public GlobalResult<ObjectNode> uploadImage(@Parameter(description = "上传的图片文件", required = true) @RequestParam("file") MultipartFile multipartFile) throws NoSuchAlgorithmException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return GlobalResult.error("请选择要上传的图片");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode data = objectMapper.createObjectNode();

        FileInfo fileInfo = uploadService.uploadImage(multipartFile);
        data.put("imageUrl", fileInfo.getUrl());
        data.put("thumbnailUrl", fileInfo.getThUrl());
        data.put("fileName", fileInfo.getOriginalFilename());
        data.put("fileSize", fileInfo.getSize());

        return GlobalResult.success(data);
    }

    @Operation(summary = "删除文件", description = "根据文件URL删除服务器上的文件")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "URL参数错误"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    @DeleteMapping
    @Transactional(rollbackFor = Exception.class)
    public GlobalResult<ObjectNode> deleteFile(@Parameter(description = "文件URL", required = true) @RequestParam("url") @NotBlank(message = "文件URL不能为空") String url) {
        uploadService.deleteFile(url);

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode data = objectMapper.createObjectNode();
        data.put("url", url);
        data.put("message", "文件删除成功");

        return GlobalResult.success(data);
    }

    @Operation(summary = "下载文件", description = "根据文件URL下载文件")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "下载成功"),
            @ApiResponse(responseCode = "400", description = "URL参数错误"),
            @ApiResponse(responseCode = "404", description = "文件不存在"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    @GetMapping("/download")
    public void downloadFile(@Parameter(description = "文件URL", required = true) @RequestParam("url") @NotBlank(message = "文件URL不能为空") String url,
                            HttpServletResponse response) throws IOException {
        try (OutputStream outputStream = response.getOutputStream()) {
            uploadService.downloadToOutPutStream(url, outputStream);
        }
    }
}
