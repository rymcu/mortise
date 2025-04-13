package com.rymcu.mortise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rymcu.mortise.entity.DictType;
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
public interface DictTypeMapper extends BaseMapper<DictType> {

    List<DictType> selectDictTypeList(@Param("page") Page<DictType> page, @Param("query") String query, @Param("typeCode") String typeCode, @Param("status") Integer status);

    int updateStatus(@Param("idDictType") Long idDictType, @Param("status") Integer status);

    int updateDelFlag(@Param("idDictType") Long idDictType, @Param("delFlag") Integer delFlag);

    int batchUpdateDelFlag(@Param("idDictTypes") List<Long> idDictTypes, @Param("delFlag") Integer delFlag);
}
