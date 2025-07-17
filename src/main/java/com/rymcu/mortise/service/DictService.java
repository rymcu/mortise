package com.rymcu.mortise.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.entity.Dict;
import com.rymcu.mortise.model.BaseOption;
import com.rymcu.mortise.model.DictInfo;
import com.rymcu.mortise.model.DictSearch;

import java.util.List;

/**
 * Created on 2024/9/22 20:04.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service
 */
public interface DictService extends IService<Dict> {
    Page<Dict> findDictList(Page<Dict> page, DictSearch search);

    Boolean saveDict(Dict dict);

    Boolean updateStatus(Long idDict, Integer status);

    Boolean updateDelFlag(Long idDict, Integer delFlag);

    Dict findById(Long idDict);

    String findLabelByTypeCodeAndValue(String dictTypeCode, String value);

    DictInfo findDictInfo(String dictTypeCode, String value);

    List<BaseOption> queryDictOptions(String dictTypeCode);

    Boolean batchUpdateDelFlag(List<Long> idDictList, Integer delFlag);
}
