package com.rymcu.mortise.service;

import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.function.Predicate;

/**
 * Created on 2025/2/16 20:03.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service
 */
public interface UploadService {
    FileInfo uploadFile(MultipartFile multipartFile) throws NoSuchAlgorithmException;


    FileInfo uploadImage(MultipartFile multipartFile) throws NoSuchAlgorithmException;

    Boolean deleteFile(FileInfo fileInfo);

    Boolean deleteFile(String url);

    Boolean deleteFileWitchCheck(
            FileInfo fileInfo,
            Predicate<FileInfo> predicate);

    Boolean deleteFileWitchCheck(
            String url,
            Predicate<FileInfo> predicate);

    byte[] downloadToBytes(FileInfo fileInfo);

    byte[] downloadToBytes(String url);

    void downloadToOutPutStream(
            FileInfo fileInfo,
            OutputStream outputStream);

    void downloadToOutPutStream(
            String url,
            OutputStream outputStream);
}
