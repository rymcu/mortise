package com.rymcu.mortise.file.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.file.entity.FilePartDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件分片记录 Mapper
 *
 * @author ronger
 */
@Mapper
public interface FilePartDetailMapper extends BaseMapper<FilePartDetail> {
}
