package com.rymcu.mortise.file.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.file.entity.FileDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件记录 Mapper
 *
 * @author ronger
 */
@Mapper
public interface FileDetailMapper extends BaseMapper<FileDetail> {
}
