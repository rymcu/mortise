package com.rymcu.mortise.service.impl;

import com.rymcu.mortise.service.UploadService;
import com.rymcu.mortise.util.FileUtils;
import jakarta.annotation.Resource;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.constant.Constant;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Predicate;

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
    private

    FileDetailService fileDetailService;

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

    /**
     * @param multipartFile
     * @return
     */
    @Override
    public FileInfo uploadImage(MultipartFile multipartFile) throws NoSuchAlgorithmException {
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
                .setThumbnailSuffix(".jpg") //指定缩略图后缀，必须是 thumbnailator 支持的图片格式，默认使用全局的
                .setSaveThFilename("_mini_") //指定缩略图的保存文件名，注意此文件名不含后缀，默认自动生成
                .image(img -> img.size(1000, 1000))  // 将图片大小调整到 1000*1000
                .thumbnail(th -> th.size(200, 200))  // 再生成一张 200*200 的缩略图
                .upload();
        // 保存文件信息
        fileDetailService.save(fileInfo);
        return fileInfo;
    }

    /**
     * @param fileInfo
     * @return
     */
    @Override
    public Boolean deleteFile(FileInfo fileInfo) {
        boolean deleteFlag = fileStorageService.delete(fileInfo);
        if (deleteFlag) {
            fileDetailService.delete(fileInfo.getUrl());
        }
        return deleteFlag;
    }

    /**
     * @param url
     * @return
     */
    @Override
    public Boolean deleteFile(String url) {
        boolean deleteFlag = fileStorageService.delete(url);
        if (deleteFlag) {
            fileDetailService.delete(url);
        }
        return deleteFlag;
    }


    /**
     * @param fileInfo
     * @param predicate
     * @return
     */
    @Override
    public Boolean deleteFileWitchCheck(
            FileInfo fileInfo,
            Predicate<FileInfo> predicate) {
        boolean deleteFlag = fileStorageService.delete(fileInfo, predicate);
        if (deleteFlag) {
            fileDetailService.delete(fileInfo.getUrl());
        }
        return deleteFlag;
    }


    /**
     * @param url
     * @param predicate
     * @return
     */
    @Override
    public Boolean deleteFileWitchCheck(
            String url
            ,
            Predicate<FileInfo> predicate) {
        boolean deleteFlag = fileStorageService.delete(url, predicate);
        if (deleteFlag) {
            fileDetailService.delete(url);
        }
        return deleteFlag;
    }

    /**
     * @param fileInfo
     * @return
     */
    @Override
    public byte[] downloadToBytes(FileInfo fileInfo) {
        boolean exists = fileStorageService.exists(fileInfo);
        return exists ? fileStorageService.download(fileInfo).bytes() : new byte[0];
    }

    /**
     * @param url
     * @return
     */
    @Override
    public byte[] downloadToBytes(String url) {
        boolean exists = fileStorageService.exists(url);
        return exists ? fileStorageService.download(url).bytes() : new byte[0];
    }

    /**
     * @param fileInfo
     * @param outputStream
     */
    @Override
    public void downloadToOutPutStream(
            FileInfo fileInfo,
            OutputStream outputStream) {
        boolean exists = fileStorageService.exists(fileInfo);
        if (exists) {
            fileStorageService.download(fileInfo).outputStream(outputStream);
        }

    }

    /**
     * @param url
     * @param outputStream
     */
    @Override
    public void downloadToOutPutStream(
            String url,
            OutputStream outputStream) {
        boolean exists = fileStorageService.exists(url);
        if (exists) {
            fileStorageService.download(url).outputStream(outputStream);
        }
    }
}
