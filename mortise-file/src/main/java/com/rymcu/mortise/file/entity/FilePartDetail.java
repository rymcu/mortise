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
 * 文件分片信息表
 * 仅在手动分片上传时使用
 *
 * @author ronger
 */
@Data
@Table("mortise_file_part_detail")
public class FilePartDetail implements Serializable {

    public static final String COL_UPLOAD_ID = "upload_id";

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 存储平台
     */
    @Column("platform")
    private String platform;

    /**
     * 上传ID
     */
    @Column("upload_id")
    private String uploadId;

    /**
     * 分片 ETag
     */
    @Column("e_tag")
    private String eTag;

    /**
     * 分片号
     */
    @Column("part_number")
    private Integer partNumber;

    /**
     * 文件大小，单位字节
     */
    @Column("part_size")
    private Long partSize;

    /**
     * 哈希信息
     */
    @Column("hash_info")
    private String hashInfo;

    /**
     * 创建时间
     */
    @Column("create_time")
    private LocalDateTime createTime;
}
