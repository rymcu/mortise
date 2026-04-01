package com.rymcu.mortise.system.query;

import com.rymcu.mortise.core.model.PageQuery;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.system.entity.DictType;
import com.rymcu.mortise.system.model.DictTypeSearch;

public interface DictTypeQueryService {

    PageResult<DictType> findDictTypeList(PageQuery pageQuery, DictTypeSearch search);

    DictType findById(Long dictTypeId);
}
