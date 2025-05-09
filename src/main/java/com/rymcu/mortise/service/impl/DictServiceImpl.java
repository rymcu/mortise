package com.rymcu.mortise.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rymcu.mortise.core.exception.ServiceException;
import com.rymcu.mortise.entity.Dict;
import com.rymcu.mortise.mapper.DictMapper;
import com.rymcu.mortise.model.BaseOption;
import com.rymcu.mortise.model.DictInfo;
import com.rymcu.mortise.model.DictSearch;
import com.rymcu.mortise.service.DictService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Created on 2024/9/22 20:04.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service.impl
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    @Override
    public IPage<Dict> findDictList(Page<Dict> page, DictSearch search) {
        List<Dict> list = baseMapper.selectDictList(page, search.getQuery(), search.getDictTypeCode(), search.getStatus());
        page.setRecords(list);
        return page;
    }

    @Override
    public Boolean saveDict(Dict dict) {
        boolean isUpdate = dict.getId() != null;
        if (isUpdate) {
            Dict oldDict = baseMapper.selectById(dict.getId());
            if (oldDict == null) {
                throw new ServiceException("数据不存在");
            }
            oldDict.setLabel(dict.getLabel());
            oldDict.setValue(dict.getValue());
            oldDict.setSortNo(dict.getSortNo());
            oldDict.setStatus(dict.getStatus());
            oldDict.setUpdatedBy(dict.getUpdatedBy());
            oldDict.setUpdatedTime(dict.getUpdatedTime());
        }
        return baseMapper.insertOrUpdate(dict);
    }

    @Override
    public Boolean updateStatus(Long idDict, Integer status) {
        return baseMapper.updateStatus(idDict, status) > 0;
    }

    @Override
    public Boolean updateDelFlag(Long idDict, Integer delFlag) {
        return baseMapper.updateDelFlag(idDict, delFlag) > 0;
    }

    @Override
    public Dict findById(Long idDict) {
        return baseMapper.selectById(idDict);
    }

    @Override
    public String findLabelByTypeCodeAndValue(String dictTypeCode, String value) {
        Dict dict = baseMapper.selectByTypeCodeAndValue(dictTypeCode, value);
        if (Objects.isNull(dict) || StringUtils.isBlank(dict.getLabel())) {
            return null;
        }
        return dict.getLabel();
    }

    @Override
    public DictInfo findDictInfo(String dictTypeCode, String value) {
        return baseMapper.selectDictInfo(dictTypeCode, value);
    }

    @Override
    public List<BaseOption> queryDictOptions(String dictTypeCode) {
        return baseMapper.selectDictOptions(dictTypeCode);
    }

    @Override
    public Boolean batchUpdateDelFlag(List<Long> idDictList, Integer delFlag) {
        return baseMapper.batchUpdateDelFlag(idDictList, delFlag) > 0;
    }
}
