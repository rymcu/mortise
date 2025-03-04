package com.rymcu.mortise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rymcu.mortise.entity.FilePartDetail;
import com.rymcu.mortise.mapper.FilePartDetailMapper;
import lombok.SneakyThrows;
import org.dromara.x.file.storage.core.upload.FilePartInfo;
import org.springframework.stereotype.Service;

/**
 * Created on 2025/2/16 18:23.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service.impl
 */
@Service
public class FilePartDetailService extends ServiceImpl<FilePartDetailMapper, FilePartDetail> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 保存文件分片信息
     * @param info 文件分片信息
     */
    @SneakyThrows
    public void saveFilePart(FilePartInfo info) {
        FilePartDetail detail = toFilePartDetail(info);
        if (save(detail)) {
            info.setId(String.valueOf(detail.getIdFilePartDetail()));
        }
    }

    /**
     * 删除文件分片信息
     */
    public void deleteFilePartByUploadId(String uploadId) {
        remove(new QueryWrapper<FilePartDetail>().eq("upload_id", uploadId));
    }

    /**
     * 将 FilePartInfo 转成 FilePartDetail
     * @param info 文件分片信息
     */
    public FilePartDetail toFilePartDetail(FilePartInfo info) throws JsonProcessingException {
        FilePartDetail detail = new FilePartDetail();
        detail.setPlatform(info.getPlatform());
        detail.setUploadId(info.getUploadId());
        detail.setETag(info.getETag());
        detail.setPartNumber(info.getPartNumber());
        detail.setPartSize(info.getPartSize());
        detail.setHashInfo(valueToJson(info.getHashInfo()));
        detail.setCreatedTime(info.getCreateTime());
        return detail;
    }

    /**
     * 将指定值转换成 json 字符串
     */
    public String valueToJson(Object value) throws JsonProcessingException {
        if (value == null) return null;
        return objectMapper.writeValueAsString(value);
    }
}
