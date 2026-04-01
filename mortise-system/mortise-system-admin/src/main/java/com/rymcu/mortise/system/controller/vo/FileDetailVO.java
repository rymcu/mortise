package com.rymcu.mortise.system.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileDetailVO {

    private Long id;
    private String url;
    private Long size;
    private String filename;
    private String originalFilename;
    private String basePath;
    private String path;
    private String ext;
    private String contentType;
    private String platform;
    private String thUrl;
    private String thFilename;
    private Long thSize;
    private String thContentType;
    private String objectId;
    private String objectType;
    private String metadata;
    private String userMetadata;
    private String thMetadata;
    private String thUserMetadata;
    private String attr;
    private String fileAcl;
    private String thFileAcl;
    private String hashInfo;
    private String uploadId;
    private Integer uploadStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
