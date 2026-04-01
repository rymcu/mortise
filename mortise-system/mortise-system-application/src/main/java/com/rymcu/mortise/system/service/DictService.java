package com.rymcu.mortise.system.service;

import com.rymcu.mortise.common.model.BaseOption;
import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.entity.Dict;
import com.rymcu.mortise.system.model.DictInfo;
import com.rymcu.mortise.system.model.DictSearch;

import java.util.List;

/**
 * Created on 2024/9/22 20:04.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service
 */
public interface DictService {
    PageResult<Dict> findDictList(PageQuery pageQuery, DictSearch search);

    Boolean updateStatus(Long idDict, Integer status);

    Boolean deleteDict(Long idDict);

    Dict findById(Long idDict);

    String findLabelByTypeCodeAndValue(String dictTypeCode, String value);

    DictInfo findDictInfo(String dictTypeCode, String value);

    List<BaseOption> queryDictOptions(String dictTypeCode);

    Boolean batchDeleteDictionaries(List<Long> idDictList);

    Long createDict(Dict dict);

    Boolean updateDict(Dict dict);
}
