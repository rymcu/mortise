package com.rymcu.mortise.system.query;

import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.common.model.BaseOption;
import com.rymcu.mortise.system.entity.Dict;
import com.rymcu.mortise.system.model.DictInfo;
import com.rymcu.mortise.system.model.DictSearch;

import java.util.List;

public interface DictQueryService {

    PageResult<Dict> findDictList(PageQuery pageQuery, DictSearch search);

    Dict findById(Long dictId);

    String findLabelByTypeCodeAndValue(String dictTypeCode, String value);

    DictInfo findDictInfo(String dictTypeCode, String value);

    List<BaseOption> queryDictOptions(String dictTypeCode);
}
