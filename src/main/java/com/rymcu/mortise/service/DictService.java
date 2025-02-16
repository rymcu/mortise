package com.rymcu.mortise.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rymcu.mortise.entity.Dict;
import com.rymcu.mortise.model.DictSearch;

/**
 * Created on 2024/9/22 20:04.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service
 */
public interface DictService {
    IPage<Dict> findDictList(Page<Dict> page, DictSearch search);

    Boolean saveDict(Dict dict);

    Boolean updateStatus(Long idDict, Integer status);

    Boolean updateDelFlag(Long idDict, Integer delFlag);

    Dict findById(Long idDict);
}
