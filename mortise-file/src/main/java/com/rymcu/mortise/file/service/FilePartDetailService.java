package com.rymcu.mortise.file.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.file.entity.FilePartDetail;
import com.rymcu.mortise.file.mapper.FilePartDetailMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.upload.FilePartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 文件分片上传记录服务
 * 仅在手动分片上传时使用
 *
 * @author ronger
 */
@Slf4j
@Service
public class FilePartDetailService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private FilePartDetailMapper filePartDetailMapper;

    /**
     * 保存文件分片信息
     *
     * @param info 文件分片信息
     */
    @SneakyThrows
    public void saveFilePart(FilePartInfo info) {
        FilePartDetail detail = toFilePartDetail(info);
        filePartDetailMapper.insert(detail);
        info.setId(String.valueOf(detail.getId()));
    }

    /**
     * 删除文件分片信息
     *
     * @param uploadId 上传ID
     */
    public void deleteFilePartByUploadId(String uploadId) {
        filePartDetailMapper.deleteByQuery(
                QueryWrapper.create().eq(FilePartDetail.COL_UPLOAD_ID, uploadId)
        );
    }

    /**
     * 将 FilePartInfo 转成 FilePartDetail
     *
     * @param info 文件分片信息
     * @return FilePartDetail
     */
    public FilePartDetail toFilePartDetail(FilePartInfo info) throws JsonProcessingException {
        FilePartDetail detail = new FilePartDetail();
        detail.setPlatform(info.getPlatform());
        detail.setUploadId(info.getUploadId());
        detail.setETag(info.getETag());
        detail.setPartNumber(info.getPartNumber());
        detail.setPartSize(info.getPartSize());
        detail.setHashInfo(valueToJson(info.getHashInfo()));
        // 将 Date 转换为 LocalDateTime
        if (info.getCreateTime() != null) {
            detail.setCreateTime(info.getCreateTime().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime());
        }
        return detail;
    }

    /**
     * 将指定值转换成 json 字符串
     */
    public String valueToJson(Object value) throws JsonProcessingException {
        if (value == null) {
            return null;
        }
        return objectMapper.writeValueAsString(value);
    }
}
