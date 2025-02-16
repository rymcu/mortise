package com.rymcu.mortise.service;

import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.security.NoSuchAlgorithmException;

/**
 * Created on 2025/2/16 20:03.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service
 */
public interface UploadService {
    FileInfo uploadFile(MultipartFile multipartFile) throws NoSuchAlgorithmException;
}
