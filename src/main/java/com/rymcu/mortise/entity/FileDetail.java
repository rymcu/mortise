package com.rymcu.mortise.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * Created on 2025/2/16 10:46.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.entity
 */
@Data
@TableName(value = "mortise_file_detail", schema = "mortise")
public class FileDetail {


    @TableId(value = "id")
    @TableField(value = "id")
    private Long idFileDetail;

    private String url;

    private Long size;

    private String filename;

    private String originalFilename;

    /**
     * 基础存储路径
     */
    private String basePath;

    /**
     * 存储路径
     */
    private String path;

    /**
     * 文件扩展名
     */
    private String ext;

    /**
     * MIME类型
     */
    private String contentType;

    /**
     * 存储平台
     */
    private String platform;

    /**
     * 缩略图访问路径
     */
    private String thUrl;

    /**
     * 缩略图名称
     */
    private String thFilename;

    /**
     * 缩略图大小，单位字节
     */
    private Long thSize;

    /**
     * 缩略图MIME类型
     */
    private String thContentType;

    /**
     * 文件所属对象id
     */
    private String objectId;

    /**
     * 文件所属对象类型，例如用户头像，评价图片
     */
    private String objectType;

    /**
     * 文件ACL
     */
    private String fileAcl;

    /**
     * 缩略图文件ACL
     */
    private String thFileAcl;

    /**
     * 哈希信息
     */
    private String hashInfo;

    /**
     * 上传ID，仅在手动分片上传时使用
     */
    private String uploadId;

    /**
     * 上传状态，仅在手动分片上传时使用，1：初始化完成，2：上传完成
     */
    private Integer uploadStatus;

    /**
     * 时间
     */
    private Date createdTime;

    /**
     * 文件元数据
     */
    private String metadata;

    /**
     * 文件用户元数据
     */
    private String userMetadata;

    /**
     * 缩略图元数据'
     */
    private String thMetadata;

    /**
     * 缩略图用户元数据
     */
    private String thUserMetadata;

    /**
     * 附加属性
     */
    private String attr;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
