package com.rymcu.mortise.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rymcu.mortise.entity.Dict;
import com.rymcu.mortise.model.BaseOption;
import com.rymcu.mortise.model.DictInfo;
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

    Dict selectByTypeCodeAndValue(@Param("dictTypeCode") String dictTypeCode, @Param("value") String value);

    DictInfo selectDictInfo(@Param("dictTypeCode") String dictTypeCode, @Param("value") String value);

    List<BaseOption> selectDictOptions(@Param("dictTypeCode") String dictTypeCode);

}
