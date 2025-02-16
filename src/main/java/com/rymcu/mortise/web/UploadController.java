package com.rymcu.mortise.web;

import com.alibaba.fastjson2.JSONObject;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.core.result.GlobalResultGenerator;
import com.rymcu.mortise.service.impl.FileDetailService;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
@RequiresPermissions(value = "user")
public class UploadController {

    @Resource
    private FileDetailService fileDetailService;

    @PostMapping("/file")
    @Transactional(rollbackFor = Exception.class)
    @RequiresPermissions(value = "user")
    public GlobalResult<JSONObject> uploadFile(@RequestParam(value = "file", required = false) MultipartFile multipartFile) throws NoSuchAlgorithmException {
        if (multipartFile == null) {
            return GlobalResultGenerator.genErrorResult("请选择要上传的文件");
        }
        JSONObject data = new JSONObject(2);

        if (multipartFile.getSize() == 0) {
            data.put("message", "上传失败!");
            return GlobalResultGenerator.genSuccessResult(data);
        }

        FileInfo fileInfo = fileDetailService.uploadFile(multipartFile);

        data.put("fileUrl", fileInfo.getUrl());
        return GlobalResultGenerator.genSuccessResult(data);
    }

}
