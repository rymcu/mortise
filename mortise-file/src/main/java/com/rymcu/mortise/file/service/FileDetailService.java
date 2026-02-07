package com.rymcu.mortise.file.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.file.entity.FileDetail;
import com.rymcu.mortise.file.mapper.FileDetailMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.hash.HashInfo;
import org.dromara.x.file.storage.core.recorder.FileRecorder;
import org.dromara.x.file.storage.core.upload.FilePartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 文件记录服务
 * 实现 x-file-storage 的 FileRecorder 接口
 * 用于将文件上传记录保存到数据库
 *
 * @author ronger
 */
@Slf4j
@Service
public class FileDetailService implements FileRecorder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private FileDetailMapper fileDetailMapper;

    @Autowired
    private FilePartDetailService filePartDetailService;

    /**
     * 保存文件信息到数据库
     */
    @SneakyThrows
    @Override
    public boolean save(FileInfo info) {
        FileDetail detail = toFileDetail(info);
        int rows = fileDetailMapper.insert(detail);
        if (rows > 0) {
            info.setId(String.valueOf(detail.getId()));
        }
        return rows > 0;
    }

    /**
     * 更新文件记录
     * 主要用在手动分片上传文件-完成上传，作用是更新文件信息
     */
    @SneakyThrows
    @Override
    public void update(FileInfo info) {
        FileDetail detail = toFileDetail(info);
        QueryWrapper qw = QueryWrapper.create();
        if (detail.getUrl() != null) {
            qw.eq(FileDetail.COL_URL, detail.getUrl());
        } else if (detail.getId() != null) {
            qw.eq(FileDetail.COL_ID, detail.getId());
        }
        fileDetailMapper.updateByQuery(detail, qw);
    }

    /**
     * 根据 url 查询文件信息
     */
    @SneakyThrows
    @Override
    public FileInfo getByUrl(String url) {
        FileDetail detail = fileDetailMapper.selectOneByQuery(
                QueryWrapper.create().eq(FileDetail.COL_URL, url)
        );
        return toFileInfo(detail);
    }

    /**
     * 根据 url 删除文件信息
     */
    @Override
    public boolean delete(String url) {
        fileDetailMapper.deleteByQuery(
                QueryWrapper.create().eq(FileDetail.COL_URL, url)
        );
        return true;
    }

    /**
     * 保存文件分片信息
     *
     * @param filePartInfo 文件分片信息
     */
    @Override
    public void saveFilePart(FilePartInfo filePartInfo) {
        filePartDetailService.saveFilePart(filePartInfo);
    }

    /**
     * 删除文件分片信息
     */
    @Override
    public void deleteFilePartByUploadId(String uploadId) {
        filePartDetailService.deleteFilePartByUploadId(uploadId);
    }

    /**
     * 将 FileInfo 转为 FileDetail
     */
    public FileDetail toFileDetail(FileInfo info) throws JsonProcessingException {
        FileDetail detail = BeanUtil.copyProperties(
                info, FileDetail.class, "metadata", "userMetadata", "thMetadata", "thUserMetadata", "attr", "hashInfo", "createTime");

        // 处理 id 转换
        if (info.getId() != null) {
            try {
                detail.setId(Long.parseLong(info.getId()));
            } catch (NumberFormatException e) {
                log.warn("无法转换文件ID: {}", info.getId());
            }
        }

        // 将 Date 转换为 LocalDateTime
        if (info.getCreateTime() != null) {
            detail.setCreateTime(info.getCreateTime().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime());
        }

        // 这里手动获取元数据并转成 json 字符串，方便存储在数据库中
        detail.setMetadata(valueToJson(info.getMetadata()));
        detail.setUserMetadata(valueToJson(info.getUserMetadata()));
        detail.setThMetadata(valueToJson(info.getThMetadata()));
        detail.setThUserMetadata(valueToJson(info.getThUserMetadata()));
        // 这里手动获取附加属性字典并转成 json 字符串，方便存储在数据库中
        detail.setAttr(valueToJson(info.getAttr()));
        // 这里手动获取哈希信息并转成 json 字符串，方便存储在数据库中
        detail.setHashInfo(valueToJson(info.getHashInfo()));
        return detail;
    }

    /**
     * 将 FileDetail 转为 FileInfo
     */
    public FileInfo toFileInfo(FileDetail detail) throws JsonProcessingException {
        if (detail == null) {
            return null;
        }
        FileInfo info = BeanUtil.copyProperties(
                detail, FileInfo.class, "metadata", "userMetadata", "thMetadata", "thUserMetadata", "attr", "hashInfo", "createTime");

        // 处理 id 转换
        if (detail.getId() != null) {
            info.setId(String.valueOf(detail.getId()));
        }

        // 将 LocalDateTime 转换为 Date
        if (detail.getCreateTime() != null) {
            info.setCreateTime(java.util.Date.from(detail.getCreateTime()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant()));
        }

        // 这里手动获取数据库中的 json 字符串并转成元数据，方便使用
        info.setMetadata(jsonToMetadata(detail.getMetadata()));
        info.setUserMetadata(jsonToMetadata(detail.getUserMetadata()));
        info.setThMetadata(jsonToMetadata(detail.getThMetadata()));
        info.setThUserMetadata(jsonToMetadata(detail.getThUserMetadata()));
        // 这里手动获取数据库中的 json 字符串并转成附加属性字典，方便使用
        info.setAttr(jsonToDict(detail.getAttr()));
        // 这里手动获取数据库中的 json 字符串并转成哈希信息，方便使用
        info.setHashInfo(jsonToHashInfo(detail.getHashInfo()));
        return info;
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

    /**
     * 将 json 字符串转换成元数据对象
     */
    public Map<String, String> jsonToMetadata(String json) throws JsonProcessingException {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
    }

    /**
     * 将 json 字符串转换成字典对象
     */
    public Dict jsonToDict(String json) throws JsonProcessingException {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        return objectMapper.readValue(json, Dict.class);
    }

    /**
     * 将 json 字符串转换成哈希信息对象
     */
    public HashInfo jsonToHashInfo(String json) throws JsonProcessingException {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        return objectMapper.readValue(json, HashInfo.class);
    }
}
