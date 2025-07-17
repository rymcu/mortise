package com.rymcu.mortise.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created on 2025/2/16 10:50.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.entity
 */
@Data
@Table(value = "mortise_file_part_detail", schema = "mortise")
public class FilePartDetail implements Serializable {

    @Id
    private Long id;

    private String platform;

    private String uploadId;

    private String eTag;

    private Integer partNumber;

    private Long partSize;

    private String hashInfo;

    private Date createdTime;

}
