package com.rymcu.mortise.system.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.rymcu.mortise.common.model.BaseOption;
import com.rymcu.mortise.system.entity.Dict;
import com.rymcu.mortise.system.model.DictInfo;
import com.rymcu.mortise.system.model.DictSearch;
import jakarta.validation.Valid;

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

    Boolean updateStatus(Long idDict, Integer status);

    Boolean deleteDict(Long idDict);

    Dict findById(Long idDict);

    String findLabelByTypeCodeAndValue(String dictTypeCode, String value);

    DictInfo findDictInfo(String dictTypeCode, String value);

    List<BaseOption> queryDictOptions(String dictTypeCode);

    Boolean batchDeleteDictionaries(List<Long> idDictList);

    Boolean createDict(@Valid Dict dict);

    Boolean updateDict(@Valid Dict dict);
}
