package com.rymcu.mortise.service;

import com.rymcu.mortise.core.service.Service;
import com.rymcu.mortise.entity.Dict;
import com.rymcu.mortise.model.DictSearch;

import java.util.List;

/**
 * Created on 2024/9/22 20:04.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service
 */
public interface DictService extends Service<Dict> {
    List<Dict> findDictList(DictSearch search);

    Boolean saveDict(Dict dict);

    Boolean updateStatus(Long idDict, Integer status);

    Boolean updateDelFlag(Long idDict, Integer delFlag);
}
