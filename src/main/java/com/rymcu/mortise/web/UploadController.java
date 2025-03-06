package com.rymcu.mortise.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.core.result.GlobalResultGenerator;
import com.rymcu.mortise.service.UploadService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;
import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

/**
 * Created on 2024/12/10 20:14.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.web
 */
@RestController
@RequestMapping("/api/v1/upload")
@PreAuthorize("isAuthenticated()")
public class UploadController {

    @Resource
    private UploadService uploadService;

    @PostMapping("/file")
    @Transactional(rollbackFor = Exception.class)
    public GlobalResult<ObjectNode> uploadFile(@RequestParam(value = "file", required = false) MultipartFile multipartFile) throws NoSuchAlgorithmException {
        if (multipartFile == null) {
            return GlobalResultGenerator.genErrorResult("请选择要上传的文件");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode data = objectMapper.createObjectNode();

        if (multipartFile.getSize() == 0) {
            data.put("message", "上传失败!");
            return GlobalResultGenerator.genSuccessResult(data);
        }

        FileInfo fileInfo = uploadService.uploadFile(multipartFile);

        data.put("fileUrl", fileInfo.getUrl());
        return GlobalResultGenerator.genSuccessResult(data);
    }


    @PostMapping("/image")
    @Transactional(rollbackFor = Exception.class)
    public GlobalResult<ObjectNode> uploadImage(@RequestParam(value = "file", required = false) MultipartFile multipartFile) throws NoSuchAlgorithmException {
        if (multipartFile == null) {
            return GlobalResultGenerator.genErrorResult("请选择要上传的图片");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode data = objectMapper.createObjectNode();

        if (multipartFile.getSize() == 0) {
            data.put("message", "上传失败!");
            return GlobalResultGenerator.genSuccessResult(data);
        }

        FileInfo fileInfo = uploadService.uploadImage(multipartFile);

        data.put("imgUrl", fileInfo.getUrl());
        data.put("imgTpUrl", fileInfo.getThUrl());
        return GlobalResultGenerator.genSuccessResult(data);
    }


    @DeleteMapping("/file")
    @Transactional(rollbackFor = Exception.class)
    public GlobalResult<ObjectNode> delete(@RequestParam(name = "String") @NotBlank(message = "请稍后下载") String url) throws NoSuchAlgorithmException {
        if (StringUtils.isBlank(url)) {
            return GlobalResultGenerator.genErrorResult("请稍后删除");
        }
        uploadService.deleteFile(url);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode data = objectMapper.createObjectNode();

        data.put("url", url);
        return GlobalResultGenerator.genSuccessResult(data);
    }

    @GetMapping("/download")
    public void download(
            @RequestParam(name = "url") @NotBlank(message = "请稍后下载") String url,
            HttpServletResponse response) throws IOException {
        try (OutputStream outputStream = response.getOutputStream()) {

            uploadService.downloadToOutPutStream(url, outputStream

            );
        }


    }
}
