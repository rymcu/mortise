package com.rymcu.mortise.service.impl;

import com.rymcu.mortise.service.UploadService;
import com.rymcu.mortise.util.FileUtils;
import jakarta.annotation.Resource;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.constant.Constant;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created on 2025/2/16 20:03.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service.impl
 */
@Service
public class UploadServiceImpl implements UploadService {
    @Resource
    private FileStorageService fileStorageService;
    @Resource
    private FileDetailService fileDetailService;

    public FileInfo uploadFile(MultipartFile multipartFile) throws NoSuchAlgorithmException {
        String originalFilename = multipartFile.getOriginalFilename();
        FileInfo fileInfo = fileStorageService.of(multipartFile)
                .setSaveFilename(System.currentTimeMillis() + FileUtils.getExtend(originalFilename))
                .setOriginalFilename(originalFilename)
                //计算 MD5
                .setHashCalculatorMd5()
                //计算 SHA256
                .setHashCalculatorSha256()
                //指定哈希名称，这里定义了一些常用的哈希名称
                .setHashCalculator(Constant.Hash.MessageDigest.MD2)
                //指定哈希名称，内部是通过 MessageDigest 来计算哈希值的，只要是 MessageDigest 支持的名称就都可以
                .setHashCalculator("SHA-512")
                .setHashCalculator(MessageDigest.getInstance("SHA-384"))
                .upload();
        // 保存文件信息
        fileDetailService.save(fileInfo);
        return fileInfo;
    }
}
