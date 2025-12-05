package com.rymcu.mortise.system.service;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.system.entity.DictType;
import com.rymcu.mortise.system.model.DictTypeSearch;
import jakarta.validation.Valid;

import java.util.List;

/**
 * Created on 2024/9/22 20:04.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service
 */
public interface DictTypeService {
    Page<DictType> findDictTypeList(Page<DictType> page, DictTypeSearch search);

    Boolean updateStatus(Long idDictType, Integer status);

    Boolean deleteDictType(Long idDictType);

    DictType findById(Long idDictType);

    Boolean batchDeleteDictTypes(List<Long> idDictTypes);

    Long createDictType(@Valid DictType dictType);

    Boolean updateDictType(@Valid DictType dictType);
}
