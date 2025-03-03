package com.rymcu.mortise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rymcu.mortise.entity.Dict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created on 2024/9/22 19:58.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.mapper
 */
@Mapper
public interface DictMapper extends BaseMapper<Dict> {
    List<Dict> selectDictList(@Param("page") Page<Dict> page, @Param("query") String query, @Param("dictTypeCode") String dictTypeCode, @Param("status") Integer status);

    int updateStatus(@Param("idDict") Long idDict, @Param("status") Integer status);

    int updateDelFlag(@Param("idDict") Long idDict, @Param("delFlag") Integer delFlag);
}
