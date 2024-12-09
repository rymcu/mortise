package com.rymcu.mortise.service.impl;

import com.rymcu.mortise.core.exception.ServiceException;
import com.rymcu.mortise.core.service.AbstractService;
import com.rymcu.mortise.entity.Dict;
import com.rymcu.mortise.mapper.DictMapper;
import com.rymcu.mortise.model.DictSearch;
import com.rymcu.mortise.service.DictService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created on 2024/9/22 20:04.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service.impl
 */
@Service
public class DictServiceImpl extends AbstractService<Dict> implements DictService {

    @Resource
    private DictMapper dictMapper;

    @Override
    public List<Dict> findDictList(DictSearch search) {
        return dictMapper.selectDictList(search.getQuery(), search.getDictTypeCode(), search.getStatus());
    }

    @Override
    public Boolean saveDict(Dict dict) {
        boolean isUpdate = dict.getIdDict() != null;
        if (isUpdate) {
            Dict oldDict = dictMapper.selectByPrimaryKey(dict.getIdDict());
            if (oldDict == null) {
                throw new ServiceException("数据不存在");
            }
            oldDict.setLabel(dict.getLabel());
            oldDict.setValue(dict.getValue());
            oldDict.setSortNo(dict.getSortNo());
            oldDict.setStatus(dict.getStatus());
            oldDict.setUpdatedBy(dict.getUpdatedBy());
            oldDict.setUpdatedTime(dict.getUpdatedTime());
            return dictMapper.updateByPrimaryKeySelective(dict) > 0;
        }
        return dictMapper.insertSelective(dict) > 0;
    }

    @Override
    public Boolean updateStatus(Long idDict, Integer status) {
        return dictMapper.updateStatus(idDict, status) > 0;
    }

    @Override
    public Boolean updateDelFlag(Long idDict, Integer delFlag) {
        return dictMapper.updateDelFlag(idDict, delFlag) > 0;
    }
}
