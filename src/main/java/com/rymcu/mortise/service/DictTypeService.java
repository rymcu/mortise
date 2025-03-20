package com.rymcu.mortise.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rymcu.mortise.entity.DictType;
import com.rymcu.mortise.model.DictTypeSearch;

/**
 * Created on 2024/9/22 20:04.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service
 */
public interface DictTypeService {
    IPage<DictType> findDictTypeList(Page<DictType> page, DictTypeSearch search);

    Boolean saveDictType(DictType dictType);

    Boolean updateStatus(Long idDictType, Integer status);

    Boolean updateDelFlag(Long idDictType, Integer delFlag);

    DictType findById(Long idDictType);
}
