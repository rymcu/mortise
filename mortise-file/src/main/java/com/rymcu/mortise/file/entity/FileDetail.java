package com.rymcu.mortise.file.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件记录表
 * 用于保存 x-file-storage 上传的文件信息
 *
 * @author ronger
 */
@Data
@Table("mortise_file_detail")
public class FileDetail implements Serializable {

    public static final String COL_ID = "id";
    public static final String COL_URL = "url";

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 文件访问地址
     */
    @Column("url")
    private String url;

    /**
     * 文件大小，单位字节
     */
    @Column("size")
    private Long size;

    /**
     * 文件名称
     */
    @Column("filename")
    private String filename;

    /**
     * 原始文件名
     */
    @Column("original_filename")
    private String originalFilename;

    /**
     * 基础存储路径
     */
    @Column("base_path")
    private String basePath;

    /**
     * 存储路径
     */
    @Column("path")
    private String path;

    /**
     * 文件扩展名
     */
    @Column("ext")
    private String ext;

    /**
     * MIME类型
     */
    @Column("content_type")
    private String contentType;

    /**
     * 存储平台
     */
    @Column("platform")
    private String platform;

    /**
     * 缩略图访问路径
     */
    @Column("th_url")
    private String thUrl;

    /**
     * 缩略图名称
     */
    @Column("th_filename")
    private String thFilename;

    /**
     * 缩略图大小，单位字节
     */
    @Column("th_size")
    private Long thSize;

    /**
     * 缩略图MIME类型
     */
    @Column("th_content_type")
    private String thContentType;

    /**
     * 文件所属对象id
     */
    @Column("object_id")
    private String objectId;

    /**
     * 文件所属对象类型
     */
    @Column("object_type")
    private String objectType;

    /**
     * 文件元数据
     */
    @Column("metadata")
    private String metadata;

    /**
     * 文件用户元数据
     */
    @Column("user_metadata")
    private String userMetadata;

    /**
     * 缩略图元数据
     */
    @Column("th_metadata")
    private String thMetadata;

    /**
     * 缩略图用户元数据
     */
    @Column("th_user_metadata")
    private String thUserMetadata;

    /**
     * 附加属性
     */
    @Column("attr")
    private String attr;

    /**
     * 文件ACL
     */
    @Column("file_acl")
    private String fileAcl;

    /**
     * 缩略图文件ACL
     */
    @Column("th_file_acl")
    private String thFileAcl;

    /**
     * 哈希信息
     */
    @Column("hash_info")
    private String hashInfo;

    /**
     * 上传ID，仅在手动分片上传时使用
     */
    @Column("upload_id")
    private String uploadId;

    /**
     * 上传状态，仅在手动分片上传时使用
     * 1：初始化完成，2：上传完成
     */
    @Column("upload_status")
    private Integer uploadStatus;

    /**
     * 创建时间
     */
    @Column("create_time")
    private LocalDateTime createTime;
}
