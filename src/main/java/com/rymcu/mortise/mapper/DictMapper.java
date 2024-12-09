package com.rymcu.mortise.mapper;

import com.rymcu.mortise.core.mapper.Mapper;
import com.rymcu.mortise.entity.Dict;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created on 2024/9/22 19:58.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.mapper
 */
public interface DictMapper extends Mapper<Dict> {
    List<Dict> selectDictList(@Param("query") String query, @Param("dictTypeCode") String dictTypeCode, @Param("status") Integer status);

    int updateStatus(@Param("idDict") Long idDict, @Param("status") Integer status);

    int updateDelFlag(@Param("idDict") Long idDict, @Param("delFlag") Integer delFlag);
}
