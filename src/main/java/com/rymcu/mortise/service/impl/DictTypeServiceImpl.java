package com.rymcu.mortise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rymcu.mortise.core.exception.ServiceException;
import com.rymcu.mortise.entity.DictType;
import com.rymcu.mortise.mapper.DictTypeMapper;
import com.rymcu.mortise.model.DictTypeSearch;
import com.rymcu.mortise.service.DictTypeService;
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
public class DictTypeServiceImpl extends ServiceImpl<DictTypeMapper, DictType> implements DictTypeService {

    @Override
    public IPage<DictType> findDictTypeList(Page<DictType> page, DictTypeSearch search) {
        List<DictType> list = baseMapper.selectDictTypeList(page, search.getQuery(), search.getTypeCode(), search.getStatus());
        page.setRecords(list);
        return page;
    }

    @Override
    public Boolean saveDictType(DictType dictType) {
        boolean isUpdate = dictType.getId() != null;
        if (isUpdate) {
            DictType oldDictType = baseMapper.selectById(dictType.getId());
            if (oldDictType == null) {
                throw new ServiceException("数据不存在");
            }
            oldDictType.setLabel(dictType.getLabel());
            oldDictType.setTypeCode(dictType.getTypeCode());
            oldDictType.setSortNo(dictType.getSortNo());
            oldDictType.setStatus(dictType.getStatus());
            oldDictType.setUpdatedBy(dictType.getUpdatedBy());
            oldDictType.setUpdatedTime(dictType.getUpdatedTime());
        }
        return baseMapper.insertOrUpdate(dictType);
    }

    @Override
    public Boolean updateStatus(Long idDictType, Integer status) {
        return baseMapper.updateStatus(idDictType, status) > 0;
    }

    @Override
    public Boolean updateDelFlag(Long idDictType, Integer delFlag) {
        return baseMapper.updateDelFlag(idDictType, delFlag) > 0;
    }

    @Override
    public DictType findById(Long idDictType) {
        return baseMapper.selectById(idDictType);
    }

    @Override
    public Boolean batchUpdateDelFlag(List<Long> idDictTypes, Integer delFlag) {
        return baseMapper.batchUpdateDelFlag(idDictTypes, delFlag) > 0;
    }
}
